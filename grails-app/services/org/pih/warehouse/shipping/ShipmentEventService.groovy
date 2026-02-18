package org.pih.warehouse.shipping

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location

@Transactional
class ShipmentEventService {

    ShipmentEventLogService shipmentEventLogService

    /**
     * Create a new shipment event representing some state change to the shipment, then logs the action.
     */
    Event createShipmentEvent(Shipment shipment, Event event) {
        if (!event.validate()) {
            throw new ValidationException("Unable to create shipment event", event.errors)
        }
        shipment.addToEvents(event)

        shipmentEventLogService.logShipmentEvent(shipment, event)

        return event
    }

    /**
     * Create a new shipment event representing some state change to the shipment, then logs the action.
     */
    Event createShipmentEvent(Shipment shipment, Date eventDate, EventCode eventCode, Location location) {
        EventType eventType = EventType.findByEventCode(eventCode)
        if (!eventType) {
            throw new RuntimeException("Unable to find event type for event code ${eventCode}")
        }

        Event event = new Event(
                eventDate: eventDate,
                eventType: eventType,
                eventLocation: location,
                createdBy: AuthService.currentUser)

        return createShipmentEvent(shipment, event)
    }

    /**
     * Deletes a shipment event representing some state change to the shipment, then logs the rollback action.
     */
    void rollbackShipmentEvent(Shipment shipment, Event event) {
        // This is one of the key differences between Events and EventLogs. When a rollback happens, we delete
        // the Event, but create a new EventLog.
        shipmentEventLogService.logShipmentEventRollback(shipment, event)

        // Remove all associations to the Event before deleting it. (The associations in EventLog are handled within
        // ShipmentEventLogService.)
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
