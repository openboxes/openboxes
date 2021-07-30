package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus

@Validateable
class StockTransfer {

    String id
    Location origin
    Location destination
    String stockTransferNumber
    Date dateCreated
    Person orderedBy

    StockTransferStatus status = StockTransferStatus.PENDING
    List<StockTransferItem> stockTransferItems = []

    static constrants = {
        origin(nullable: true)
        destination(nullable: true)
        stockTransferNumber(nullable: true)
        status(nullable: true)
        stockTransferItems(nullable: true)
        dateCreated(nullable: true)
        orderedBy(nullable: true)
    }

    static StockTransfer createFromOrder(Order order) {
        StockTransfer stockTransfer = new StockTransfer(
                id: order.id,
                origin: order.origin,
                destination: order.destination,
                stockTransferNumber: order.orderNumber,
                status: getStatus(order.status),
                dateCreated: order.dateOrdered,
                orderedBy: order.orderedBy
        )

        // Add all order items to stock transfer
        order.orderItems.each { orderItem ->
            if (!orderItem.parentOrderItem) {
                stockTransfer.stockTransferItems.add(StockTransferItem.createFromOrderItem(orderItem))
            }
        }

        return stockTransfer
    }

    static StockTransferStatus getStatus(OrderStatus orderStatus) {
        StockTransferStatus stockTransferStatus = StockTransferStatus.valueOf(orderStatus.name())
        return stockTransferStatus ?: StockTransferStatus.PENDING
    }

    Map toJson() {
        return [
                id                 : id,
                stockTransferNumber: stockTransferNumber,
                status             : status?.name(),
                dateCreated        : dateCreated?.format("MMMM dd, yyyy"),
                "origin.id"        : origin?.id,
                "origin.name"      : origin?.name,
                "destination.id"   : destination?.id,
                "destination.name" : destination?.name,
                stockTransferItems : stockTransferItems.collect { it?.toJson() },
                orderedBy          : orderedBy?.name
        ]
    }
}
