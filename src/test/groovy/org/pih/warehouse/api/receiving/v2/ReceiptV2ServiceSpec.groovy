package org.pih.warehouse.api.receiving.v2

import java.time.Instant
import java.time.LocalDate

import grails.core.GrailsApplication
import grails.testing.gorm.DataTest
import grails.validation.ValidationException
import grails.testing.services.ServiceUnitTest
import org.springframework.context.ApplicationContext
import org.springframework.validation.ObjectError
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryItemManager
import org.pih.warehouse.inventory.RefreshProductAvailabilityEvent
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptCompleteRequestCommand
import org.pih.warehouse.receiving.ReceiptDto
import org.pih.warehouse.receiving.ReceiptEditReceivingInfoCommand
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptItemCompleteRequest
import org.pih.warehouse.receiving.ReceiptItemEditReceivingInfoRequest
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.receiving.ReceiptV2Marker
import org.pih.warehouse.receiving.ShipmentForReceiptValidator
import org.pih.warehouse.receiving.ShipmentItemReceivedQuantitiesDto
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentStatusTransitionEvent
import org.pih.warehouse.shipping.ShipmentType

/**
 * The cancel-remaining logic and the shipment event decision are exercised through the service's private helpers
 * (Groovy dynamic dispatch does not enforce private), which keeps those tests free of the persistence fixture that
 * the full completeReceipt flow requires. The transaction and orchestration tests run the real flow against a
 * minimal valid domain graph.
 */
@Unroll
class ReceiptV2ServiceSpec extends Specification implements ServiceUnitTest<ReceiptV2Service>, DataTest {

