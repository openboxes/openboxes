package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryItemManager
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionSource
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType

/**
 * Runs against a minimal real domain graph so that the transaction (and the source stamped on it) actually persists.
 */
class ReceiptTransactionManagerSpec extends Specification implements DataTest {

    ReceiptTransactionManager receiptTransactionManager

    TransactionIdentifierService transactionIdentifierService
    InventoryItemManager inventoryItemManager

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, Shipment, ShipmentItem, ShipmentType, Transaction, TransactionEntry,
                TransactionSource, Product, InventoryItem, Inventory, Location, Requisition)
    }

    void setup() {
        transactionIdentifierService = Mock(TransactionIdentifierService) {
            generate(_) >> "TRX-001"
        }
        inventoryItemManager = Mock(InventoryItemManager)
        receiptTransactionManager = new ReceiptTransactionManager(transactionIdentifierService, inventoryItemManager)

        // TransactionType uses a uuid id generator, so a row with the fixed transfer-in id cannot be inserted into
        // the mocked datastore - intercept the static lookup instead.
        TransactionType transferInType = new TransactionType(name: "Transfer In", transactionCode: TransactionCode.CREDIT)
        transferInType.id = Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
        GroovySpy(TransactionType, global: true)
        TransactionType.get(_) >> transferInType
    }

    void 'createInboundTransaction should record an inbound transfer transaction crediting the received quantities'() {
        given:
        Shipment shipment = buildShipment()
        shipment.requisition = new Requisition(name: "REQ-1").save(validate: false, flush: true)
        ShipmentItem firstShipmentItem = buildShipmentItem(100)
        ReceiptItem firstItem = buildReceiptItem(firstShipmentItem, 70, [binLocation: new Location(name: "Bin")])
        ShipmentItem secondShipmentItem = buildShipmentItem(50)
        ReceiptItem secondItem = buildReceiptItem(secondShipmentItem, 30)
        Receipt receipt = createReceipt(shipment, [firstItem, secondItem], ReceiptStatusCode.RECEIVED)

        when:
        Transaction transaction = receiptTransactionManager.createInboundTransaction(receipt)

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

        and: 'the action that created it is recorded as a receipt against the two locations the stock moved between'
        assert TransactionSource.list() == [transaction.transactionSource]
        TransactionSource transactionSource = transaction.transactionSource
        assert transactionSource.transactionAction == TransactionAction.RECEIPT
        assert transactionSource.receipt == receipt
        assert transactionSource.shipment == shipment
        assert transactionSource.requisition == shipment.requisition
        assert transactionSource.origin == shipment.origin
        assert transactionSource.destination == shipment.destination

        and: 'each receipt item is credited with its received quantity'
        assert transaction.transactionEntries.size() == 2
        TransactionEntry firstEntry = transaction.transactionEntries.find { it.inventoryItem == firstItem.inventoryItem }
        assert firstEntry.quantity == 70
        assert firstEntry.binLocation == firstItem.binLocation
        TransactionEntry secondEntry = transaction.transactionEntries.find { it.inventoryItem == secondItem.inventoryItem }
        assert secondEntry.quantity == 30
    }

    void 'createInboundTransaction should not credit the lines that received nothing'() {
        given: 'a receipt whose second line was received against and whose other two lines were not'
        Shipment shipment = buildShipment()
        ShipmentItem emptyShipmentItem = buildShipmentItem(50)
        ReceiptItem emptyItem = buildReceiptItem(emptyShipmentItem, null)
        ShipmentItem receivedShipmentItem = buildShipmentItem(100)
        ReceiptItem receivedItem = buildReceiptItem(receivedShipmentItem, 70)
        ShipmentItem zeroShipmentItem = buildShipmentItem(20)
        ReceiptItem zeroItem = buildReceiptItem(zeroShipmentItem, 0)
        Receipt receipt = createReceipt(shipment, [emptyItem, receivedItem, zeroItem], ReceiptStatusCode.RECEIVED)

        when:
        Transaction transaction = receiptTransactionManager.createInboundTransaction(receipt)

        then: 'only the line that moved stock is credited - the other two get no entry of their own'
        assert transaction.transactionEntries.size() == 1
        TransactionEntry entry = transaction.transactionEntries.first()
        assert entry.inventoryItem == receivedItem.inventoryItem
        assert entry.quantity == 70

        and: 'no inventory item is resolved for the lines that were left out'
        0 * inventoryItemManager.getOrCreateInventoryItem(_, _, _)
    }

    void 'deleteTransactionSourcesForReceipts should clear the transaction that the source it deletes stamps'() {
        given: 'a completed receipt with its inbound transaction and the source recorded for it'
        Shipment shipment = buildShipment()
        ShipmentItem shipmentItem = buildShipmentItem(100)
        ReceiptItem receiptItem = buildReceiptItem(shipmentItem, 100)
        Receipt receipt = createReceipt(shipment, [receiptItem], ReceiptStatusCode.RECEIVED)
        transactionIdentifierService.generate(_ as Transaction) >> "TRX-003"
        Transaction transaction = receiptTransactionManager.createInboundTransaction(receipt)
        TransactionSource transactionSource = transaction.transactionSource

        when:
        receiptTransactionManager.deleteTransactionSourcesForReceipts([receipt])

        then: 'the transaction lets go of the source before it is deleted - its foreign key would otherwise block it'
        assert transactionSource != null
        assert transaction.transactionSource == null
        assert TransactionSource.count() == 0
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
        Transaction transaction = receiptTransactionManager.createInboundTransaction(receipt)

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
        receiptTransactionManager.createInboundTransaction(receipt)

        then:
        thrown(IllegalStateException)
    }

    // ----------------------------------------------------------------------------------------------------------
    // Fixture helpers
    // ----------------------------------------------------------------------------------------------------------

    /**
     * A minimal shipment that satisfies the Shipment constraints, so it survives the explicit save (and the
     * beforeInsert/beforeUpdate hooks) that the manager performs while recording the inbound transaction.
     */
    private static Shipment buildShipment(Inventory inventory = new Inventory()) {
        Shipment shipment = new Shipment(
                name: "Test shipment",
                origin: new Location(name: "Origin"),
                destination: buildDestination(inventory),
                expectedShippingDate: new Date() - 7,
                shipmentType: new ShipmentType(name: "Default"),
        )
        // The event-based hooks and constraints assume an initialized collection (a TreeSet because the domain
        // declares the property as a SortedSet).
        shipment.events = new TreeSet()
        return shipment
    }

    /**
     * The receiving location, which holds the inventory the received quantities are credited to.
     */
    private static Location buildDestination(Inventory inventory) {
        return new Location(
                name: "Destination",
                inventory: inventory,
                supportedActivities: [ActivityCode.PARTIAL_RECEIVING.id] as Set,
        )
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
                quantityShipped: shipmentItem.quantity,
                quantityReceived: quantityReceived,
                binLocation: args.binLocation,
                isSplitItem: false,
        )
        shipmentItem.addToReceiptItems(receiptItem)
        return receiptItem
    }

    private static Receipt createReceipt(Shipment shipment, List<ReceiptItem> receiptItems,
                                         ReceiptStatusCode statusCode, Date dateDelivered = new Date() - 1) {
        Receipt receipt = new Receipt(receiptStatusCode: statusCode, actualDeliveryDate: dateDelivered)
        receiptItems.each { ReceiptItem receiptItem -> receipt.addToReceiptItems(receiptItem) }
        // Link both sides (like the production startReceipt does) - the transaction is recorded off the receipt's
        // shipment, which a one-sided receipt.shipment assignment would leave without its receipt.
        shipment.addToReceipts(receipt)
        receipt.save(failOnError: true, flush: true)
        return receipt
    }
}
