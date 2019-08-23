package org.pih.warehouse.api

import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus

@Validateable
class Putaway {

    String id
    Location origin
    Location destination
    String putawayNumber
    Person putawayAssignee
    Date putawayDate
    Date dateCreated
    Person orderedBy
    String sortBy

    PutawayStatus putawayStatus
    List<PutawayItem> putawayItems = []

    static constrants = {
        origin(nullable: true)
        destination(nullable: true)
        putawayNumber(nullable: true)
        putawayStatus(nullable: true)
        putawayAssignee(nullable: true)
        putawayDate(nullable: true)
        putawayItems(nullable: true)
        dateCreated(nullable: true)
        orderedBy(nullable: true)
    }

    List<PutawayItem> getPutawayItems() {
        return putawayItems.sort { a, b ->
            (sortBy == "currentBins" ? a.currentBins?.toLowerCase() <=> b.currentBins?.toLowerCase() : 0) ?:
                    (sortBy == "preferredBin" ? a.preferredBin?.toLowerCase() <=> b.preferredBin?.toLowerCase() : 0) ?:
                            a.product?.category?.name <=> b.product?.category?.name ?:
                                    a.product?.name <=> b.product?.name ?:
                                            a.id <=> b.id
        }
    }

    Map toJson() {
        return [
                id                : id,
                putawayNumber     : putawayNumber,
                putawayStatus     : putawayStatus?.name(),
                putawayDate       : putawayDate?.format("MM/dd/yyyy"),
                dateCreated       : dateCreated?.format("MMMM dd, yyyy"),
                putawayAssignee   : putawayAssignee,
                "origin.id"       : origin?.id,
                "origin.name"     : origin?.name,
                "destination.id"  : destination?.id,
                "destination.name": destination?.name,
                putawayItems      : getPutawayItems().collect { it?.toJson() },
                orderedBy         : orderedBy?.name,
                sortBy            : sortBy
        ]
    }

    static Putaway createFromOrder(Order order) {
        Putaway putaway = new Putaway(
                id: order.id,
                origin: order.origin,
                destination: order.destination,
                putawayNumber: order.orderNumber,
                putawayStatus: Putaway.getPutawayStatus(order.status),
                putawayAssignee: order.completedBy,
                putawayDate: order.dateCompleted,
                dateCreated: order.dateOrdered,
                orderedBy: order.orderedBy
        )


        // Add all order items to putaway
        order.orderItems.each { orderItem ->
            if (!orderItem.parentOrderItem) {
                putaway.putawayItems.add(PutawayItem.createFromOrderItem(orderItem))
            }
        }

        return putaway
    }


    static PutawayStatus getPutawayStatus(OrderStatus orderStatus) {
        switch (orderStatus) {
            case OrderStatus.PENDING:
                return PutawayStatus.PENDING
            case OrderStatus.COMPLETED:
                return PutawayStatus.COMPLETED
            case OrderStatus.CANCELED:
                return PutawayStatus.CANCELED
            default:
                return null
        }

    }


}
