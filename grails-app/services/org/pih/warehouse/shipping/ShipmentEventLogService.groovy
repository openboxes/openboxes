package org.pih.warehouse.shipping

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import java.time.Instant

import org.pih.warehouse.core.Event
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode

@Transactional
class ShipmentEventLogService {

    /**
     * Log the occurrence of some shipment related action.
     */
    EventLog createShipmentEventLog(Shipment shipment, EventLog eventLog) {
        if (!eventLog.validate()) {
            throw new ValidationException("Unable to create shipment event log", eventLog.errors)
        }
        shipment.addToEventLogs(eventLog)

        return eventLog
    }

    /**
     * Log the occurrence of a shipment {@link Event}.
     */
    EventLog logShipmentEvent(Shipment shipment, Event event) {

        EventLog eventLog = new EventLog(
                event: event,
                eventCode: event.eventType?.eventCode,
                eventDate: event.eventDate ? InstantParser.asInstant(event.eventDate) : Instant.now(),
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: event.comment?.comment,
                location: event.eventLocation)

        return createShipmentEventLog(shipment, eventLog)
    }

    /**
     * Log the occurrence of a shipment {@link Event} being rolled back / reverted.
     */
    EventLog logShipmentEventRollback(Shipment shipment, Event event) {
        EventLog rollbackEventLog = new EventLog(
                event: null,  // We can't reference the event because it is going to be deleted by the rollback
                eventCode: event.eventType?.eventCode,
                eventDate: Instant.now(),
                eventLogCode: EventLogCode.ROLLBACK_EVENT_OCCURRED,
                location: event.eventLocation)

        // Find all other event logs that reference the Event and remove the reference so that we don't get
        // "deleted object would be re-saved by cascade" exceptions.
        shipment.eventLogs.findAll { it.event == event }.each { it.event = null }

        return createShipmentEventLog(shipment, rollbackEventLog)
    }
}
