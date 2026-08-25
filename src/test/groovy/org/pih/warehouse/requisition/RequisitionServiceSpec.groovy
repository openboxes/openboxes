package org.pih.warehouse.requisition

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest

import org.pih.warehouse.core.DeliveryTypeCode
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

    void 'getRequisitionsPendingAutoAllocation should order requisitions by priority, then delivery type code priority, then date created'() {
        given:
        Location origin = new Location(id: 1)
        Date now = new Date()

        and: 'requisitions are registered out of order so the finder cannot rely on insertion order'
        Requisition lowPriorityOlder = new Requisition(
                id: "1",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
                deliveryTypeCode: DeliveryTypeCode.SHIP_TO,
                dateCreated: now - 2,
        )
        Requisition highPriority = new Requisition(
                id: "2",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 100,
                deliveryTypeCode: DeliveryTypeCode.DEFAULT,
                dateCreated: now,
        )
        Requisition samePriorityBetterDeliveryType = new Requisition(
                id: "3",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
                deliveryTypeCode: DeliveryTypeCode.PICK_UP,
                dateCreated: now - 1,
        )
        Requisition samePriorityNullDeliveryTypeOlder = new Requisition(
                id: "4",
                origin: origin,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
                deliveryTypeCode: null,
                dateCreated: now - 3,
        )

        and: 'a requisition that should not be included since it is not pending auto allocation'
        Requisition notCreated = new Requisition(
                id: "5",
                origin: origin,
                status: RequisitionStatus.PICKED,
                autoAllocationRequested: true,
                priority: 100,
                dateCreated: now,
        )

        [lowPriorityOlder, highPriority, samePriorityBetterDeliveryType, samePriorityNullDeliveryTypeOlder, notCreated].each {
            it.save(validate: false, flush: true)
        }

        when:
        List<Requisition> requisitions = service.getRequisitionsPendingAutoAllocation(origin)

        then: 'highest priority is first, then lowest delivery type priority, then oldest date created, with null delivery type sorted last'
        requisitions == [
                highPriority,
                samePriorityBetterDeliveryType,
                lowPriorityOlder,
                samePriorityNullDeliveryTypeOlder,
        ]
    }
}
