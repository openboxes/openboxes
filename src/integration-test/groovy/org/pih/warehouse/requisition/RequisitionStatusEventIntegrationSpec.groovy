package org.pih.warehouse.requisition

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

/**
 * Verifies that requisition status transitions are recorded as Events by detecting the change where it is
 * persisted (Requisition#beforeUpdate / #afterInsert, via GORM dirty checking) rather than by intercepting
 * assignment.
 *
 * Deliberately NOT @Rollback: RequisitionStatusChangedEventService runs in the BEFORE_COMMIT phase, so
 * nothing is recorded unless the transaction actually commits. Test data is removed in cleanup() instead.
 */
class RequisitionStatusEventIntegrationSpec extends IntegrationSpec {

    private final List<String> requisitionIds = []

    void cleanup() {
        Requisition.withNewTransaction {
            requisitionIds.each { String requisitionId ->
                Requisition requisition = Requisition.get(requisitionId)
                if (!requisition) {
                    return
                }
                List<Event> events = new ArrayList<>(requisition.events ?: [])
                requisition.eventLogs?.each { it.event = null }
                events.each { requisition.removeFromEvents(it) }
                requisition.delete()
                events.each { it.delete() }
            }
        }
        requisitionIds.clear()
    }

    void 'inserting a requisition records the initial status transition against its origin'() {
        when:
        String requisitionId = createRequisition(RequisitionStatus.CREATED)

        then: 'the Event is recorded with the origin as its location, which is only populated by insert time'
        eventCodesFor(requisitionId) == [EventCode.CREATED]
        locationNameOfFirstEvent(requisitionId) == mainFacilityName()
    }

    void 'changing the status of a persisted requisition records the transition'() {
        given:
        String requisitionId = createRequisition(RequisitionStatus.CREATED)

        when:
        transitionTo(requisitionId, RequisitionStatus.PICKING)

        then:
        eventCodesFor(requisitionId) == [EventCode.CREATED, EventCode.PICKING]
    }

    void 'reassigning the same status records nothing'() {
        given:
        String requisitionId = createRequisition(RequisitionStatus.CREATED)

        when:
        transitionTo(requisitionId, RequisitionStatus.CREATED)

        then:
        eventCodesFor(requisitionId) == [EventCode.CREATED]
    }

    void 'a requisition that is never saved records nothing'() {
        given: 'a transient requisition, as built by the controllers to back a create form'
        int eventCountBefore = countEvents()

        when:
        Requisition requisition = new Requisition(status: RequisitionStatus.CREATED)

        then: 'no Event and no EventType are written for an object that never reaches the database'
        requisition.events == null
        countEvents() == eventCountBefore
    }

    void 'loading a persisted requisition records nothing'() {
        given:
        String requisitionId = createRequisition(RequisitionStatus.CREATED)
        int eventCountBefore = countEvents()

        when:
        RequisitionStatus status = Requisition.withNewTransaction {
            Requisition.withSession { it.clear() }
            return Requisition.get(requisitionId).status
        }

        then:
        status == RequisitionStatus.CREATED
        countEvents() == eventCountBefore
    }

    private String createRequisition(RequisitionStatus status) {
        String requisitionId = Requisition.withNewTransaction {
            Location origin = new LocationTestBuilder().findOrBuildMainFacility()
            Location destination = new LocationTestBuilder()
                    .name("Test Destination ${UUID.randomUUID()}")
                    .build(true)
            return Requisition.build(
                    origin: origin,
                    destination: destination,
                    requestedBy: Person.build(),
                    status: status,
            ).id
        }
        requisitionIds << requisitionId
        return requisitionId
    }

    private void transitionTo(String requisitionId, RequisitionStatus status) {
        Requisition.withNewTransaction {
            Requisition requisition = Requisition.get(requisitionId)
            requisition.status = status
            requisition.save()
        }
    }

    private List<EventCode> eventCodesFor(String requisitionId) {
        return Requisition.withNewTransaction {
            Requisition.withSession { it.clear() }
            return Requisition.get(requisitionId)
                    .events
                    ?.sort { it.dateCreated }
                    ?.collect { it.eventType?.eventCode } ?: []
        }
    }

    private String locationNameOfFirstEvent(String requisitionId) {
        return Requisition.withNewTransaction {
            Requisition.withSession { it.clear() }
            return Requisition.get(requisitionId).events?.min { it.dateCreated }?.eventLocation?.name
        }
    }

    private String mainFacilityName() {
        return Requisition.withNewTransaction {
            return new LocationTestBuilder().findOrBuildMainFacility().name
        }
    }

    private int countEvents() {
        return Requisition.withNewTransaction {
            return Event.count()
        }
    }
}
