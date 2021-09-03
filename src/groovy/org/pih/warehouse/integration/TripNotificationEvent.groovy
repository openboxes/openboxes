package org.pih.warehouse.integration

import org.pih.warehouse.integration.xml.trip.Trip
import org.springframework.context.ApplicationEvent

class TripNotificationEvent extends ApplicationEvent {

    Trip trip

    TripNotificationEvent(Trip trip) {
        super(trip)
        this.trip = trip
    }

}
