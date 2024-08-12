package org.pih.warehouse.outbound

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

class FulfillmentRequest implements Validateable {
    String description

    Location origin

    Location destination

    Person requestedBy

    Date dateRequested

    static constraints = {
        description(nullable: true)
        origin(validator: { Location origin, FulfillmentRequest obj ->
            if (origin?.id == obj.destination?.id) {
                return ['sameAsDestination']
            }
            return true
        })
        destination(validator: { Location destination, FulfillmentRequest obj ->
            if (destination?.id == obj.origin?.id) {
                return ['sameAsOrigin']
            }
            return true
        })
    }
}
