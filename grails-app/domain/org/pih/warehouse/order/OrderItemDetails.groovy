package org.pih.warehouse.order

import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product

class OrderItemDetails implements Serializable {
    String id
    OrderItem orderItem
    Order order
    String orderNumber
    Product product
    Integer quantity
    String orderItemStatus
    UnitOfMeasure quantityUom
    BigDecimal quantityPerUom = 1
    BigDecimal unitPrice

    static mapping = {
        id generator: 'uuid'
        version false
        cache usage: "read-only"
    }

    static constraints = {
        id(nullable: true)
        order(nullable: true)
        orderNumber(nullable: true)
        product(nullable: true)
        quantity(nullable: true)
        orderItemStatus(nullable: true)
        quantityUom(nullable: true)
        quantityPerUom(nullable: true)
        unitPrice(nullable: true)
    }

    static transients = ['quantityOrdered', 'quantityShipped', 'quantityReceived', 'quantityInvoiced', 'derivedStatus']

    def getQuantityOrdered() {
        return quantity * (quantityPerUom as Integer) // * qtyPerUom for comparison with quantity shipped
    }

    def getQuantityShipped() {
        return orderItem.shipmentItems*.quantity?.sum() ?: 0
    }

    def getQuantityReceived() {
        return orderItem.shipmentItems*.receiptItems*.quantityReceived?.sum()?.sum() ?: 0
    }

    def getQuantityInvoiced() {
        return orderItem.invoiceItems*.quantity?.sum() ?: 0
    }

    OrderSummaryStatus getDerivedStatus() {
        if (quantityInvoiced >= quantityOrdered) {
            return OrderSummaryStatus.INVOICED
        } else if (quantityInvoiced > 0) {
            return OrderSummaryStatus.PARTIALLY_INVOICED
        } else if (quantityReceived >= quantityOrdered) {
            return OrderSummaryStatus.RECEIVED
        } else if (quantityReceived > 0) {
            return OrderSummaryStatus.PARTIALLY_INVOICED
        } else if (quantityShipped >= quantityOrdered) {
            return OrderSummaryStatus.SHIPPED
        } else if (quantityShipped > 0) {
            return OrderSummaryStatus.PARTIALLY_SHIPPED
        }

        return order?.status?.name() as OrderSummaryStatus ?: OrderSummaryStatus.PENDING
    }

    Map toJson() {
        return [
            id                      : id,
            "product.id"            : product?.id,
            "product.productCode"   : product?.productCode,
            "product.name"          : product?.name,
            orderNumber             : orderNumber,
            quantity                : quantity,
            orderItemStatus         : orderItemStatus,
            quantityUom             : quantityUom,
            quantityPerUom          : quantityPerUom,
            unitPrice               : unitPrice,
            quantityOrdered         : quantityOrdered,
            quantityShipped         : quantityShipped,
            quantityReceived        : quantityReceived,
            quantityInvoiced        : quantityInvoiced,
            derivedStatus           : derivedStatus
        ]
    }
}
