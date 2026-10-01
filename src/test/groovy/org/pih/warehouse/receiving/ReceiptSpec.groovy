package org.pih.warehouse.receiving

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.shipping.ShipmentItem

class ReceiptSpec extends Specification implements DomainUnitTest<Receipt> {

    void 'getReceiptItemsWithQuantityReceivedOrCanceled should skip the empty items'() {
        given:
        ReceiptItem receivedItem = new ReceiptItem(quantityReceived: 5)
        ReceiptItem canceledItem = new ReceiptItem(quantityReceived: 0, quantityCanceled: 3)
        ReceiptItem zeroedItem = new ReceiptItem(quantityReceived: 0, quantityCanceled: 0)
        ReceiptItem untouchedItem = new ReceiptItem()
        Receipt receipt = buildReceipt([receivedItem, canceledItem, zeroedItem, untouchedItem])

        expect:
        receipt.receiptItemsWithQuantityReceivedOrCanceled == [receivedItem, canceledItem] as Set
    }

    void 'sortReceiptItemsWithQuantityReceivedOrCanceledBySortOrder should skip the empty items'() {
        given:
        ReceiptItem secondItem = new ReceiptItem(quantityReceived: 5, shipmentItem: new ShipmentItem(sortOrder: 2))
        ReceiptItem untouchedItem = new ReceiptItem(shipmentItem: new ShipmentItem(sortOrder: 0))
        ReceiptItem firstItem = new ReceiptItem(quantityCanceled: 3, shipmentItem: new ShipmentItem(sortOrder: 1))
        Receipt receipt = buildReceipt([secondItem, untouchedItem, firstItem])

        expect:
        receipt.sortReceiptItemsWithQuantityReceivedOrCanceledBySortOrder() == [firstItem, secondItem]
        receipt.sortReceiptItemsBySortOrder() == [untouchedItem, firstItem, secondItem]
    }

    private static Receipt buildReceipt(List<ReceiptItem> receiptItems) {
        Receipt receipt = new Receipt()
        receiptItems.each { ReceiptItem receiptItem -> receipt.addToReceiptItems(receiptItem) }
        return receipt
    }
}
