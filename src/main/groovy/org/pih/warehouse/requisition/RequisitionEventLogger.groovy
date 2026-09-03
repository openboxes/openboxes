package org.pih.warehouse.requisition

import grails.validation.ValidationException
import java.time.Instant
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Event
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode

/**
 * Uses {@link EventLog} to log the occurrence of Requisition related {@link Event}s.
 */
@Component
class RequisitionEventLogger {

    /**
     * Log the occurrence of some requisition related action.
     */
    private EventLog createEventLog(Requisition requisition, EventLog eventLog) {
        if (!eventLog.save()) {
            throw new ValidationException("Unable to create requisition event log", eventLog.errors)
        }
        requisition.addToEventLogs(eventLog)

        return eventLog
    }

    /**
     * Log the occurrence of a requisition {@link Event}.
     */
    EventLog logEvent(Requisition requisition, Event event) {
        EventLog eventLog = new EventLog(
                event: event,
                eventCode: event.eventType?.eventCode,
                eventDate: event.eventDate ? InstantParser.asInstant(event.eventDate) : Instant.now(),
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: event.comment?.comment,
                location: event.eventLocation)

        return createEventLog(requisition, eventLog)
    }

    /**
     * Log the occurrence of a requisition {@link Event} being rolled back / reverted.
     */
    EventLog logEventRollback(Requisition requisition, Event event) {
        EventLog rollbackEventLog = new EventLog(
                event: null,  // We can't reference the event because it is going to be deleted by the rollback
                eventCode: event.eventType?.eventCode,
                eventDate: Instant.now(),
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                location: event.eventLocation)

        // Find all other event logs that reference the Event and remove the reference so that we don't get
        // "deleted object would be re-saved by cascade" exceptions.
        requisition.eventLogs.findAll { it.event == event }.each { it.event = null }

        return createEventLog(requisition, rollbackEventLog)
    }
}
