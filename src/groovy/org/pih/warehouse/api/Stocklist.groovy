package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.requisition.Requisition

class Stocklist {

    Requisition requisition

    Location destination
    Location origin
    Person requestedBy
    String name

    static Stocklist createFromRequisition(Requisition requisition) {
        return new Stocklist(requisition: requisition, requestedBy: requisition.requestedBy, destination: requisition.destination, origin: requisition.origin)
    }

    Map toJson() {
        return [
                "requisition.id"  : requisition?.id,
                name              : requisition?.name,
                "destination.id"  : destination?.id,
                "destination.name": destination?.name,
                "origin.id"       : origin?.id,
                "origin.name"     : origin?.name,
                "requestedBy.id"  : requestedBy?.id,
                "requestedBy.name": requestedBy?.name
        ]
    }
}
