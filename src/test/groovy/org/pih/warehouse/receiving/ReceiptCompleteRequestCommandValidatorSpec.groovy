package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment

@Unroll
class ReceiptCompleteRequestCommandValidatorSpec extends Specification implements DataTest {

    ReceiptCompleteRequestCommandValidator validator = new ReceiptCompleteRequestCommandValidator()

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, Product, Shipment, Location)

        // ReceiptItemCompleteRequest implements ObjectValidatable, whose validate() performs javax validation
        // through the "defaultValidator" bean. A running app gets that bean from Boot's autoconfiguration, but
        // the DataTest context does not register it, so items could not be validated here without this.
        defineBeans {
            defaultValidator(LocalValidatorFactoryBean)
        }
    }

    void 'doValidate should accept a receipt that was started by the old receiving workflow'() {
        given: 'a pending receipt carrying no v2 marker'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [],
        ))

        then: 'the workflow the receipt was started by does not gate its completion'
        assert result.valid
    }

    void 'doValidate should reject a receipt that received nothing (quantityReceived: #quantityReceived)'() {
        given: 'a pending receipt whose only line received nothing'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false, quantityReceived)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'completing it would record an inbound transaction with no entries at all, so it is rejected'
        assert !result.valid
        assert result.errors*.code == ["receiptCompleteRequestCommand.receipt.nothingReceived"]

        where: 'the line was either never given a quantity, or deliberately given a zero'
        quantityReceived << [null, 0]
    }

    void 'doValidate should reject a receipt that carries no lines at all'() {
        given:
        Receipt receipt = buildPendingReceipt()

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then:
        assert !result.valid
        assert result.errors*.code == ["receiptCompleteRequestCommand.receipt.nothingReceived"]
    }

    void 'doValidate should accept a receipt where a single line received something'() {
        given: 'a pending receipt whose lines received nothing, except for one'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false, 0)
        buildReceiptItem(receipt, true, null)
        buildReceiptItem(receipt, true, 5)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'the one line that moved stock is enough - the transaction gets its entry'
        assert result.valid
    }

    void 'doValidate should reject the cancel-remaining flag on a split item'() {
        given: 'a pending receipt with an original line and a split line'
        Receipt receipt = buildPendingReceipt()
        ReceiptItem originalItem = buildReceiptItem(receipt, false)
        ReceiptItem splitItem = buildReceiptItem(receipt, true)

        when: 'both lines are flagged'
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [
                        new ReceiptItemCompleteRequest(receiptItem: originalItem, cancelRemainingQuantity: true),
                        new ReceiptItemCompleteRequest(receiptItem: splitItem, cancelRemainingQuantity: true),
                ],
        ))

        then: 'only the split line is rejected'
        assert !result.valid
        assert result.errors*.code == ["receiptCompleteRequestCommand.itemsToComplete.cancelRemainingOnSplitItem"]
        assert result.errors.first().arguments.toString().contains(splitItem.id.toString())
        assert !result.errors.first().arguments.toString().contains(originalItem.id.toString())
    }

    void 'doValidate should accept the cancel-remaining flag on an original item (isSplitItem: #isSplitItem)'() {
        given:
        Receipt receipt = buildPendingReceipt()
        ReceiptItem originalItem = buildReceiptItem(receipt, isSplitItem)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [new ReceiptItemCompleteRequest(receiptItem: originalItem, cancelRemainingQuantity: true)],
        ))

        then:
        assert result.valid

        where: 'a missing flag (legacy data) counts as an original line'
        isSplitItem << [Boolean.FALSE, null]
    }

    void 'doValidate should accept a split item that does not flag cancel-remaining'() {
        given:
        Receipt receipt = buildPendingReceipt()
        ReceiptItem splitItem = buildReceiptItem(receipt, true)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [new ReceiptItemCompleteRequest(receiptItem: splitItem, cancelRemainingQuantity: false)],
        ))

        then:
        assert result.valid
    }

    void 'doValidate should reject completing a line that received something into no bin at a location supporting #supportedActivity'() {
        given: 'a pending receipt whose lines received something, one of them into no bin'
        Receipt receipt = buildPendingReceipt()
        ReceiptItem itemWithBin = buildReceiptItem(receipt, false, 10, new Location(name: "Bin"))
        ReceiptItem itemWithoutBin = buildReceiptItem(receipt, true, 5)
        receiveInto(receipt, supportedActivity)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'only the line that would receive its stock outside of any bin is rejected'
        assert !result.valid
        assert result.errors*.code == ["receiptCompleteRequestCommand.receipt.binLocationMissing"]
        assert result.errors.first().arguments.toString().contains(itemWithoutBin.id.toString())
        assert !result.errors.first().arguments.toString().contains(itemWithBin.id.toString())

        where: 'either activity on its own makes the location track bin locations'
        supportedActivity << [ActivityCode.PICK_STOCK, ActivityCode.PUTAWAY_STOCK]
    }

    void 'doValidate should accept completing a receipt whose binless line received nothing (quantityReceived: #quantityReceived)'() {
        given: 'a pending receipt where the only line without a bin received nothing'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false, 10, new Location(name: "Bin"))
        buildReceiptItem(receipt, true, quantityReceived)
        receiveInto(receipt, ActivityCode.PUTAWAY_STOCK)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'that line moves no stock, so it needs no bin to move it into'
        assert result.valid

        where: 'the line was either never given a quantity, or deliberately given a zero'
        quantityReceived << [null, 0]
    }

    void 'doValidate should accept completing without bin locations at a location that does not track bin locations'() {
        given:
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false, 10)
        receiveInto(receipt, ActivityCode.RECEIVE_STOCK)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then: 'the location holds no bins at all'
        assert result.valid
    }

    void 'doValidate should reject completing lines that receive into an inactive bin'() {
        given:
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false, 10,
                new Location(locationNumber: "ACTIVE-BIN", active: true))
        ReceiptItem itemInInactiveBin1 = buildReceiptItem(receipt, true, 5,
                new Location(locationNumber: "INACTIVE-BIN-1", active: false))
        ReceiptItem itemInInactiveBin2 = buildReceiptItem(receipt, true, 5,
                new Location(locationNumber: "INACTIVE-BIN-2", active: false))

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then:
        assert !result.valid
        assert result.errors*.code == [
                "receiptCompleteRequestCommand.receipt.binLocationInactive",
                "receiptCompleteRequestCommand.receipt.binLocationInactive",
        ]
        assert result.errors*.arguments*.toList() as Set == [
                ["INACTIVE-BIN-1", itemInInactiveBin1.id],
                ["INACTIVE-BIN-2", itemInInactiveBin2.id],
        ] as Set
    }

    void 'doValidate should accept completing lines that receive into active bins'() {
        given:
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, false, 10, new Location(locationNumber: "BIN-1", active: true))

        expect:
        assert validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt)).valid
    }

    void 'doValidate should reject a split item with lot: #lotNumber and expiry: #expirationDate when lotAndExpiryControl is enabled'() {
        given: 'a pending receipt for a split item on a product with lotAndExpiryControl enabled'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, true, 5, null, true, lotNumber, expirationDate)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt))

        then:
        assert !result.valid
        assert result.errors*.code == ["receiptCompleteRequestCommand.receipt.missingLotAndExpiry"]
        assert result.errors.first().arguments.toString().contains("Product")
        assert !result.errors.first().arguments.toString().contains("Code")

        where:
        lotNumber | expirationDate
        null      | null
        "valid"   | null
        null      | new Date(2025, 01, 01)
    }

    void 'doValidate should accept a split item with a valid lot and expiry when lotAndExpiryControl is enabled'() {
        given: 'a pending receipt for a split item on a product with lotAndExpiryControl enabled and both fields set'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, true, 5, null, true, "valid", new Date(2025, 01, 01))

        expect:
        assert validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt)).valid
    }

    void 'doValidate should accept an item with isSplitItem: #splitItem, lot: #lotNumber, expiry: #expirationDate, and quantity: #quantity when lotAndExpiryControl is #lotAndExpiryControl'() {
        given: 'a pending receipt for a product with lotAndExpiryControl disabled'
        Receipt receipt = buildPendingReceipt()
        buildReceiptItem(receipt, splitItem, quantity, null, lotAndExpiryControl, lotNumber, expirationDate)

        and: 'another receipt item that makes the receipt valid even if the other item receives nothing'
        buildReceiptItem(receipt, false)

        expect:
        assert validator.doValidate(new ReceiptCompleteRequestCommand(receipt: receipt)).valid

        where:
        splitItem  | lotAndExpiryControl | quantity | lotNumber | expirationDate
        // Split item and invalid but lotAndExpiryControl is disabled
        true       | false               | 5        | null      | null
        true       | false               | 5        | "valid"   | null
        true       | false               | 5        | null      | new Date(2025, 01, 01)
        // Not a split item and invalid but receiving no quantity so still succeeds even if lotAndExpiryControl is enabled
        false      | true                | 0        | null      | null
        false      | true                | 0        | "valid"   | null
        false      | true                | 0        | null      | new Date(2025, 01, 01)
    }

    // ----------------------------------------------------------------------------------------------------------
    // Fixture helpers - the items are persisted so they carry distinct ids (transient items would all share a
    // null id and falsely trip the duplicate check).
    // ----------------------------------------------------------------------------------------------------------

    private static Receipt buildPendingReceipt() {
        Receipt receipt = new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING, actualDeliveryDate: new Date())
        receipt.save(failOnError: true, flush: true)
        return receipt
    }

    private static ReceiptItem buildReceiptItem(
            Receipt receipt,
            Boolean isSplitItem,
            Integer quantityReceived = 10,
            Location binLocation = null,
            boolean lotAndExpiryControl = false,
            String lotNumber = null,
            Date expirationDate = null
    ) {
        InventoryItem inventoryItem = (lotNumber && expirationDate) ? new InventoryItem(
                lotNumber: lotNumber,
                expirationDate: expirationDate,
        ) : null

        ReceiptItem receiptItem = new ReceiptItem(
                product: new Product(name: "Product", code: "Code", lotAndExpiryControl: lotAndExpiryControl),
                inventoryItem: inventoryItem,
                quantityShipped: isSplitItem ? 0 : 100,
                quantityReceived: quantityReceived,
                isSplitItem: isSplitItem,
                binLocation: binLocation,
        )
        receipt.addToReceiptItems(receiptItem)
        receiptItem.save(failOnError: true, flush: true)
        return receiptItem
    }

    /**
     * Puts the receipt on a shipment received into a location supporting the given activity, which is what decides
     * whether that location tracks bin locations (Location.hasBinLocationSupport). The shipment is attached after the
     * items are persisted so that it stays out of the datastore.
     */
    private static void receiveInto(Receipt receipt, ActivityCode supportedActivity) {
        receipt.shipment = new Shipment(destination: new Location(
                name: "Destination",
                supportedActivities: [supportedActivity.id] as Set,
        ))
    }
}
