package org.pih.warehouse.receiving

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.validation.ObjectValidationResult
import org.pih.warehouse.product.Product

@Unroll
class ReceiptItemsBatchRequestValidatorSpec extends Specification implements DataTest {

    ReceiptItemsBatchRequestValidator validator = new ReceiptItemsBatchRequestValidator()

    void setupSpec() {
        mockDomains(Receipt, ReceiptItem, Product)
    }

    void 'doValidate should reject deleting an original item (isSplitItem: #isSplitItem)'() {
        given: 'an original line and a split line'
        Receipt receipt = buildPendingReceipt()
        ReceiptItem originalItem = buildReceiptItem(receipt, isSplitItem)
        ReceiptItem splitItem = buildReceiptItem(receipt, true)

        when: 'both are requested to be deleted'
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToDelete: [originalItem.id, splitItem.id],
        ))

        then: 'only the original line is rejected'
        assert !result.valid
        assert result.errors*.code == ["receiptItemsBatchRequest.itemsToDelete.originalItem"]
        assert result.errors.first().arguments.toString().contains(originalItem.id.toString())
        assert !result.errors.first().arguments.toString().contains(splitItem.id.toString())

        where: 'a missing flag (legacy data) counts as an original line'
        isSplitItem << [Boolean.FALSE, null]
    }

    void 'doValidate should accept deleting split items'() {
        given:
        Receipt receipt = buildPendingReceipt()
        ReceiptItem splitItem = buildReceiptItem(receipt, true)

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToDelete: [splitItem.id],
        ))

        then:
        assert result.valid
    }

    void 'doValidate should leave unknown item identifiers for the service to report'() {
        given:
        Receipt receipt = buildPendingReceipt()

        when:
        ObjectValidationResult result = validator.doValidate(new ReceiptItemsBatchRequest(
                receipt: receipt,
                itemsToDelete: ["unknown-id"],
        ))

        then:
        assert result.valid
    }

    // ----------------------------------------------------------------------------------------------------------
    // Fixture helpers - the items are persisted because the delete validation looks them up by id.
    // ----------------------------------------------------------------------------------------------------------

    private static Receipt buildPendingReceipt() {
        Receipt receipt = new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING, actualDeliveryDate: new Date())
        receipt.save(failOnError: true, flush: true)
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
