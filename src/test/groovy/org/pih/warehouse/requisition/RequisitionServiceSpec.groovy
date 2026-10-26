package org.pih.warehouse.requisition

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionIdentifierService
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import grails.validation.ValidationException

@Unroll
class RequisitionServiceSpec extends Specification implements ServiceUnitTest<RequisitionService>, DataTest {

    @Shared
    InventoryService inventoryService

    void setupSpec() {
        mockDomain Requisition
        mockDomain Picklist
    }

    void setup() {
        inventoryService = Stub(InventoryService) {
            generateTransactionNumber(_ as Transaction) >> UUID.randomUUID().toString()
        }
        service.inventoryService = inventoryService

        service.requisitionIdentifierService = Stub(RequisitionIdentifierService) {
            generate() >> UUID.randomUUID().toString()
        }
    }


    void 'saveRequisition saves the given requisition'() {
        given:
        Requisition requisition = new Requisition(id: 1)

        expect:
        null != service.saveRequisition(requisition)
    }

    void 'issueRequisition should throw an exception when picklist is missing'() {
        given:
        Requisition requisition = new Requisition(id: 1)
        String comment = "Comment to requisition with errors"

        and:
        Requisition persistedRequisition = service.saveRequisition(requisition)

        when:
        service.issueRequisition(persistedRequisition, Mock(User), Mock(Person), comment)

        then:
        thrown(ValidationException)
    }

    void 'issueRequisition should throw an exception when inventory service is unable to save local transfer'() {
        given:
        Requisition requisition = new Requisition(id: 1)
        String comment = "Comment to requisition with errors"

        and:
        Picklist.metaClass.static.findByRequisition = {
            Requisition foundRequisition -> return new Picklist(requisition: foundRequisition)
        }
        Requisition persistedRequisition = service.saveRequisition(requisition)

        when:
        service.issueRequisition(persistedRequisition, Mock(User), Mock(Person), comment)

        then:
        thrown(ValidationException)
    }

    void 'issueRequisition should change the requisition status to issued'() {
        given:
        Requisition requisition = new Requisition(id: 1)
        String comment = "Comment to issued requisition"

        and:
        Picklist.metaClass.static.findByRequisition = {
            Requisition foundRequisition -> return new Picklist(requisition: foundRequisition)
        }
        Requisition persistedRequisition = service.saveRequisition(requisition)

        and:
        service.inventoryService.saveLocalTransfer(_ as Transaction) >> true

        when:
        service.issueRequisition(persistedRequisition, Mock(User), Mock(Person), comment)

        then:
        notThrown(ValidationException)
        persistedRequisition.status == RequisitionStatus.ISSUED
    }

    void 'rollbackRequisition should change requisition status to #requisitionStatus when requisition has status #currentStatus'() {
        given:
        Requisition requisition = new Requisition(
                id: 1,
                status: currentStatus as RequisitionStatus,
                issuedBy: issuedBy as Person,
                dateIssued: dateIssued as Date,
        )

        when:
        service.rollbackRequisition(requisition)

        then:
        requisition.status == requisitionStatus
        requisition.issuedBy == null
        requisition.dateIssued == null

        where:
        currentStatus              || requisitionStatus          | issuedBy     | dateIssued
        RequisitionStatus.CHECKING || RequisitionStatus.CHECKING | null         | null
        RequisitionStatus.ISSUED   || RequisitionStatus.CHECKING | Mock(Person) | new Date()
    }

    void 'cloneRequisition should return copy of the passed requisition'() {
        given:
        Requisition requisition = new Requisition(
                id: 1,
                name: 'Requisition',
                version: 1,
                requestedBy: Mock(Person),
                description: 'Description',
                dateRequested: new Date(),
                requestedDeliveryDate: new Date(),
                lastUpdated: new Date(),
                status: RequisitionStatus.CHECKING,
                type: RequisitionType.ADHOC,
                origin: Mock(Location),
        )

        when:
        Requisition copyOfRequisition = service.cloneRequisition(requisition)
        Map jsonOfOriginalRequisition = requisition.toJson()
        Map jsonOfCopiedRequisition = copyOfRequisition.toJson()

        then:
        'Copy of ' + jsonOfOriginalRequisition['name'] == jsonOfCopiedRequisition['name']
        jsonOfOriginalRequisition.remove('name')
        jsonOfCopiedRequisition.remove('name')
        jsonOfOriginalRequisition == jsonOfCopiedRequisition
    }

    // NOTE: getRequisitionsPendingAutoAllocation orders results using a createCriteria query, ordering
    // partly by deliveryTypePriority, a Hibernate formula-mapped property computed via raw SQL. The
    // grails-gorm-testing-support mockDomain used by this spec does not execute real SQL, so it can
    // filter and order by plain columns (origin/status/autoAllocationRequested/priority, verified below)
    // but cannot evaluate the formula-based deliveryTypePriority ordering or reliably control dateCreated
    // (GORM overwrites it on insert). The full three-level ordering is verified against a real database
    // in RequisitionServiceIntegrationSpec.
    void 'getRequisitionsPendingAutoAllocation should filter to CREATED requisitions pending auto allocation for the given origin, ordered by priority (highest first)'() {
        given:
        Location origin = new Location(id: 1)

        Requisition lowPriority = new Requisition(
                id: "1",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
        )
        Requisition highPriority = new Requisition(
                id: "2",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 100,
        )

        and: 'requisitions that should not be included'
        Requisition notCreated = new Requisition(
                id: "3",
                origin: origin,
                status: RequisitionStatus.PICKED,
                autoAllocationRequested: true,
                priority: 100,
        )
        Requisition notRequestedForAutoAllocation = new Requisition(
                id: "4",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: false,
                priority: 100,
        )
        Requisition differentOrigin = new Requisition(
                id: "5",
                origin: new Location(id: 2),
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 100,
        )

        [lowPriority, highPriority, notCreated, notRequestedForAutoAllocation, differentOrigin].each {
            it.save(validate: false, flush: true)
        }

        when:
        List<Requisition> requisitions = service.getRequisitionsPendingAutoAllocation(origin)

        then:
        requisitions == [highPriority, lowPriority]
    }
}
