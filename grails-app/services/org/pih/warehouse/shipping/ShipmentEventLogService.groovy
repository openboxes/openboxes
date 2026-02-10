package org.pih.warehouse.shipping

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventLog
import org.pih.warehouse.core.EventLogCode

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
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: event.comment?.comment ?: event.eventType?.description,
                eventLocation: event.eventLocation)

        return createShipmentEventLog(shipment, eventLog)
    }

    /**
     * Log the occurrence of a shipment {@link Event} being rolled back / reverted.
     */
    EventLog logShipmentEventRollback(Shipment shipment, Event event) {
        EventLog eventLog = new EventLog(
                event: null,  // We can't reference the event in the log because it is going to be deleted
                eventLogCode: EventLogCode.ROLLBACK_EVENT_OCCURRED,
                message: "Rolling back event: ${event.eventType?.eventCode}",
                eventLocation: event.eventLocation)

        // Find all other event logs referencing that Event and null the reference so that we don't get
        // "deleted object would be re-saved by cascade" exceptions.
        List<EventLog> eventLogsToClean = EventLog.findAllByEvent(event)
        for (EventLog eventLogToClean in eventLogsToClean) {
            eventLogToClean.event = null
        }

        return createShipmentEventLog(shipment, eventLog)
    }
}
