package org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
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
}
