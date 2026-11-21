package org.pih.warehouse.shipping

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptStatusCode

class ShipmentSpec extends Specification implements DomainUnitTest<Shipment> {

    void 'getReceivedReceipts should return an empty list when the shipment has no receipts'() {
        given:
        Shipment shipment = new Shipment()

        expect:
        assert shipment.receivedReceipts == []
    }

    void 'getReceivedReceipts should skip pending receipts'() {
        given:
        Receipt pendingReceipt = new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING)
        Shipment shipment = new Shipment(receipts: new TreeSet([pendingReceipt]))

        expect:
        assert shipment.receivedReceipts == []
    }

    void 'getReceivedReceipts should return only received receipts in their sorted order'() {
        given:
        Date now = new Date()
        Receipt firstReceived = buildReceipt(ReceiptStatusCode.RECEIVED, now - 2)
        Receipt secondReceived = buildReceipt(ReceiptStatusCode.RECEIVED, now - 1)
        Receipt pendingReceipt = buildReceipt(ReceiptStatusCode.PENDING, now)
        Shipment shipment = new Shipment(receipts: new TreeSet([pendingReceipt, secondReceived, firstReceived]))

        when:
        List<Receipt> receivedReceipts = shipment.receivedReceipts

        then:
        assert receivedReceipts == [firstReceived, secondReceived]
    }

    private static Receipt buildReceipt(ReceiptStatusCode receiptStatusCode, Date dateCreated) {
        Receipt receipt = new Receipt(receiptStatusCode: receiptStatusCode)
        receipt.dateCreated = dateCreated
        return receipt
    }
}
