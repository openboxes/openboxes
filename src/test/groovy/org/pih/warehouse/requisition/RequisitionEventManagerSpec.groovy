package org.pih.warehouse.requisition

import grails.testing.gorm.DataTest
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.event.EventTypeManager
import spock.lang.Specification

class RequisitionEventManagerSpec extends Specification implements DataTest {

    RequisitionEventLogger requisitionEventLogger
    EventTypeManager eventTypeManager
    RequisitionEventManager requisitionEventManager

    void setupSpec() {
        mockDomains(Requisition, Event)
    }

    void setup() {
        requisitionEventLogger = Mock(RequisitionEventLogger)
        eventTypeManager = Mock(EventTypeManager)
        requisitionEventManager = new RequisitionEventManager(requisitionEventLogger, eventTypeManager)
    }

    void 'recordStatusChange should create a new event for a forward transition'() {
        given:
        Location origin = new Location()
        Requisition requisition = new Requisition()
        EventType eventType = new EventType(eventCode: EventCode.PICKING)
        eventTypeManager.getOrCreateEventType(EventCode.PICKING) >> eventType

        when:
        requisitionEventManager.recordStatusChange(requisition, RequisitionStatus.CREATED, RequisitionStatus.PICKING, origin)

        then:
        requisition.events.size() == 1
        requisition.events[0].eventType == eventType
        requisition.events[0].eventLocation == origin
        1 * requisitionEventLogger.logEvent(requisition, _ as Event)
        0 * requisitionEventLogger.logEventRollback(*_)
    }

    void 'recordStatusChange should treat the very first status assignment as a forward transition'() {
        given:
        Requisition requisition = new Requisition()
        EventType eventType = new EventType(eventCode: EventCode.CREATED)
        eventTypeManager.getOrCreateEventType(EventCode.CREATED) >> eventType

        when:
        requisitionEventManager.recordStatusChange(requisition, null, RequisitionStatus.CREATED, null)

        then:
        requisition.events.size() == 1
        1 * requisitionEventLogger.logEvent(requisition, _ as Event)
    }

    void 'recordStatusChange should do nothing for a forward transition into a status without an eventCode'() {
        given:
        Requisition requisition = new Requisition()

        when:
        // DELETED has no EventCode counterpart and isn't part of the tracked transition timeline
        requisitionEventManager.recordStatusChange(requisition, RequisitionStatus.CREATED, RequisitionStatus.DELETED, null)

        then:
        !requisition.events
        0 * requisitionEventLogger.logEvent(*_)
        0 * requisitionEventLogger.logEventRollback(*_)
        0 * eventTypeManager.getOrCreateEventType(*_)
    }

    void 'recordStatusChange should roll back the old status event for a backward transition instead of creating a new one'() {
        given:
        Requisition requisition = new Requisition()
        EventType issuedEventType = new EventType(eventCode: EventCode.ISSUED)
        Event issuedEvent = new Event(eventType: issuedEventType)
        issuedEvent.save(flush: true)
        requisition.addToEvents(issuedEvent)

        when:
        requisitionEventManager.recordStatusChange(requisition, RequisitionStatus.ISSUED, RequisitionStatus.CHECKING, null)

        then:
        !requisition.events.contains(issuedEvent)
        1 * requisitionEventLogger.logEventRollback(requisition, issuedEvent)
        0 * requisitionEventLogger.logEvent(*_)
        0 * eventTypeManager.getOrCreateEventType(*_)
    }

    void 'recordStatusChange should do nothing when rolling back a status that never had a recorded event'() {
        given:
        Requisition requisition = new Requisition()

        when:
        requisitionEventManager.recordStatusChange(requisition, RequisitionStatus.ISSUED, RequisitionStatus.CHECKING, null)

        then:
        noExceptionThrown()
        !requisition.events
        0 * requisitionEventLogger.logEvent(*_)
        0 * requisitionEventLogger.logEventRollback(*_)
    }

    void 'recordStatusChange should use CANCELLED for the CANCELED status, which has no same-named EventCode'() {
        given:
        Requisition requisition = new Requisition()
        EventType eventType = new EventType(eventCode: EventCode.CANCELLED)
        eventTypeManager.getOrCreateEventType(EventCode.CANCELLED) >> eventType

        when:
        requisitionEventManager.recordStatusChange(requisition, RequisitionStatus.CREATED, RequisitionStatus.CANCELED, null)

        then:
        requisition.events.size() == 1
        1 * requisitionEventLogger.logEvent(requisition, _ as Event)
    }

    void 'recordStatusChange should do nothing when the old status being rolled back from has no eventCode'() {
        given:
        Requisition requisition = new Requisition()

        when:
        // DELETED has no eventCode; RequisitionStatus.CREATED's lower sortOrder still makes this a rollback
        requisitionEventManager.recordStatusChange(requisition, RequisitionStatus.DELETED, RequisitionStatus.CREATED, null)

        then:
        noExceptionThrown()
        !requisition.events
        0 * requisitionEventLogger.logEvent(*_)
        0 * requisitionEventLogger.logEventRollback(*_)
        0 * eventTypeManager.getOrCreateEventType(*_)
    }
}
