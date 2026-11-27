package org.pih.warehouse.api

import grails.validation.Validateable
import org.hibernate.Hibernate
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.requisition.Requisition

class Stocklist implements Validateable {

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
                // A lazy Person association is a Hibernate proxy of Person even when the row is a User
                "requestedBy.id"  : Hibernate.unproxy(requestedBy)?.id,
                "requestedBy.name": requestedBy?.name
        ]
    }
}
