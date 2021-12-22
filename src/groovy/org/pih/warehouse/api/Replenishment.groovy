package org.pih.warehouse.api

import com.google.common.base.Enums
import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus

@Validateable
class Replenishment {

    String id
    Location origin
    Location destination
    String replenishmentNumber
    Date dateCreated
    Person orderedBy

    ReplenishmentStatus status = ReplenishmentStatus.PENDING
    List<ReplenishmentItem> replenishmentItems = []

    static constrants = {
        origin(nullable: true)
        destination(nullable: true)
        replenishmentNumber(nullable: true)
        status(nullable: true)
        replenishmentItems(nullable: true)
        dateCreated(nullable: true)
        orderedBy(nullable: true)
    }

    static Replenishment createFromOrder(Order order) {
        Replenishment replenishment = new Replenishment(
                id: order.id,
                origin: order.origin,
                destination: order.destination,
                replenishmentNumber: order.orderNumber,
                status: getStatus(order.status),
                dateCreated: order.dateOrdered,
                orderedBy: order.orderedBy
        )

        // Add all order items to replenishment
        order.orderItems.each { orderItem ->
            replenishment.replenishmentItems.add(ReplenishmentItem.createFromOrderItem(orderItem))
        }

        return replenishment
    }

    static ReplenishmentStatus getStatus(OrderStatus orderStatus) {
        ReplenishmentStatus replenishmentStatus = Enums.getIfPresent(ReplenishmentStatus, orderStatus.name()).orNull()
        return replenishmentStatus ?: ReplenishmentStatus.PENDING
    }

    Map toJson() {
        return [
                id                 : id,
                replenishmentNumber: replenishmentNumber,
                status             : status?.name(),
                dateCreated        : dateCreated?.format("MMMM dd, yyyy"),
                "origin.id"        : origin?.id,
                "origin.name"      : origin?.name,
                "destination.id"   : destination?.id,
                "destination.name" : destination?.name,
                replenishmentItems : replenishmentItems.sort { a, b ->
                    a.product?.productCode <=> b.product?.productCode ?:
                        a.inventoryItem?.lotNumber <=> b.inventoryItem?.lotNumber ?:
                            a.originBinLocation?.zone?.name <=> b.originBinLocation?.zone?.name ?:
                                a.originBinLocation?.name <=> b.originBinLocation?.name
                }.collect { it?.toJson() },
                orderedBy          : orderedBy?.name
        ]
    }
}
