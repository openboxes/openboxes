package org.pih.warehouse.order

import grails.validation.ValidationException
import org.springframework.stereotype.Component

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.event.EventManager

/**
 * Manages the lifecycle (creating and rolling back) of an Order related {@link Event}.
 */
@Component
class OrderEventManager extends EventManager {

    final OrderEventLogger orderEventLogger

    OrderEventManager(final OrderEventLogger orderEventLogger) {
        this.orderEventLogger = orderEventLogger
    }

    private Event createEvent(Order order, Event event) {
        if (!event.save()) {
            throw new ValidationException("Unable to create order event", event.errors)
        }
        order.addToEvents(event)

        orderEventLogger.logEvent(order, event)

        return event
    }

    /**
     * Create a new event representing the putaway of an order, then logs the action.
     */
    Event createPutawayEvent(Order order, Putaway putaway, EventCode eventCode) {
        EventType eventType = getOrCreateEventType(eventCode)
        Event event = new Event(
                eventDate: putaway.putawayDate ?: new Date(),
                eventType: eventType,
                eventLocation: putaway.destination,
                createdBy: AuthService.currentUser,
        )

        return createEvent(order, event)
    }
}
