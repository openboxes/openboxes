package org.pih.warehouse.order

import grails.validation.ValidationException
import java.time.Instant
import org.springframework.stereotype.Component

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.core.EventCode
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
     * Logs the occurrence of a putaway order being completed.
     */
    EventLog logPutaway(Order order, Putaway putaway) {

        EventLog eventLog = new EventLog(
                event: null,  // A putaway is not Event based
                eventCode: EventCode.PUTAWAY,
                eventDate: putaway.putawayDate ? InstantParser.asInstant(putaway.putawayDate) : Instant.now(),
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: order.description,
                location: putaway.destination,
        )

        return createEventLog(order, eventLog)
    }

    /**
     * Logs the occurrence of a putaway order being partially completed.
     */
    EventLog logPartialPutaway(Order order, Putaway putaway) {

        EventLog eventLog = new EventLog(
                event: null,  // A putaway is not Event based
                eventCode: EventCode.PARTIALLY_PUTAWAY,
                eventDate: putaway.putawayDate ? InstantParser.asInstant(putaway.putawayDate) : Instant.now(),
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: order.description,
                location: putaway.destination,
        )

        return createEventLog(order, eventLog)
    }
}
