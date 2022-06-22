package org.pih.warehouse.order

import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class OrderItemSummary implements Serializable {
    String id
    Order order
    String orderNumber
    Product product
    Integer quantity
    String orderItemStatus
    UnitOfMeasure quantityUom
    BigDecimal quantityPerUom = 1
    BigDecimal unitPrice

    Integer quantityOrdered
    Integer quantityShipped
    Integer quantityReceived
    Integer quantityInvoiced
    String derivedStatus

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
        quantityOrdered(nullable: true)
        quantityShipped(nullable: true)
        quantityReceived(nullable: true)
        quantityInvoiced(nullable: true)
        derivedStatus(nullable: true)
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
