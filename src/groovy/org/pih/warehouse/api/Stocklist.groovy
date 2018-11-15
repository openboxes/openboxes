package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.requisition.Requisition

class Stocklist {

    Requisition requisition

    Location location
    Person manager

    String name
    Integer monthlyDemand
    Integer replenishmentPeriod
    Integer maximumQuantity
    String uof

    static Stocklist createFromRequisition(Requisition requisition) {
        return new Stocklist(requisition: requisition, manager: requisition.requestedBy, location: requisition.destination)
    }

    Map toJson() {
        return [
                "requisition.id": requisition?.id,
                name: requisition?.name,
                "location.id": location?.id,
                "location.name": location?.name,
                "locationGroup.id": location?.locationGroup?.id,
                "locationGroup.name": location?.locationGroup?.name,
                monthlyDemand: monthlyDemand,
                "manager.id": manager?.id,
                "manager.name": manager?.name,
                replenishmentPeriod: replenishmentPeriod,
                maximumQuantity: maximumQuantity,
                uof: uof
        ]
    }
}
