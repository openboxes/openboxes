/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.integration

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventTypeCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.integration.xml.execution.ExecutionStatus
import org.springframework.context.ApplicationListener

import java.text.SimpleDateFormat

class TripExecutionEventService implements ApplicationListener<TripExecutionEvent>  {

    boolean transactional = true

    def stockMovementService
    def notificationService
    def grailsApplication

    void onApplicationEvent(TripExecutionEvent tripExecutionEvent) {
        log.info "Trip execution " + tripExecutionEvent.execution.toString()
        SimpleDateFormat dateFormatter = new SimpleDateFormat(grailsApplication.config.openboxes.integration.defaultDateFormat)
        tripExecutionEvent.execution.executionStatus.each { ExecutionStatus executionStatus ->

            // Locate stock movement by tracking number
            String trackingNumber = executionStatus.orderId
            StockMovement stockMovement = stockMovementService.findByTrackingNumber(trackingNumber)
            if (!stockMovement) {
                throw new IllegalArgumentException("Unable to locate stock movement by tracking number ${trackingNumber}")
            }

            // Identify event type associated with status
            String statusCode = executionStatus.status
            EventType eventType = EventType.findByCode(statusCode)
            if (!eventType) {
                throw new IllegalArgumentException("Status code ${statusCode} not associated with an event type")
            }

            // Validate status update
            Shipment shipment = stockMovement?.shipment
            if (eventType.eventCode == EventTypeCode.COMPLETED && !shipment.hasShipped()) {
                throw IllegalStateException("Unable to complete a shipment until it has been shipped")
            }

            // Create new shipment event to represent status update
            BigDecimal latitude = executionStatus?.geoData?.latitude
            BigDecimal longitude = executionStatus?.geoData?.longitude
            Date eventDate = dateFormatter.parse(executionStatus.dateTime)
            Event event = new Event(eventType: eventType, eventDate: eventDate, longitude: longitude, latitude: latitude)
            shipment.addToEvents(event)
            shipment.save(flush: true)
        }
    }
}
