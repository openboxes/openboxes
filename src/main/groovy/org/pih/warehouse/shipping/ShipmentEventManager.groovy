package org.pih.warehouse.shipping

import grails.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.event.EventManager

/**
 * Manages the lifecycle (creating and rolling back) of a Shipment related {@link Event}.
 */
@Component
class ShipmentEventManager extends EventManager {

    @Autowired
    ShipmentEventLogger shipmentEventLogger

    /**
     * Create a new Shipment event representing some state change to the shipment, then logs the action.
     */
    Event createEvent(Shipment shipment, Event event) {
        if (!event.save()) {
            throw new ValidationException("Unable to create shipment event", event.errors)
        }
        shipment.addToEvents(event)

        shipmentEventLogger.logEvent(shipment, event)

        return event
    }

    /**
     * Create a new Shipment Event representing some state change to the shipment, then logs the action.
     */
    Event createEvent(Shipment shipment, Date eventDate, EventCode eventCode, Location location) {
        EventType eventType = getOrCreateEventType(eventCode)
        Event event = new Event(
                eventDate: eventDate,
                eventType: eventType,
                eventLocation: location,
                createdBy: AuthService.currentUser)

        return createEvent(shipment, event)
    }

    /**
     * Deletes a Shipment Event representing some state change to the Shipment, then logs the rollback action.
     */
    void rollbackEvent(Shipment shipment, Event event) {
        // This is one of the key differences between Events and EventLogs. When a rollback happens, we delete
        // the Event, but create a new EventLog.
        shipmentEventLogger.logEventRollback(shipment, event)

        // Remove all associations to the Event before deleting it.
        shipment.removeFromEvents(event)
        // We should technically call shipment.resynchronizeEventAndStatus() here since removing the Event will modify
        // the values of status and currentEvent, but because those values are refreshed when the shipment is persisted,
        // and since we don't rely on those values during rollback, for performance reasons we simply null the current
        // event and let the fields be refreshed when save() is called on the shipment later.
        shipment.currentEvent = null
        event.delete()

        // Ensure the order summary gets refreshed
        if (shipment.isFromPurchaseOrder) {
            shipment.disableRefresh = false
        }
    }
}
