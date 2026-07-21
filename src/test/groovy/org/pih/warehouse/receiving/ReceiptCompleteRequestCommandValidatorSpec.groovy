package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.product.Product

@Unroll
class ReceiptCompleteRequestCommandValidatorSpec extends Specification implements DataTest {

    ReceiptCompleteRequestCommandValidator validator = new ReceiptCompleteRequestCommandValidator()

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, ReceiptV2Marker, Product)
    }

    void 'doValidate should reject a receipt that was not created by the v2 workflow'() {
        given: 'a pending receipt without the v2 marker'
        Receipt receipt = new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING, actualDeliveryDate: new Date())
        receipt.save(failOnError: true, flush: true)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [],
        ))

        then:
        !result.valid
        result.errors*.code == ["receiptCompleteRequestCommand.receipt.notV2"]
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
                        new ReceiptItemCompleteRequest(receiptItem: originalItem, cancelRemaining: true),
                        new ReceiptItemCompleteRequest(receiptItem: splitItem, cancelRemaining: true),
                ],
        ))

        then: 'only the split line is rejected'
        !result.valid
        result.errors*.code == ["receiptCompleteRequestCommand.itemsToComplete.cancelRemainingOnSplitItem"]
        result.errors.first().arguments.toString().contains(splitItem.id.toString())
        !result.errors.first().arguments.toString().contains(originalItem.id.toString())
    }

    void 'doValidate should accept the cancel-remaining flag on an original item (isSplitItem: #isSplitItem)'() {
        given:
        Receipt receipt = buildPendingReceipt()
        ReceiptItem originalItem = buildReceiptItem(receipt, isSplitItem)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptCompleteRequestCommand(
                receipt: receipt,
                itemsToComplete: [new ReceiptItemCompleteRequest(receiptItem: originalItem, cancelRemaining: true)],
        ))

        then:
        result.valid

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
                itemsToComplete: [new ReceiptItemCompleteRequest(receiptItem: splitItem, cancelRemaining: false)],
        ))

        then:
        result.valid
    }

    // ----------------------------------------------------------------------------------------------------------
    // Fixture helpers - the items are persisted so they carry distinct ids (transient items would all share a
    // null id and falsely trip the duplicate check).
    // ----------------------------------------------------------------------------------------------------------

    private static Receipt buildPendingReceipt() {
        Receipt receipt = new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING, actualDeliveryDate: new Date())
        receipt.save(failOnError: true, flush: true)
        // The completable receipts of these tests are v2 receipts, so stamp the marker startReceipt would create.
        new ReceiptV2Marker(receipt: receipt).save(failOnError: true, flush: true)
        return receipt
    }

    private static ReceiptItem buildReceiptItem(Receipt receipt, Boolean isSplitItem) {
        ReceiptItem receiptItem = new ReceiptItem(
                product: new Product(name: "Product"),
                quantityShipped: isSplitItem ? 0 : 100,
                isSplitItem: isSplitItem,
        )
        receipt.addToReceiptItems(receiptItem)
        receiptItem.save(failOnError: true, flush: true)
        return receiptItem
    }
}
