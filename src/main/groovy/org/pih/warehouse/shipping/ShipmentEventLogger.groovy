package org.pih.warehouse.shipping

import grails.validation.ValidationException
import java.time.Instant
import org.springframework.stereotype.Component

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode

/**
 * Uses {@link EventLog} to log the occurrence of Shipment related {@link Event}s and other actions.
 */
@Component
class ShipmentEventLogger {

    /**
     * Log the occurrence of some shipment related action.
     */
    private EventLog createEventLog(Shipment shipment, EventLog eventLog) {
        if (!eventLog.save()) {
            throw new ValidationException("Unable to create shipment event log", eventLog.errors)
        }
        shipment.addToEventLogs(eventLog)

        return eventLog
    }

    /**
     * Log the occurrence of a shipment {@link Event}.
     */
    EventLog logEvent(Shipment shipment, Event event) {

        EventLog eventLog = new EventLog(
                event: event,
                eventCode: event.eventType?.eventCode,
                eventDate: event.eventDate ? InstantParser.asInstant(event.eventDate) : Instant.now(),
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: event.comment?.comment,
                location: event.eventLocation)

        return createEventLog(shipment, eventLog)
    }

    /**
     * Log the occurrence of a shipment {@link Event} being rolled back / reverted.
     */
    EventLog logEventRollback(Shipment shipment, Event event) {
        EventLog rollbackEventLog = new EventLog(
                event: null,  // We can't reference the event because it is going to be deleted by the rollback
                eventCode: event.eventType?.eventCode,
                eventDate: Instant.now(),
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                location: event.eventLocation)

        // Find all other event logs that reference the Event and remove the reference so that we don't get
        // "deleted object would be re-saved by cascade" exceptions.
        shipment.eventLogs.findAll { it.event == event }.each { it.event = null }

        return createEventLog(shipment, rollbackEventLog)
    }
}
