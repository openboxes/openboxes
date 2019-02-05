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

    PutawayStatus putawayStatus
    List<PutawayItem> putawayItems = []
    //LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(PutawayItem.class));

    static constrants = {
        origin(nullable:true)
        destination(nullable:true)
        putawayNumber(nullable:true)
        putawayStatus(nullable:true)
        putawayAssignee(nullable:true)
        putawayDate(nullable:true)
        putawayItems(nullable:true)
        dateCreated(nullable:true)
    }

    Map toJson() {
        return [
                id: id,
                putawayNumber: putawayNumber,
                putawayStatus: putawayStatus?.name(),
                putawayDate: putawayDate?.format("MM/dd/yyyy"),
                dateCreated: dateCreated?.format("MMMM dd, yyyy"),
                putawayAssignee: putawayAssignee,
                "origin.id": origin?.id,
                "origin.name": origin?.name,
                "destination.id": destination?.id,
                "destination.name": destination?.name,
                putawayItems: putawayItems.collect { it?.toJson() }
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
                dateCreated: order.dateOrdered
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
        switch(orderStatus) {
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