    ShipmentService shipmentService
    TransactionIdentifierService transactionIdentifierService
    InventoryItemManager inventoryItemManager
    ReceiptIdentifierService receiptIdentifierService
    ReceiptService receiptService
    MessageLocalizer messageLocalizer
    ApplicationContext mainContext

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, ReceiptV2Marker, Shipment, ShipmentItem, ShipmentType, Container,
                Transaction, TransactionEntry, Product, InventoryItem, Inventory, Location)
    }

    void setup() {
        shipmentService = Mock(ShipmentService)
        transactionIdentifierService = Mock(TransactionIdentifierService) {
            generate(_) >> "TRX-001"
        }
        inventoryItemManager = Mock(InventoryItemManager)
        receiptIdentifierService = Mock(ReceiptIdentifierService)
        receiptService = Mock(ReceiptService)
        mainContext = Mock(ApplicationContext)
        // Resolves messages to their code
        messageLocalizer = Stub(MessageLocalizer) {
            localize(_ as ObjectError) >> { ObjectError error -> error.code }
        }

        service.messageLocalizer = messageLocalizer
        service.shipmentForReceiptValidator = new ShipmentForReceiptValidator()
        service.shipmentService = shipmentService
        service.transactionIdentifierService = transactionIdentifierService
        service.inventoryItemManager = inventoryItemManager
        service.receiptIdentifierService = receiptIdentifierService
        service.receiptService = receiptService
        service.grailsApplication = Stub(GrailsApplication) {
            getMainContext() >> mainContext
        }

        // TransactionType uses a uuid id generator, so a row with the fixed transfer-in id cannot be inserted into
        // the mocked datastore - intercept the static lookup instead.
        TransactionType transferInType = new TransactionType(name: "Transfer In", transactionCode: TransactionCode.CREDIT)
        transferInType.id = Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
        GroovySpy(TransactionType, global: true)
        TransactionType.get(_) >> transferInType
    }

    // ----------------------------------------------------------------------------------------------------------
    // startReceipt - the pending receipt and its empty original lines, against a minimal real domain graph.
    // ----------------------------------------------------------------------------------------------------------

    void 'startReceipt should create an empty original receipt item for every shipment item left to receive'() {
        given:
        ShipmentItem firstShipmentItem = buildShipmentItem(100)
        ShipmentItem secondShipmentItem = buildShipmentItem(50)
        Shipment shipment = buildReceivableShipment([firstShipmentItem, secondShipmentItem])

        when:
        ReceiptDto result = service.startReceipt(shipment.id)

        then:
        1 * receiptIdentifierService.generate(_ as Receipt) >> "RCPT-001"
        1 * receiptService.createTemporaryReceivingBin(shipment)

        and: 'a pending receipt is created with one line per shipment item'
        assert shipment.receipts.size() == 1
        Receipt receipt = shipment.receipts.first()
        assert receipt.receiptStatusCode == ReceiptStatusCode.PENDING
        assert receipt.receiptNumber == "RCPT-001"
        assert receipt.receiptItems.size() == 2

        and: 'each line is an empty original mirroring its shipment item'
        ReceiptItem originalItem = receipt.receiptItems.find { it.shipmentItem == firstShipmentItem }
        assert originalItem.quantityReceived == null
        assert originalItem.isSplitItem == Boolean.FALSE
        assert originalItem.quantityShipped == 100
        assert originalItem.product == firstShipmentItem.product
        assert originalItem.inventoryItem == firstShipmentItem.inventoryItem
        assert originalItem.sortOrder == 0
        assert originalItem.binLocation == null
        assert firstShipmentItem.receiptItems.contains(originalItem)
        assert receipt.receiptItems.find { it.shipmentItem == secondShipmentItem }.quantityShipped == 50

        and: 'the response carries the created lines'
        assert result.receiptStatus == ReceiptStatusCode.PENDING
        assert result.receiptItems.size() == 2
        assert result.receiptItems.every { it.quantityReceived == null && !it.isSplitItem }
    }

    void 'startReceipt should assign the receiving bin of the shipment to every line it creates'() {
        given:
        Shipment shipment = buildReceivableShipment([buildShipmentItem(100), buildShipmentItem(50)])
        Location receivingBin = new Location(name: "R-TEST")

        when:
        service.startReceipt(shipment.id)

        then:
        1 * receiptIdentifierService.generate(_ as Receipt) >> "RCPT-001"
        1 * receiptService.createTemporaryReceivingBin(shipment) >> receivingBin

        and: 'every line starts out in the receiving bin, so it is received into it unless the user moves it'
        Set<ReceiptItem> receiptItems = shipment.receipts.first().receiptItems
        assert receiptItems.size() == 2
        assert receiptItems.every { it.binLocation == receivingBin }
    }

    void 'startReceipt should skip shipment items already fully consumed by previous receipts'() {
        given: 'one shipment item fully received by a completed receipt and one still left to receive'
        ShipmentItem consumedItem = buildShipmentItem(30)
        ShipmentItem openItem = buildShipmentItem(50)
        Shipment shipment = buildReceivableShipment([consumedItem, openItem])
        ReceiptItem consumedOriginal = buildReceiptItem(consumedItem, 30)
        ReceiptItem openOriginal = buildReceiptItem(openItem, 40)
        createReceipt(shipment, [consumedOriginal, openOriginal], ReceiptStatusCode.RECEIVED)

        when:
        ReceiptDto result = service.startReceipt(shipment.id)

        then: 'the new receipt only carries an original line for the item that still has a remainder'
        assert result.receiptItems.size() == 1
        Receipt receipt = shipment.receipts.find { it.receiptStatusCode == ReceiptStatusCode.PENDING }
        assert receipt.receiptItems.size() == 1
        ReceiptItem newOriginalItem = receipt.receiptItems.first()
        assert newOriginalItem.shipmentItem == openItem
        assert newOriginalItem.quantityReceived == null
        assert newOriginalItem.isSplitItem == Boolean.FALSE
        assert newOriginalItem.quantityShipped == 50

        and: 'the consumed item keeps only its previous line'
        assert consumedItem.receiptItems.size() == 1
    }

    void 'startReceipt should mark the created receipt as a v2 receipt'() {
        given:
        Shipment shipment = buildReceivableShipment([buildShipmentItem(100)])

        when:
        service.startReceipt(shipment.id)

        then: 'a marker row ties the pending receipt to the v2 workflow'
        assert ReceiptV2Marker.count() == 1
        assert ReceiptV2Marker.list().first().receipt == shipment.receipts.first()
    }

    void 'startReceipt should reject a shipment that already has a pending receipt'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        createReceipt(shipment, [buildReceiptItem(shipmentItem, 0)], ReceiptStatusCode.PENDING)

        when:
        service.startReceipt(shipment.id)

        then:
        ValidationException e = thrown(ValidationException)
        assert e.errors.getFieldError("receipts").code == "shipment.pendingReceiptExists.message"
    }

    void 'startReceipt should reject a shipment that has not been shipped yet'() {
        given: 'a shipment still pending on the outbound side'
        Shipment shipment = buildShipment()
        shipment.addToShipmentItems(buildShipmentItem(100))
        shipment.save(failOnError: true, flush: true)
        shipment.currentStatus = ShipmentStatusCode.PENDING

        when:
        service.startReceipt(shipment.id)

        then:
        ValidationException e = thrown(ValidationException)
        assert e.errors.getFieldError("currentStatus").code == "stockMovement.hasNotBeenShipped.message"
    }

    void 'startReceipt should reject a shipment already fully consumed by lines received against an edited product'() {
        given: 'a completed receipt that consumed the full quantity via an edited-product split plus a cancel'
        ShipmentItem shipmentItem = buildShipmentItem(75)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 0)
        originalItem.quantityCanceled = 15
        ReceiptItem editedSplitItem = buildReceiptItem(shipmentItem, 60, [quantityShipped: 0, isSplitItem: true])
        editedSplitItem.product = new Product(name: "Edited product")
        createReceipt(shipment, [originalItem, editedSplitItem], ReceiptStatusCode.RECEIVED)

        when:
        service.startReceipt(shipment.id)

        then: 'the legacy product-filtered check would still see 60 to receive - the v2 math rejects the start'
        ValidationException e = thrown(ValidationException)
        assert e.errors.getFieldError("shipmentItems").code == "stockMovement.hasAlreadyBeenReceived.message"
    }

    // ----------------------------------------------------------------------------------------------------------
    // getReceivingBlockedReason - the guard of the receiving page
    // ----------------------------------------------------------------------------------------------------------

    void 'getReceivingBlockedReason should return no reason for a shipped shipment at its destination'() {
        given:
        Shipment shipment = buildReceivableShipment([buildShipmentItem(100)])

        expect:
        service.getReceivingBlockedReason(shipment, shipment.destination) == null
    }

    void 'getReceivingBlockedReason should return no reason for a shipment with a pending receipt'() {
        given: 'a receipt started by a previous visit to the receiving page'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        createReceipt(shipment, [buildReceiptItem(shipmentItem, 0)], ReceiptStatusCode.PENDING)

        expect: 'unlike startReceipt, resuming it is allowed'
        service.getReceivingBlockedReason(shipment, shipment.destination) == null
    }

    void 'getReceivingBlockedReason should report a shipment that has not been shipped'() {
        given:
        Shipment shipment = buildReceivableShipment([buildShipmentItem(100)])
        shipment.currentStatus = ShipmentStatusCode.PENDING

        expect:
        service.getReceivingBlockedReason(shipment, shipment.destination) ==
                "stockMovement.hasNotBeenShipped.message"
    }

    void 'getReceivingBlockedReason should report a location other than the destination of the shipment'() {
        given:
        Shipment shipment = buildReceivableShipment([buildShipmentItem(100)])
        Location otherLocation = new Location(name: "Other location")
        otherLocation.id = "other-location-id"

        expect:
        service.getReceivingBlockedReason(shipment, otherLocation) ==
                "stockMovement.isDifferentLocation.message"
    }

    // ----------------------------------------------------------------------------------------------------------
    // getReceivedQuantitiesByShipmentItemId - the workflow-aware totals rendered by legacy views.
    // ----------------------------------------------------------------------------------------------------------

    void 'getReceivedQuantitiesByShipmentItemId should count v2 lines received against an edited product'() {
        given: 'a completed v2 receipt: a canceled remainder on the original plus an edited-product split'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 40)
        originalItem.quantityCanceled = 10
        ReceiptItem editedSplitItem = buildReceiptItem(shipmentItem, 50, [quantityShipped: 0, isSplitItem: true])
        editedSplitItem.product = new Product(name: "Edited product")
        markAsV2(createReceipt(shipment, [originalItem, editedSplitItem], ReceiptStatusCode.RECEIVED))

        when:
        Map<String, ShipmentItemReceivedQuantitiesDto> result = service.getReceivedQuantitiesByShipmentItemId(shipment)

        then: 'the edited-product line counts towards the totals, so the item reports fully received'
        assert result.size() == 1
        with(result[shipmentItem.id]) {
            quantityReceived == 90
            quantityCanceled == 10
            fullyReceived
        }
    }

    void 'getReceivedQuantitiesByShipmentItemId should skip old-workflow lines received against an edited product'() {
        given: 'a completed receipt without a v2 marker carrying an added line with an edited product'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 40)
        originalItem.quantityCanceled = 10
        ReceiptItem editedProductItem = buildReceiptItem(shipmentItem, 50, [quantityShipped: 0])
        editedProductItem.product = new Product(name: "Edited product")
        createReceipt(shipment, [originalItem, editedProductItem], ReceiptStatusCode.RECEIVED)

        when:
        Map<String, ShipmentItemReceivedQuantitiesDto> result = service.getReceivedQuantitiesByShipmentItemId(shipment)

        then: 'only the matching-product line counts - an old-workflow edited line does not consume the shipment item'
        with(result[shipmentItem.id]) {
            quantityReceived == 40
            quantityCanceled == 10
            !fullyReceived
        }
    }

    void 'getReceivedQuantitiesByShipmentItemId should ignore the lines of pending receipts'() {
        given: 'a pending v2 receipt with quantities already entered'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 60)
        markAsV2(createReceipt(shipment, [originalItem], ReceiptStatusCode.PENDING))

        when:
        Map<String, ShipmentItemReceivedQuantitiesDto> result = service.getReceivedQuantitiesByShipmentItemId(shipment)

        then: 'the item still gets an entry, with nothing counted yet'
        with(result[shipmentItem.id]) {
            quantityReceived == 0
            quantityCanceled == 0
            !fullyReceived
        }
    }

    void 'getReceivedQuantitiesByShipmentItemId should discriminate edited-product lines per receipt on the same shipment item'() {
        given: 'a completed old-workflow receipt whose added line carries an edited product'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Shipment shipment = buildReceivableShipment([shipmentItem])
        ReceiptItem oldOriginalItem = buildReceiptItem(shipmentItem, 20)
        ReceiptItem oldEditedItem = buildReceiptItem(shipmentItem, 30, [quantityShipped: 0])
        oldEditedItem.product = new Product(name: "Old edited product")
        createReceipt(shipment, [oldOriginalItem, oldEditedItem], ReceiptStatusCode.RECEIVED)

        and: 'a completed v2 receipt on the same shipment item with its own edited-product split'
        ReceiptItem v2OriginalItem = buildReceiptItem(shipmentItem, 10)
        ReceiptItem v2EditedSplitItem = buildReceiptItem(shipmentItem, 40, [quantityShipped: 0, isSplitItem: true])
        v2EditedSplitItem.product = new Product(name: "V2 edited product")
        markAsV2(createReceipt(shipment, [v2OriginalItem, v2EditedSplitItem], ReceiptStatusCode.RECEIVED))

        when:
        Map<String, ShipmentItemReceivedQuantitiesDto> result = service.getReceivedQuantitiesByShipmentItemId(shipment)

        then: 'the v2 edited line counts towards the totals, the old-workflow edited line does not'
        with(result[shipmentItem.id]) {
            quantityReceived == 70
            quantityCanceled == 0
            !fullyReceived
        }
    }

    // ----------------------------------------------------------------------------------------------------------
    // editReceivingInfo - the split lines added while receiving, against a minimal real domain graph.
    // ----------------------------------------------------------------------------------------------------------

    void 'editReceivingInfo should create split lines with a quantity shipped of zero'() {
        given: 'a pending receipt whose shipment item already carries its original line'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 40)
        Receipt receipt = createReceipt(shipment, [originalItem], ReceiptStatusCode.PENDING)

        InventoryItem splitLot = new InventoryItem(product: shipmentItem.product, lotNumber: "LOT-2")
        ReceiptEditReceivingInfoCommand command = new ReceiptEditReceivingInfoCommand(
                receipt: receipt,
                shipmentItem: shipmentItem,
                itemsToSave: [new ReceiptItemEditReceivingInfoRequest(
                        rowId: "temp-1",
                        product: shipmentItem.product,
                        lotNumber: "LOT-2",
                        quantityReceiving: 30,
                )],
        )

        when:
        service.editReceivingInfo(command)

        then:
        1 * inventoryItemManager.getOrCreateInventoryItem(shipmentItem.product, "LOT-2", null) >> splitLot

        and: 'the new line is flagged as a split server-side and carries no quantity shipped of its own'
        ReceiptItem splitItem = receipt.receiptItems.find { it.isSplitItem }
        assert splitItem.quantityShipped == 0
        assert splitItem.quantityReceived == 30
        assert splitItem.inventoryItem == splitLot
        assert shipmentItem.receiptItems.contains(splitItem)
        assert originalItem.quantityShipped == 100
    }

    void 'editReceivingInfo should not change the split flag or quantity shipped when updating an existing line'() {
        given: 'a pending receipt with the original line already carrying received quantity'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 40)
        Receipt receipt = createReceipt(shipment, [originalItem], ReceiptStatusCode.PENDING)

        ReceiptEditReceivingInfoCommand command = new ReceiptEditReceivingInfoCommand(
                receipt: receipt,
                shipmentItem: shipmentItem,
                itemsToSave: [new ReceiptItemEditReceivingInfoRequest(
                        receiptItem: originalItem,
                        product: shipmentItem.product,
                        lotNumber: "LOT-1",
                        quantityReceiving: 60,
                )],
        )

        when:
        service.editReceivingInfo(command)

        then:
        1 * inventoryItemManager.getOrCreateInventoryItem(shipmentItem.product, "LOT-1", null) >>
                shipmentItem.inventoryItem

        and: 'only the receiving info changes - the line stays the original with its full quantity shipped'
        assert originalItem.quantityReceived == 60
        assert originalItem.isSplitItem == Boolean.FALSE
        assert originalItem.quantityShipped == 100
    }

    void 'editReceivingInfo should apply an edited expiration date to the lot being received against'() {
        given: 'a pending receipt whose original line is received against an existing lot'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 40)
        Receipt receipt = createReceipt(shipment, [originalItem], ReceiptStatusCode.PENDING)

        LocalDate newExpirationDate = LocalDate.of(2028, 3, 1)
        Date expectedExpirationDate = JavaUtilDateParser.asDate(newExpirationDate)

        ReceiptEditReceivingInfoCommand command = new ReceiptEditReceivingInfoCommand(
                receipt: receipt,
                shipmentItem: shipmentItem,
                itemsToSave: [new ReceiptItemEditReceivingInfoRequest(
                        receiptItem: originalItem,
                        product: shipmentItem.product,
                        lotNumber: "LOT-1",
                        expirationDate: newExpirationDate,
                        quantityReceiving: 40,
                )],
        )

        when:
        service.editReceivingInfo(command)

        then: 'the date is pushed onto the existing lot, which getOrCreateInventoryItem alone would have left as is'
        1 * inventoryItemManager.getOrCreateInventoryItem(shipmentItem.product, "LOT-1", expectedExpirationDate) >>
                shipmentItem.inventoryItem
        1 * inventoryItemManager.updateExpirationDate(shipmentItem.inventoryItem, expectedExpirationDate) >>
                { InventoryItem inventoryItem, Date expirationDate ->
                    inventoryItem.expirationDate = expirationDate
                    return inventoryItem
                }

        and: 'the received line follows the lot'
        assert originalItem.expirationDate == expectedExpirationDate
    }

    void 'editReceivingInfo should clear the expiration date of the lot when the date is emptied'() {
        given: 'a pending receipt whose original line is received against a lot carrying a date'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        shipmentItem.inventoryItem.expirationDate = JavaUtilDateParser.asDate(LocalDate.of(2028, 3, 1))
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 40)
        Receipt receipt = createReceipt(shipment, [originalItem], ReceiptStatusCode.PENDING)

        ReceiptEditReceivingInfoCommand command = new ReceiptEditReceivingInfoCommand(
                receipt: receipt,
                shipmentItem: shipmentItem,
                itemsToSave: [new ReceiptItemEditReceivingInfoRequest(
                        receiptItem: originalItem,
                        product: shipmentItem.product,
                        lotNumber: "LOT-1",
                        expirationDate: null,
                        quantityReceiving: 40,
                )],
        )

        when:
        service.editReceivingInfo(command)

        then: 'the emptied date reaches the lot instead of being dropped on the way'
        1 * inventoryItemManager.getOrCreateInventoryItem(shipmentItem.product, "LOT-1", null) >>
                shipmentItem.inventoryItem
        1 * inventoryItemManager.updateExpirationDate(shipmentItem.inventoryItem, null) >>
                { InventoryItem inventoryItem, Date expirationDate ->
                    inventoryItem.expirationDate = expirationDate
                    return inventoryItem
                }

        and: 'the received line follows the lot'
        assert originalItem.expirationDate == null
    }

    // ----------------------------------------------------------------------------------------------------------
    // cancelRemainingQuantities - runs on the in-memory graph only, with the receipt already flagged as RECEIVED
    // (which is the contract completeReceipt upholds before applying the cancels).
    // ----------------------------------------------------------------------------------------------------------

    void 'cancelRemainingQuantities should cancel the line remainder of a flagged line'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 70)
        Receipt receipt = buildReceivedReceipt([receiptItem])

        when:
        service.cancelRemainingQuantities(receipt, [completeRequest(receiptItem, true)])

        then:
        assert receiptItem.quantityCanceled == 30
        assert shipmentItem.quantityRemaining == 0
    }

    void 'cancelRemainingQuantities should leave lines without the flag untouched'() {
        given: 'one line sent with the flag disabled and one line missing from the request'
        ShipmentItem firstShipmentItem = buildShipmentItem(100)
        ReceiptItem unflaggedItem = buildReceiptItem(firstShipmentItem, 70)
        ShipmentItem secondShipmentItem = buildShipmentItem(50)
        ReceiptItem missingItem = buildReceiptItem(secondShipmentItem, 20)
        Receipt receipt = buildReceivedReceipt([unflaggedItem, missingItem])

        when:
        service.cancelRemainingQuantities(receipt, [completeRequest(unflaggedItem, false)])

        then:
        assert unflaggedItem.quantityCanceled == null
        assert missingItem.quantityCanceled == null
    }

    void 'cancelRemainingQuantities should account for quantities received by previous receipts'() {
        given: 'a shipment item that already had 30 received by a previous receipt'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem previousItem = buildReceiptItem(shipmentItem, 30)
        buildReceivedReceipt([previousItem])

        and: 'a current receipt line receiving 20 more, still carrying the full quantity as its quantity shipped'
        ReceiptItem currentItem = buildReceiptItem(shipmentItem, 20)
        Receipt receipt = buildReceivedReceipt([currentItem])

        when:
        service.cancelRemainingQuantities(receipt, [completeRequest(currentItem, true)])

        then: 'the cap limits the line remainder (80) to what the shipment item actually has left'
        assert currentItem.quantityCanceled == 50
    }

    void 'cancelRemainingQuantities should cancel only on the original line when both lines are flagged'() {
        given: 'an original line and a split line that received part of the quantity'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 20)
        ReceiptItem splitItem = buildReceiptItem(shipmentItem, 30, [quantityShipped: 0, isSplitItem: true])
        Receipt receipt = buildReceivedReceipt([originalItem, splitItem])

        when: 'both lines are flagged'
        service.cancelRemainingQuantities(
                receipt, [completeRequest(originalItem, true), completeRequest(splitItem, true)])

        then: 'the split line is skipped and the original line cancels the shipment item remainder'
        assert originalItem.quantityCanceled == 50
        assert splitItem.quantityCanceled == null
        assert shipmentItem.quantityRemaining == 0
    }

    void 'cancelRemainingQuantities should not set a negative canceled quantity on an over-received line'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 120)
        Receipt receipt = buildReceivedReceipt([receiptItem])

        when:
        service.cancelRemainingQuantities(receipt, [completeRequest(receiptItem, true)])

        then:
        assert receiptItem.quantityCanceled == 0
    }

    void 'cancelRemainingQuantities should not cancel when an over-received sibling line already covered the shipment item'() {
        given: 'a split line over-received enough to cover the full shipment item quantity'
        ShipmentItem shipmentItem = buildShipmentItem(500)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 200)
        ReceiptItem overReceivedSplitItem = buildReceiptItem(shipmentItem, 350, [quantityShipped: 0, isSplitItem: true])
        Receipt receipt = buildReceivedReceipt([originalItem, overReceivedSplitItem])

        when: 'only the original line is flagged'
        service.cancelRemainingQuantities(receipt, [completeRequest(originalItem, true)])

        then: 'nothing is left to cancel on the shipment item'
        assert originalItem.quantityCanceled == 0
    }

    void 'cancelRemainingQuantities should cancel the shipment item remainder on the original line when split lines received part of it'() {
        given: 'an original line and a split line (quantity shipped zero) that received part of the quantity'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 20)
        ReceiptItem splitItem = buildReceiptItem(shipmentItem, 30, [quantityShipped: 0, isSplitItem: true])
        Receipt receipt = buildReceivedReceipt([originalItem, splitItem])

        when: 'only the original line is flagged'
        service.cancelRemainingQuantities(receipt, [completeRequest(originalItem, true)])

        then: 'the original line cancels exactly what is left after both lines received their quantities'
        assert originalItem.quantityCanceled == 50
        assert splitItem.quantityCanceled == null
        assert shipmentItem.quantityRemaining == 0
    }

    void 'cancelRemainingQuantities should count split lines received against an edited product towards the shipment item remainder'() {
        given: 'an original line (nothing received) and split lines whose product was edited while receiving'
        ShipmentItem shipmentItem = buildShipmentItem(45)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 0)
        ReceiptItem firstEditedItem = buildReceiptItem(shipmentItem, 2, [quantityShipped: 0, isSplitItem: true])
        firstEditedItem.product = new Product(name: "Edited product")
        ReceiptItem secondEditedItem = buildReceiptItem(shipmentItem, 6, [quantityShipped: 0, isSplitItem: true])
        secondEditedItem.product = new Product(name: "Other edited product")
        Receipt receipt = buildReceivedReceipt([originalItem, firstEditedItem, secondEditedItem])

        when: 'only the original line is flagged'
        service.cancelRemainingQuantities(receipt, [completeRequest(originalItem, true)])

        then: 'the split quantities consume the remainder even though their product no longer matches the shipment item'
        assert originalItem.quantityCanceled == 37
    }

    void 'cancelRemainingQuantities should skip a flagged split line'() {
        given:
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 20)
        ReceiptItem splitItem = buildReceiptItem(shipmentItem, 30, [quantityShipped: 0, isSplitItem: true])
        Receipt receipt = buildReceivedReceipt([originalItem, splitItem])

        when: 'only the split line is flagged (the request validator normally rejects this)'
        service.cancelRemainingQuantities(receipt, [completeRequest(splitItem, true)])

        then: 'nothing is canceled and the shipment item keeps its remainder open'
        assert splitItem.quantityCanceled == null
        assert originalItem.quantityCanceled == null
        assert shipmentItem.quantityRemaining == 50
    }

    void 'cancelRemainingQuantities should cancel every line of the receipt when the destination does not support partial receiving'() {
        given: 'two under-received lines'
        ShipmentItem firstShipmentItem = buildShipmentItem(100)
        ReceiptItem firstItem = buildReceiptItem(firstShipmentItem, 70)
        ShipmentItem secondShipmentItem = buildShipmentItem(50)
        ReceiptItem secondItem = buildReceiptItem(secondShipmentItem, 20)
        Receipt receipt = buildReceivedReceipt([firstItem, secondItem], false)

        when: 'nothing is flagged in the request'
        service.cancelRemainingQuantities(receipt, [])

        then: 'both lines cancel their remainder, so the shipment has nothing left to receive'
        assert firstItem.quantityCanceled == 30
        assert secondItem.quantityCanceled == 30
        assert firstShipmentItem.quantityRemaining == 0
        assert secondShipmentItem.quantityRemaining == 0
    }

    void 'cancelRemainingQuantities should still cancel on the original line only when the destination does not support partial receiving'() {
        given: 'an original line and a split line that received part of the quantity'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 20)
        ReceiptItem splitItem = buildReceiptItem(shipmentItem, 30, [quantityShipped: 0, isSplitItem: true])
        Receipt receipt = buildReceivedReceipt([originalItem, splitItem], false)

        when:
        service.cancelRemainingQuantities(receipt, [])

        then: 'the split line is skipped and the original line cancels the shipment item remainder'
        assert originalItem.quantityCanceled == 50
        assert splitItem.quantityCanceled == null
        assert shipmentItem.quantityRemaining == 0
    }

    // ----------------------------------------------------------------------------------------------------------
    // createShipmentReceivedEvent - the RECEIVED vs PARTIALLY_RECEIVED decision. The shipment is stubbed (mirrors
    // how the legacy ReceiptServiceSpec tests savePartialReceiptEvent), except for the fully-received state, which
    // the service computes itself (see isShipmentFullyReceived), so it is driven by a real item graph instead.
    // ----------------------------------------------------------------------------------------------------------

    void 'createShipmentReceivedEvent should create #expectedEventDescription event when #caseDescription'() {
        given: 'a shipment item either fully received or with part of its quantity still open'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, fullyReceived ? 100 : 40)
        buildReceipt([receiptItem], ReceiptStatusCode.RECEIVED)

        Location destination = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> supportsPartialReceiving
        }
        // The data variables must not shadow the stubbed method names - a shadowed name makes the closure call
        // the (boolean) variable instead of registering the interaction, leaving the stub on its default response.
        Shipment shipment = Stub(Shipment) {
            getDestination() >> destination
            wasReceived() >> alreadyReceived
            wasPartiallyReceived() >> alreadyPartiallyReceived
            getShipmentItems() >> ([shipmentItem] as Set)
        }
        Receipt receipt = new Receipt(actualDeliveryDate: new Date(), shipment: shipment)

        when:
        service.createShipmentReceivedEvent(receipt)

        then:
        expectedReceived * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        expectedPartiallyReceived * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)

        where:
        caseDescription                               | supportsPartialReceiving | fullyReceived | alreadyReceived | alreadyPartiallyReceived || expectedReceived | expectedPartiallyReceived
        'the shipment ends up fully received'         | true                     | true          | false           | false                    || 1                | 0
        'the destination does not support partials'   | false                    | false         | false           | false                    || 1                | 0
        'the shipment is partially received first'    | true                     | false         | false           | false                    || 0                | 1
        'the shipment was already partially received' | true                     | false         | false           | true                     || 0                | 0

        expectedEventDescription = expectedReceived ? "a RECEIVED" : (expectedPartiallyReceived ? "a PARTIALLY_RECEIVED" : "no")
    }

    void 'createShipmentReceivedEvent should count lines received against an edited product towards fully received'() {
        given: 'a shipment item fully consumed by an edited-product split plus a cancel on the original line'
        ShipmentItem shipmentItem = buildShipmentItem(75)
        ReceiptItem originalItem = buildReceiptItem(shipmentItem, 0)
        originalItem.quantityCanceled = 15
        ReceiptItem editedSplitItem = buildReceiptItem(shipmentItem, 60, [quantityShipped: 0, isSplitItem: true])
        editedSplitItem.product = new Product(name: "Edited product")
        buildReceipt([originalItem, editedSplitItem], ReceiptStatusCode.RECEIVED)

        Location destination = Stub(Location) {
            supports(ActivityCode.PARTIAL_RECEIVING) >> true
        }
        Shipment shipment = Stub(Shipment) {
            getDestination() >> destination
            getShipmentItems() >> ([shipmentItem] as Set)
        }
        Receipt receipt = new Receipt(actualDeliveryDate: new Date(), shipment: shipment)

        when:
        service.createShipmentReceivedEvent(receipt)

        then: 'the legacy product-filtered check would see it as partial - the v2 math sees it as fully received'
        1 * shipmentService.createShipmentEvent(_, _, EventCode.RECEIVED, _)
        0 * shipmentService.createShipmentEvent(_, _, EventCode.PARTIALLY_RECEIVED, _)
    }

    // ----------------------------------------------------------------------------------------------------------
    // createInboundTransaction - runs against a minimal real domain graph so the transaction actually persists.
    // ----------------------------------------------------------------------------------------------------------

    void 'createInboundTransaction should record an inbound transfer transaction crediting the received quantities'() {
        given:
        Shipment shipment = buildShipment()
        ShipmentItem firstShipmentItem = buildShipmentItem(100)
        ReceiptItem firstItem = buildReceiptItem(firstShipmentItem, 70, [binLocation: new Location(name: "Bin")])
        ShipmentItem secondShipmentItem = buildShipmentItem(50)
        ReceiptItem secondItem = buildReceiptItem(secondShipmentItem, null)
        Receipt receipt = createReceipt(shipment, [firstItem, secondItem], ReceiptStatusCode.RECEIVED)

        when:
        Transaction transaction = service.createInboundTransaction(receipt)

        then: 'a transfer-in transaction is persisted and associated with the receipt and shipment'
        1 * transactionIdentifierService.generate(_ as Transaction) >> "TRX-002"
        assert Transaction.list() == [transaction]
        assert transaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
        assert transaction.transactionNumber == "TRX-002"
        assert transaction.receipt == receipt
        assert transaction.incomingShipment == shipment
        assert transaction.source == shipment.origin
        assert transaction.destination == null
        assert transaction.inventory == shipment.destination.inventory
        assert transaction.transactionDate == receipt.actualDeliveryDate
        assert shipment.incomingTransactions.contains(transaction)

        and: 'each receipt item is credited with its received quantity (defaulting to zero)'
        assert transaction.transactionEntries.size() == 2
        TransactionEntry firstEntry = transaction.transactionEntries.find { it.inventoryItem == firstItem.inventoryItem }
        assert firstEntry.quantity == 70
        assert firstEntry.binLocation == firstItem.binLocation
        TransactionEntry secondEntry = transaction.transactionEntries.find { it.inventoryItem == secondItem.inventoryItem }
        assert secondEntry.quantity == 0
    }

    void 'createInboundTransaction should resolve a missing inventory item from the lot fields'() {
        given: 'a receipt item without an inventory item (legacy shipment item)'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        Date expirationDate = new Date() + 365
        ReceiptItem receiptItem = buildReceiptItem(
                shipmentItem, 100, [inventoryItem: null, lotNumber: "LOT-9", expirationDate: expirationDate])
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.RECEIVED)

        InventoryItem resolvedInventoryItem = new InventoryItem(product: receiptItem.product, lotNumber: "LOT-9")

        when:
        Transaction transaction = service.createInboundTransaction(receipt)

        then:
        1 * inventoryItemManager.getOrCreateInventoryItem(receiptItem.product, "LOT-9", expirationDate) >> resolvedInventoryItem
        assert transaction.transactionEntries.first().inventoryItem == resolvedInventoryItem
    }

    void 'createInboundTransaction should fail when the destination has no inventory'() {
        given:
        Shipment shipment = buildShipment(null)
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 100)
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.RECEIVED)

        when:
        service.createInboundTransaction(receipt)

        then:
        thrown(IllegalStateException)
    }

    // ----------------------------------------------------------------------------------------------------------
    // completeReceipt - the full orchestration against a minimal real domain graph.
    // ----------------------------------------------------------------------------------------------------------

    void 'completeReceipt should receive the receipt, apply cancels, record the transaction and publish the events'() {
        given: 'a pending receipt receiving 70 of 100 with cancel remaining requested'
        ShipmentItem shipmentItem = buildShipmentItem(100)
        // The item hangs off the shipment because the RECEIVED-vs-PARTIALLY_RECEIVED decision asserted below is
        // computed from the shipment's items (see isShipmentFullyReceived).
        Shipment shipment = buildReceivableShipment([shipmentItem])
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 70)
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.PENDING)

        Instant dateDelivered = Instant.now().minusSeconds(3600)
        ReceiptCompleteRequestCommand command = new ReceiptCompleteRequestCommand(
                receipt: receipt,
                dateDelivered: dateDelivered,
                itemsToComplete: [completeRequest(receiptItem, true)],
        )

        when:
        ReceiptDto result = service.completeReceipt(command)

        then: 'the receipt is flagged as received with the delivery date from the command'
        assert receipt.receiptStatusCode == ReceiptStatusCode.RECEIVED
        assert receipt.actualDeliveryDate == Date.from(dateDelivered)
        assert result.receiptStatus == ReceiptStatusCode.RECEIVED

        and: 'the remaining quantity is canceled'
        assert receiptItem.quantityCanceled == 30

        and: 'the shipment event is created and the inbound transaction is recorded'
        // Interaction arguments are evaluated before when: runs, so match on the date the command carries instead
        // of receipt.actualDeliveryDate, which completeReceipt overwrites with that very value (asserted above).
        1 * shipmentService.createShipmentEvent(shipment, Date.from(dateDelivered), EventCode.RECEIVED, shipment.destination)
        assert Transaction.list().size() == 1

        and: 'the status transition event carries the completed receipt'
        1 * mainContext.publishEvent({ Object event ->
            event instanceof ShipmentStatusTransitionEvent && event.receipt == receipt &&
                    event.shipmentStatusCode == ShipmentStatusCode.RECEIVED
        })

        and: 'the product availability refresh is triggered for the created transaction'
        1 * mainContext.publishEvent(_ as RefreshProductAvailabilityEvent)
    }

    void 'completeReceipt should keep the delivery date already carried by the receipt when the command has none'() {
        given:
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 100)
        Date originalDateDelivered = new Date() - 2
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.PENDING, originalDateDelivered)

        when:
        service.completeReceipt(new ReceiptCompleteRequestCommand(receipt: receipt))

        then:
        assert receipt.actualDeliveryDate == originalDateDelivered
    }

    void 'completeReceipt should apply the cancels only after flagging the receipt as received'() {
        given: 'a shipment item that already had 30 received by a previous receipt'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem previousItem = buildReceiptItem(shipmentItem, 30)
        createReceipt(shipment, [previousItem], ReceiptStatusCode.RECEIVED)

        and: 'a pending receipt receiving 20 more with cancel remaining requested'
        ReceiptItem currentItem = buildReceiptItem(shipmentItem, 20)
        Receipt receipt = createReceipt(shipment, [currentItem], ReceiptStatusCode.PENDING)

        ReceiptCompleteRequestCommand command = new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [completeRequest(currentItem, true)],
        )

        when:
        service.completeReceipt(command)

        then: 'the current receipt quantities are already counted, so only the true remainder is canceled'
        assert currentItem.quantityCanceled == 50
    }

    void 'completeReceipt should cancel every remainder when the destination does not support partial receiving'() {
        given: 'a pending receipt of a destination that cannot receive in parts, receiving 70 of 100'
        Shipment shipment = buildShipment(new Inventory(), false)
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 70)
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.PENDING)

        when: 'the request carries no lines to cancel (the client hides the cancel controls)'
        service.completeReceipt(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'the remainder is canceled anyway and the shipment is closed as received'
        assert receiptItem.quantityCanceled == 30
        1 * shipmentService.createShipmentEvent(shipment, _, EventCode.RECEIVED, shipment.destination)
    }

    void 'completeReceipt should write out a zero on the lines that were never given a quantity'() {
        given: 'a pending receipt whose original line was started but never received against'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, null)
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.PENDING)

        when:
        service.completeReceipt(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'the "nothing entered yet" null does not outlive the pending receipt'
        assert receiptItem.quantityReceived == 0
    }

    void 'completeReceipt should cancel the whole quantity of a line left empty when the destination does not support partial receiving'() {
        given: 'a pending receipt of a destination that cannot receive in parts, whose only line was never received against'
        Shipment shipment = buildShipment(new Inventory(), false)
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, null)
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.PENDING)

        when: 'the request carries no lines to cancel'
        service.completeReceipt(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'the empty line counts as nothing received, so the full shipped quantity is canceled'
        assert receiptItem.quantityCanceled == 100

        and: 'the "nothing entered yet" null is written out as a zero'
        assert receiptItem.quantityReceived == 0
    }

    // ----------------------------------------------------------------------------------------------------------
    // sortByPackLevel - the ordering of the packing list view.
    // ----------------------------------------------------------------------------------------------------------

    void 'sortByPackLevel should order the items by their pack levels'() {
        given: 'two pallets of two boxes each, with one item per box, in no particular order'
        Container pallet1 = buildPallet("Pallet 1", 1)
        Container pallet2 = buildPallet("Pallet 2", 2)
        List<ShipmentItem> shipmentItems = [
                buildPackedShipmentItem("P2-B2", buildBox(pallet2, "Box 2", 2)),
                buildPackedShipmentItem("P1-B2", buildBox(pallet1, "Box 2", 2)),
                buildPackedShipmentItem("P2-B1", buildBox(pallet2, "Box 1", 1)),
                buildPackedShipmentItem("P1-B1", buildBox(pallet1, "Box 1", 1)),
        ]

        when:
        List<ShipmentItem> sorted = service.sortByPackLevel(shipmentItems)

        then: 'they come out ordered by pack level 1, then by pack level 2'
        assert sorted*.lotNumber == ["P1-B1", "P1-B2", "P2-B1", "P2-B2"]
    }

    void 'sortByPackLevel should keep the order of the items packed at the same level'() {
        given: 'three items in the same box'
        Container box = buildBox(buildPallet("Pallet 1", 1), "Box 1", 1)
        List<ShipmentItem> shipmentItems = [
                buildPackedShipmentItem("third", box),
                buildPackedShipmentItem("first", box),
                buildPackedShipmentItem("second", box),
        ]

        when:
        List<ShipmentItem> sorted = service.sortByPackLevel(shipmentItems)

        then: 'the order the query returned them in is preserved'
        assert sorted*.lotNumber == ["third", "first", "second"]
    }

    void 'sortByPackLevel should keep the order of the items whose pack levels compare equal'() {
        given: 'two items in different pallets that carry no sort order of their own'
        List<ShipmentItem> shipmentItems = [
                buildPackedShipmentItem("second", buildPallet("Pallet B", null)),
                buildPackedShipmentItem("first", buildPallet("Pallet A", null)),
        ]

        when:
        List<ShipmentItem> sorted = service.sortByPackLevel(shipmentItems)

        then: 'nothing orders the pallets, so the order the query returned the items in stands'
        assert sorted*.lotNumber == ["second", "first"]
    }

    // ----------------------------------------------------------------------------------------------------------
    // Fixture helpers
    // ----------------------------------------------------------------------------------------------------------

    private static Container buildPallet(String name, Integer sortOrder) {
        return new Container(name: name, sortOrder: sortOrder)
    }

    private static Container buildBox(Container pallet, String name, Integer sortOrder) {
        return new Container(name: name, sortOrder: sortOrder, parentContainer: pallet)
    }

    /**
     * An item packed in the given container, labeled through its lot number so that the assertions can tell the
     * items apart: a shipment item compares by product and lot number, so asserting on the items themselves would
     * treat them all as equal.
     */
    private static ShipmentItem buildPackedShipmentItem(String label, Container container) {
        return new ShipmentItem(lotNumber: label, container: container)
    }

    private static ReceiptItemCompleteRequest completeRequest(ReceiptItem receiptItem, boolean cancelRemainingQuantity) {
        return new ReceiptItemCompleteRequest(receiptItem: receiptItem, cancelRemainingQuantity: cancelRemainingQuantity)
    }

    /**
     * A minimal shipment that satisfies the Shipment constraints, so it survives the explicit save (and the
     * beforeInsert/beforeUpdate hooks) that the service performs while recording the inbound transaction.
     */
    private static Shipment buildShipment(Inventory inventory = new Inventory(), boolean supportsPartialReceiving = true) {
        Shipment shipment = new Shipment(
                name: "Test shipment",
                origin: new Location(name: "Origin"),
                destination: buildDestination(supportsPartialReceiving, inventory),
                expectedShippingDate: new Date() - 7,
                shipmentType: new ShipmentType(name: "Default"),
        )
        // The event-based hooks and constraints assume an initialized collection (a TreeSet because the domain
        // declares the property as a SortedSet).
        shipment.events = new TreeSet()
        return shipment
    }

    /**
     * A saved shipment carrying the given items, forced into a receivable (shipped) status. The status is derived
     * from system events when the shipment is saved, so instead of building the whole event graph it is overridden
     * in memory afterwards - the receivability validation only ever reads the in-memory value.
     */
    private static Shipment buildReceivableShipment(List<ShipmentItem> shipmentItems) {
        Shipment shipment = buildShipment()
        shipmentItems.each { ShipmentItem shipmentItem -> shipment.addToShipmentItems(shipmentItem) }
        shipment.save(failOnError: true, flush: true)
        shipment.currentStatus = ShipmentStatusCode.SHIPPED
        return shipment
    }

    private static ShipmentItem buildShipmentItem(Integer quantity) {
        Product product = new Product(name: "Product")
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: "LOT-1")
        return new ShipmentItem(quantity: quantity, product: product, inventoryItem: inventoryItem)
    }

    private static ReceiptItem buildReceiptItem(ShipmentItem shipmentItem, Integer quantityReceived, Map args = [:]) {
        ReceiptItem receiptItem = new ReceiptItem(
                product: shipmentItem.product,
                inventoryItem: args.containsKey("inventoryItem") ? args.inventoryItem : shipmentItem.inventoryItem,
                lotNumber: args.lotNumber,
                expirationDate: args.expirationDate,
                // Original lines carry the shipment item's full quantity by default (like startReceipt creates
                // them). Tests can allocate a portion instead, or zero for split lines - hence containsKey
                // rather than an elvis, which would swallow the zero.
                quantityShipped: args.containsKey("quantityShipped") ? args.quantityShipped : shipmentItem.quantity,
                quantityReceived: quantityReceived,
                binLocation: args.binLocation,
                isSplitItem: args.isSplitItem ?: false,
        )
        shipmentItem.addToReceiptItems(receiptItem)
        return receiptItem
    }

    /**
     * The receiving location, configured with or without the partial receiving activity.
     */
    private static Location buildDestination(boolean supportsPartialReceiving, Inventory inventory = null) {
        return new Location(
                name: "Destination",
                inventory: inventory,
                supportedActivities: [supportsPartialReceiving
                        ? ActivityCode.PARTIAL_RECEIVING.id
                        : ActivityCode.RECEIVE_STOCK.id] as Set,
        )
    }

    /**
     * Builds an unsaved receipt for the tests that only exercise the in-memory object graph.
     */
    private static Receipt buildReceipt(List<ReceiptItem> receiptItems, ReceiptStatusCode statusCode) {
        Receipt receipt = new Receipt(receiptStatusCode: statusCode, actualDeliveryDate: new Date() - 1)
        receiptItems.each { ReceiptItem receiptItem -> receipt.addToReceiptItems(receiptItem) }
        return receipt
    }

    /**
     * An unsaved, already received receipt of an unsaved shipment carrying the only fact the cancel-remaining logic
     * reads off it: whether the destination supports partial receiving.
     */
    private static Receipt buildReceivedReceipt(List<ReceiptItem> receiptItems, boolean supportsPartialReceiving = true) {
        Receipt receipt = buildReceipt(receiptItems, ReceiptStatusCode.RECEIVED)
        receipt.shipment = new Shipment(destination: buildDestination(supportsPartialReceiving))
        return receipt
    }

    private static Receipt createReceipt(Shipment shipment, List<ReceiptItem> receiptItems,
                                         ReceiptStatusCode statusCode, Date dateDelivered = new Date() - 1) {
        Receipt receipt = buildReceipt(receiptItems, statusCode)
        receipt.actualDeliveryDate = dateDelivered
        // Link both sides (like the production startReceipt does) - reads such as the pending-receipt check and
        // the v2 marker lookup walk shipment.receipts, which a one-sided receipt.shipment assignment leaves empty.
        shipment.addToReceipts(receipt)
        receipt.save(failOnError: true, flush: true)
        return receipt
    }

    private static void markAsV2(Receipt receipt) {
        new ReceiptV2Marker(receipt: receipt).save(failOnError: true, flush: true)
    }
}
