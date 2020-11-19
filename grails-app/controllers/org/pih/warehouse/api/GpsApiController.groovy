/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.requisition.Requisition

class GpsApiController {

    def stockMovementService

    def index = {
        log.info "webhook: " + params

        def identifier = params.device.attributes["stockMovementNumber"]
        def latitude = params.position.latitude
        def longitude = params.position.longitude

        // Find the stock movement associate with the identifier
        Requisition requisition = Requisition.findByRequestNumber(identifier)
        StockMovement stockMovement = stockMovementService.getStockMovement(requisition.id)

        // Assumes you have a location type called GPS Location
        // FIXME Location should have explicit lat/long properties but we're just storing both in
        // the location.name field to make this prototype simple. We'd actually want to switch
        // from using events (which are closer to status changes) to something like a shipment
        // route table.
        Location eventLocation = new Location()
        eventLocation.name = "${longitude}:${latitude}"
        eventLocation.locationType = LocationType.findByName("GPS Location")

        // Assume you've created an Event Type with eventCode = IN_TRANSIT
        Event event = new Event()
        event.eventType = EventType.findByEventCode(EventCode.IN_TRANSIT)
        event.eventDate = new Date()
        event.eventLocation = eventLocation

        requisition.shipment.addToEvents(event)
        requisition.shipment.currentEvent = event
        requisition.shipment.save(flush:true)

        response.status = 201
        render(event as JSON)
    }

    def route = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        // Hacky
        def coordinates =
                stockMovement.shipment.events.findAll { Event event ->
                    event.eventLocation.locationType.name == "GPS Location"
                }.collect {
                    Event event -> event?.eventLocation?.name?.split(":")
                }

        def data = [type: "Feature", geometry: [type: "LineString", coordinates: coordinates]]
        render(data as JSON)
    }

}
