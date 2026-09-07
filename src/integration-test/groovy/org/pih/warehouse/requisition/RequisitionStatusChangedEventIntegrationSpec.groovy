package org.pih.warehouse.requisition

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener

/**
 * Verifies the full requisition.status -> requisition.events wiring against a real Hibernate session (not
 * just the mocked GORM datastore used by unit specs): Requisition#beforeInsert/#beforeUpdate publish a
 * RequisitionStatusChangedEvent (the latter relying on GORM's isDirty('status')/getPersistentValue('status'),
 * which - per jmiranda's own stated uncertainty on the PR - needed to be confirmed to detect changes and
 * report the right "old" value), which RequisitionStatusChangedEventService turns into an actual
 * RequisitionEventManager#recordStatusChange call. This replaced the previous approach of doing this from a
 * custom Requisition#setStatus setter.
 *
 * Deliberately NOT @Rollback: RequisitionStatusChangedEventService is a BEFORE_COMMIT transactional listener
 * (required - see its class doc for why a plain listener throws ConcurrentModificationException), which only
 * fires on a genuine transaction commit. A @Rollback test transaction never commits, so it would never
 * actually exercise that listener. Each test commits for real (via inTransaction) against the ephemeral
 * per-test-run testcontainers database, and builds its own uniquely-named data, so no cleanup is needed.
 */
class RequisitionStatusChangedEventIntegrationSpec extends IntegrationSpec {

    ApplicationContext applicationContext

    void 'beforeInsert creates an Event for the initial status'() {
        given:
        String requisitionId = inTransaction { buildRequisition(RequisitionStatus.CREATED).id }

        expect:
        eventCodesFor(requisitionId) == [EventCode.CREATED]
    }

    void 'beforeUpdate creates a new Event for a forward status transition'() {
        given: 'a persisted requisition, reloaded so the update below is a genuine change against DB state'
        String requisitionId = inTransaction { buildRequisition(RequisitionStatus.CREATED).id }

        when: 'the status is changed and committed, triggering a real UPDATE and beforeUpdate'
        inTransaction {
            Requisition persisted = Requisition.get(requisitionId)
            persisted.status = RequisitionStatus.PICKING
            persisted.save(flush: true)
        }

        then:
        eventCodesFor(requisitionId) as Set == [EventCode.CREATED, EventCode.PICKING] as Set
    }

    void 'beforeUpdate rolls back the old status Event for a backward status transition'() {
        given: 'a persisted requisition already moved forward to PICKING'
        String requisitionId = inTransaction { buildRequisition(RequisitionStatus.CREATED).id }
        inTransaction {
            Requisition forward = Requisition.get(requisitionId)
            forward.status = RequisitionStatus.PICKING
            forward.save(flush: true)
        }

        when: 'the status is rolled back to CREATED and committed, triggering a real UPDATE and beforeUpdate'
        inTransaction {
            Requisition rolledBack = Requisition.get(requisitionId)
            rolledBack.status = RequisitionStatus.CREATED
            rolledBack.save(flush: true)
        }

        then: 'the PICKING event is gone, leaving only the original CREATED event'
        eventCodesFor(requisitionId) == [EventCode.CREATED]
    }

    void 'beforeUpdate publishes a RequisitionStatusChangedEvent with the correct old and new status'() {
        given: 'a persisted requisition, reloaded so the update below is a genuine change against DB state'
        String requisitionId = inTransaction { buildRequisition(RequisitionStatus.CREATED).id }
        List<RequisitionStatusChangedEvent> capturedEvents = captureStatusChangedEvents()

        when: 'the status is changed and committed, triggering a real UPDATE and beforeUpdate'
        inTransaction {
            Requisition persisted = Requisition.get(requisitionId)
            persisted.status = RequisitionStatus.PICKING
            persisted.save(flush: true)
        }

        then:
        capturedEvents.size() == 1
        capturedEvents[0].oldStatus == RequisitionStatus.CREATED
        capturedEvents[0].newStatus == RequisitionStatus.PICKING
    }

    void 'beforeUpdate does not publish an event when status is unchanged'() {
        given: 'a persisted requisition, reloaded so the update below is against genuine DB state'
        String requisitionId = inTransaction { buildRequisition(RequisitionStatus.CREATED).id }
        List<RequisitionStatusChangedEvent> capturedEvents = captureStatusChangedEvents()

        when: 'an unrelated field changes and is committed, triggering a real UPDATE but no status change'
        inTransaction {
            Requisition persisted = Requisition.get(requisitionId)
            persisted.description = "updated description ${UUID.randomUUID()}"
            persisted.save(flush: true)
        }

        then:
        capturedEvents.isEmpty()
    }

    private Requisition buildRequisition(RequisitionStatus status) {
        Location origin = new LocationTestBuilder().findOrBuildMainFacility()
        Person requestedBy = Person.build()
        return Requisition.build(
                origin: origin,
                destination: origin,
                requestedBy: requestedBy,
                status: status,
        )
    }

    private List<EventCode> eventCodesFor(String requisitionId) {
        return inTransaction {
            Requisition.get(requisitionId).events*.eventType*.eventCode
        }
    }

    private <T> T inTransaction(Closure<T> closure) {
        return Requisition.withTransaction { closure.call() } as T
    }

    private List<RequisitionStatusChangedEvent> captureStatusChangedEvents() {
        // Coercing a closure to ApplicationListener<T> erases T at runtime, so Spring's multicaster can't
        // filter by generic event type and instead invokes this listener for every ApplicationEvent - hence
        // the manual instanceof check rather than relying on the declared closure parameter type.
        List<RequisitionStatusChangedEvent> capturedEvents = []
        applicationContext.addApplicationListener({ Object event ->
            if (event instanceof RequisitionStatusChangedEvent) {
                capturedEvents << event
            }
        } as ApplicationListener)
        return capturedEvents
    }
}
