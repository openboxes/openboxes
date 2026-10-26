package org.pih.warehouse.regression.platform.shipping

import org.pih.warehouse.shipping.*
import grails.testing.gorm.DataTest
import grails.validation.ValidationException
import spock.lang.Specification

import java.time.Instant

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.event.EventTypeManager
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderEventLogger
import org.pih.warehouse.order.OrderEventManager

class EventManagersSpec extends Specification implements DataTest {

    EventType shippedType
    EventTypeManager eventTypeManager = Stub(EventTypeManager)
    Location depot

    void setupSpec() {
        mockDomains(Shipment, Order, Event, EventType, EventLog, Location, Comment)
    }

    void setup() {
        shippedType = new EventType(name: 'Shipped', eventCode: EventCode.SHIPPED).save(validate: false)
        eventTypeManager.getOrCreateEventType(_ as EventCode) >> shippedType
        depot = new Location(name: 'Depot').save(validate: false, flush: true)
    }

    void 'creating a shipment event saves it, adds it to the shipment and logs it'() {
        given:
        Shipment shipment = new Shipment(name: 'S-1').save(validate: false)
        ShipmentEventManager manager = new ShipmentEventManager(new ShipmentEventLogger(), eventTypeManager)
        Date shippedOn = Date.from(Instant.parse('2026-03-05T12:00:00Z'))

        when:
        Event event = manager.createEvent(shipment, shippedOn, EventCode.SHIPPED, depot)

        then:
        event.id
        event.eventType == shippedType
        event.eventLocation == depot
        shipment.events.contains(event)
        shipment.eventLogs.size() == 1
        with(shipment.eventLogs.first()) {
            it.event == event
            eventCode == EventCode.SHIPPED
            eventDate == Instant.parse('2026-03-05T12:00:00Z')
            eventLogCode == EventLogCode.EVENT_OCCURRED
            location == depot
        }
    }

    void 'rolling back a shipment event deletes it and logs the rollback'() {
        given:
        Shipment shipment = new Shipment(name: 'S-1').save(validate: false)
        ShipmentEventManager manager = new ShipmentEventManager(new ShipmentEventLogger(), eventTypeManager)
        Event event = manager.createEvent(shipment, new Date(), EventCode.SHIPPED, depot)
        shipment.currentEvent = event

        when:
        manager.rollbackEvent(shipment, event)

        then:
        !shipment.events.contains(event)
        shipment.currentEvent == null
        shipment.eventLogs*.eventLogCode as Set == [EventLogCode.EVENT_OCCURRED, EventLogCode.EVENT_ROLLBACK_OCCURRED] as Set
        shipment.eventLogs.every { it.event == null }
        Event.get(event.id) == null
    }

    void 'an invalid event is rejected'() {
        given:
        Shipment shipment = new Shipment(name: 'S-1').save(validate: false)
        ShipmentEventManager manager = new ShipmentEventManager(new ShipmentEventLogger(), eventTypeManager)
        Event invalid = Stub(Event) { save() >> null }

        when:
        manager.createEvent(shipment, invalid)

        then:
        ValidationException e = thrown()
        e.message.startsWith('Unable to create shipment event')
    }

    void 'a putaway event of an order is logged with the comment or the order description'() {
        given:
        Order order = new Order(orderNumber: 'PUT-1', description: 'Putaway of PO-1').save(validate: false)
        OrderEventManager manager = new OrderEventManager(new OrderEventLogger(), eventTypeManager)
        Putaway putaway = new Putaway(destination: depot, putawayDate: Date.from(Instant.parse('2026-03-05T12:00:00Z')))

        when:
        Event event = manager.createPutawayEvent(order, putaway, EventCode.PUTAWAY)

        then:
        order.events.contains(event)
        event.eventLocation == depot
        with(order.eventLogs.first()) {
            message == 'Putaway of PO-1'
            eventDate == Instant.parse('2026-03-05T12:00:00Z')
            location == depot
        }
    }
}
