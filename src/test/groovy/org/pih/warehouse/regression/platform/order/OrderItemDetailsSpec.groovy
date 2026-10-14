package org.pih.warehouse.regression.platform.order

import spock.lang.PendingFeature

import org.pih.warehouse.order.*
import spock.lang.Specification

import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.ShipmentItem

class OrderItemDetailsSpec extends Specification {

    private static OrderItemDetails details(Map args) {
        List<ShipmentItem> shipped = (args.shipped ?: []).collect { quantity, received ->
            new ShipmentItem(quantity: quantity,
                    receiptItems: received.collect { new ReceiptItem(quantityReceived: it) } as Set)
        }
        OrderItem orderItem = new OrderItem(
                shipmentItems: shipped as Set,
                invoiceItems: (args.invoiced ?: []).collect { new InvoiceItem(quantity: it) } as Set)
        return new OrderItemDetails(orderItem: orderItem, quantity: args.quantity ?: 10,
                quantityPerUom: args.quantityPerUom ?: 1, order: args.order)
    }

    void 'ordered quantity is in each units'() {
        expect:
        details(quantity: 4, quantityPerUom: 12).quantityOrdered == 48
    }

    void 'shipped, received and invoiced quantities add up the related items'() {
        given:
        OrderItemDetails item = details(quantity: 10, shipped: [[4, [3, 1]], [5, [2]]], invoiced: [2, 1])

        expect:
        item.quantityShipped == 9
        item.quantityReceived == 6
        item.quantityInvoiced == 3
    }

    void 'nothing shipped, received or invoiced counts as zero'() {
        given:
        OrderItemDetails item = details(quantity: 10)

        expect:
        item.quantityShipped == 0
        item.quantityReceived == 0
        item.quantityInvoiced == 0
    }

    @PendingFeature(reason = 'OBAF returns PARTIALLY_INVOICED for a partially received line')
    void 'the derived status is #expected for shipped #shipped and invoiced #invoiced'() {
        expect:
        details(quantity: 10, shipped: shipped, invoiced: invoiced).derivedStatus == expected

        where:
        shipped               | invoiced || expected
        [[10, [10]]]          | [10]     || OrderSummaryStatus.INVOICED
        [[10, [10]]]          | [4]      || OrderSummaryStatus.PARTIALLY_INVOICED
        [[10, [10]]]          | []       || OrderSummaryStatus.RECEIVED
        [[10, [4]]]           | []       || OrderSummaryStatus.PARTIALLY_RECEIVED
        [[10, []]]            | []       || OrderSummaryStatus.SHIPPED
        [[3, []]]             | []       || OrderSummaryStatus.PARTIALLY_SHIPPED
    }

    void 'without any activity the derived status follows the order status'() {
        expect:
        details(quantity: 10, order: new Order(status: OrderStatus.PLACED)).derivedStatus == OrderSummaryStatus.PLACED
        details(quantity: 10).derivedStatus == OrderSummaryStatus.PENDING
    }

    void 'the json view lists the product and the derived quantities'() {
        given:
        OrderItemDetails item = details(quantity: 2, shipped: [[2, []]])
        item.product = new Product(productCode: 'P1', name: 'Gloves')
        item.orderNumber = 'PO-1'
        item.unitPrice = 1.5

        when:
        Map json = item.toJson()

        then:
        json['product.productCode'] == 'P1'
        json['product.name'] == 'Gloves'
        json.orderNumber == 'PO-1'
        json.unitPrice == 1.5
        json.quantityOrdered == 2
        json.quantityShipped == 2
        json.quantityReceived == 0
        json.quantityInvoiced == 0
        json.derivedStatus == OrderSummaryStatus.SHIPPED
    }
}
