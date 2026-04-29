package org.pih.warehouse.order

import grails.validation.ValidationException
import java.time.Instant
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Event
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode

/**
 * Logs the occurrence of Order related events.
 */
@Component
class OrderEventLogger {

    private EventLog createEventLog(Order order, EventLog eventLog) {
        if (!eventLog.save()) {
            throw new ValidationException("Unable to create order event log", eventLog.errors)
        }
        order.addToEventLogs(eventLog)

        return eventLog
    }

    /**
     * Log the occurrence of an order event.
     */
    EventLog logEvent(Order order, Event event) {

        EventLog eventLog = new EventLog(
                event: event,
                eventCode: event.eventType?.eventCode,
                eventDate: event.eventDate ? InstantParser.asInstant(event.eventDate) : Instant.now(),
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: event.comment?.comment ?: order.description,
                location: event.eventLocation,
        )

        return createEventLog(order, eventLog)
    }
}
