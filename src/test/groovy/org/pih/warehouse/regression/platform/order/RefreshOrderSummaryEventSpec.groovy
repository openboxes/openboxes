package org.pih.warehouse.regression.platform.order

import org.pih.warehouse.order.*
import spock.lang.Specification

import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment

/**
 * Order summaries are only refreshed for purchase orders, whatever changed.
 */
class RefreshOrderSummaryEventSpec extends Specification {

    Order purchaseOrder = order('po', OrderTypeCode.PURCHASE_ORDER)
    Order transferOrder = order('to', OrderTypeCode.TRANSFER_ORDER)

    private static Order order(String id, OrderTypeCode typeCode) {
        Order order = new Order(orderType: new OrderType(code: typeCode.name(), orderTypeCode: typeCode))
        order.id = id
        return order
    }

    void 'an order event refreshes purchase orders only'() {
        expect:
        new RefreshOrderSummaryEvent(purchaseOrder).orderIds == ['po']
        new RefreshOrderSummaryEvent(transferOrder).orderIds == []
        new RefreshOrderSummaryEvent(purchaseOrder, true).with { [orderIds, isDelete] } == [['po'], true]
        new RefreshOrderSummaryEvent([purchaseOrder, transferOrder]).orderIds == ['po']
        !new RefreshOrderSummaryEvent(purchaseOrder).disableRefresh
    }

    void 'an order item event refreshes its order and keeps the refresh flag'() {
        given:
        OrderItem item = new OrderItem(order: purchaseOrder, disableRefresh: true)

        expect:
        new RefreshOrderSummaryEvent(item).orderIds == ['po']
        new RefreshOrderSummaryEvent(item).disableRefresh
        new RefreshOrderSummaryEvent(new OrderItem(disableRefresh: false)).orderIds == []
    }

    void 'shipment and receipt events refresh the purchase orders that were shipped'() {
        given:
        Shipment shipment = Stub(Shipment) {
            getPurchaseOrders() >> [purchaseOrder]
            getDisableRefresh() >> false
        }
        Receipt receipt = new Receipt(shipment: shipment)
        receipt.disableRefresh = true

        expect:
        new RefreshOrderSummaryEvent(shipment).orderIds == ['po']
        new RefreshOrderSummaryEvent(receipt).orderIds == ['po']
        new RefreshOrderSummaryEvent(receipt).disableRefresh
    }

    void 'regular invoices refresh their purchase orders, prepayment invoices none'() {
        given:
        Invoice regular = Stub(Invoice) {
            getIsPrepaymentInvoice() >> false
            getOrders() >> [purchaseOrder, transferOrder]
            getDisableRefresh() >> true
        }
        Invoice prepayment = Stub(Invoice) {
            getIsPrepaymentInvoice() >> true
            getOrders() >> [purchaseOrder]
        }

        expect:
        new RefreshOrderSummaryEvent(regular).orderIds == ['po']
        new RefreshOrderSummaryEvent(regular).disableRefresh
        new RefreshOrderSummaryEvent(prepayment).orderIds == []
    }
}
