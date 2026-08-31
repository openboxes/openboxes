package org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.CommodityClass
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

import org.pih.warehouse.requisition.RequisitionType

class RequisitionSpec extends Specification implements DomainUnitTest<Requisition> {

    void setup() {
        // requisition.status = X now has a side effect (see Requisition#setStatus): it asks
        // RequisitionEventManager to record the transition. Stub it out by default so unrelated tests that merely
        // construct a Requisition with a status aren't affected; tests that care about this behavior install
        // their own mock instead.
        Requisition.metaClass.getRequisitionEventManager = { -> Stub(RequisitionEventManager) }
    }

    void cleanup() {
        GroovySystem.metaClassRegistry.removeMetaClass(Requisition)
    }

    void 'validate should return true for a valid requisition'() {
        when:
        Location location = new Location()
        Product product1 = new Product(name: 'Advil 200mg')
        Product product2 = new Product(name: 'Tylenol 325mg')
        RequisitionItem item1 = new RequisitionItem(product: product1, quantity: 10)
        RequisitionItem item2 = new RequisitionItem(product: product2, quantity: 20)
        Person person = new Person()

        Requisition requisition = new Requisition(
                name: 'testRequisition',
                commodityClass: CommodityClass.MEDICATION,
                type:  RequisitionType.NON_STOCK,
                origin: location,
                destination: location,
                requestedBy: person,
                dateRequested: new Date(),
                requestedDeliveryDate: new Date() + 1,
        )

        requisition.addToRequisitionItems(item1)
        requisition.addToRequisitionItems(item2)

        then:
        assert requisition.validate()
    }

    @Unroll
    void 'isInErrorState should be #expected when status is #status and most recent event log is #eventLogCode'() {
        given:
        Requisition requisition = new Requisition(status: status)
        if (eventLogCode) {
            // dateCreated is an auto-timestamp audit field: GORM's map constructor silently drops it, so it
            // has to be assigned as a separate statement to control ordering in this test.
            EventLog stale = new EventLog(eventLogCode: EventLogCode.EVENT_OCCURRED, message: "stale")
            stale.dateCreated = Instant.now().minusSeconds(60)
            EventLog latest = new EventLog(eventLogCode: eventLogCode, message: "Allocation failed: boom")
            latest.dateCreated = Instant.now()
            requisition.eventLogs = [stale, latest]
        }

        expect:
        requisition.isInErrorState() == expected

        where:
        status                        | eventLogCode                       || expected
        RequisitionStatus.CREATED     | EventLogCode.ERROR_OCCURRED        || true
        RequisitionStatus.PICKING     | EventLogCode.ERROR_OCCURRED        || true
        RequisitionStatus.ISSUED      | EventLogCode.ERROR_OCCURRED        || false
        RequisitionStatus.CREATED     | EventLogCode.EVENT_OCCURRED        || false
        RequisitionStatus.CREATED     | null                               || false
    }

    void 'getMostRecentErrorMessage should return the message of the latest ERROR_OCCURRED event log'() {
        given:
        Requisition requisition = new Requisition(status: RequisitionStatus.CREATED)
        // dateCreated is an auto-timestamp audit field: GORM's map constructor silently drops it, so it has
        // to be assigned as a separate statement to control ordering in this test.
        EventLog first = new EventLog(eventLogCode: EventLogCode.ERROR_OCCURRED, message: "Allocation failed: first")
        first.dateCreated = Instant.now().minusSeconds(60)
        EventLog second = new EventLog(eventLogCode: EventLogCode.ERROR_OCCURRED, message: "Allocation failed: second")
        second.dateCreated = Instant.now()
        requisition.eventLogs = [first, second]

        expect:
        requisition.getMostRecentErrorMessage() == "Allocation failed: second"
    }

    @Unroll
    void 'setStatus should ask RequisitionEventManager to record the transition from #oldStatus to #newStatus'() {
        given:
        Location origin = new Location()
        Requisition requisition = new Requisition(status: oldStatus, origin: origin)

        and:
        RequisitionEventManager mockManager = Mock(RequisitionEventManager)
        requisition.metaClass.getRequisitionEventManager = { -> mockManager }

        when:
        requisition.status = newStatus

        then:
        1 * mockManager.recordStatusChange(requisition, oldStatus, newStatus, origin)

        where:
        oldStatus                 | newStatus
        null                      | RequisitionStatus.CREATED
        RequisitionStatus.CREATED | RequisitionStatus.PICKING
        RequisitionStatus.PICKING | RequisitionStatus.ISSUED
        RequisitionStatus.CREATED | RequisitionStatus.VERIFYING
        // Backward transitions: whether this is a rollback is entirely RequisitionEventManager's call
        // (see RequisitionEventManagerSpec) - setStatus just has to report both statuses faithfully.
        RequisitionStatus.ISSUED  | RequisitionStatus.CHECKING
        RequisitionStatus.CANCELED | RequisitionStatus.PENDING
    }

    void 'setStatus should not ask RequisitionEventManager to do anything when the status does not actually change'() {
        given:
        Requisition requisition = new Requisition(status: RequisitionStatus.CREATED)

        and:
        RequisitionEventManager mockManager = Mock(RequisitionEventManager)
        requisition.metaClass.getRequisitionEventManager = { -> mockManager }

        when:
        requisition.status = RequisitionStatus.CREATED

        then:
        0 * mockManager.recordStatusChange(*_)
    }

    void 'getMostRecentEvent should break ties on EventType.sortOrder when two events land in the same second'() {
        // event_date/date_created only have second precision in the DB (see the
        // set-sort-order-on-requisition-event-types migration), so an automatic allocation immediately followed
        // by automatic issuance can produce two Events with identical timestamps. EventType.sortOrder is what
        // Event#compareTo falls back on to still order them correctly.
        given:
        Date sameSecond = new Date()

        Event pickingEvent = new Event(eventDate: sameSecond, eventType: new EventType(eventCode: EventCode.PICKING, sortOrder: 7))
        pickingEvent.dateCreated = sameSecond

        Event issuedEvent = new Event(eventDate: sameSecond, eventType: new EventType(eventCode: EventCode.ISSUED, sortOrder: 12))
        issuedEvent.dateCreated = sameSecond

        Requisition requisition = new Requisition()
        // Deliberately added with the earlier (lower sortOrder) event last, so a naive "last added" or Set
        // iteration order tiebreak would return the wrong (chronologically earlier) event.
        requisition.events = [issuedEvent, pickingEvent]

        expect:
        requisition.getMostRecentEvent() == issuedEvent
    }
}
