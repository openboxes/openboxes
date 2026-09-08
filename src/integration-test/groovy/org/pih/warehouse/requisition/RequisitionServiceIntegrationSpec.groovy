package org.pih.warehouse.requisition

import grails.gorm.transactions.Rollback
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

/**
 * Verifies the auto allocation sweep query ordering (OBLS-904). getRequisitionsPendingAutoAllocation
 * orders results partly by deliveryTypePriority, a Hibernate formula-mapped property computed via raw
 * SQL, so this behavior can only be verified against a real database rather than the mocked GORM
 * datastore used by RequisitionServiceSpec.
 */
@Rollback
class RequisitionServiceIntegrationSpec extends IntegrationSpec {

    RequisitionService requisitionService

    void 'getRequisitionsPendingAutoAllocation orders requisitions by priority, then delivery type code priority, then date created'() {
        given: 'requisitions built out of order, pending auto allocation at the same origin'
        Location origin = new LocationTestBuilder().findOrBuildMainFacility()
        Location destination = new LocationTestBuilder().name("Test Destination ${UUID.randomUUID()}").build(true)
        Person requestedBy = Person.build()

        Requisition lowPriorityOlder = Requisition.build(
                origin: origin,
                destination: destination,
                requestedBy: requestedBy,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
                deliveryTypeCode: DeliveryTypeCode.SHIP_TO,
        )
        Requisition highPriority = Requisition.build(
                origin: origin,
                destination: destination,
                requestedBy: requestedBy,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 100,
                deliveryTypeCode: DeliveryTypeCode.DEFAULT,
        )
        Requisition samePriorityBetterDeliveryType = Requisition.build(
                origin: origin,
                destination: destination,
                requestedBy: requestedBy,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
                deliveryTypeCode: DeliveryTypeCode.PICK_UP,
        )
        Requisition samePriorityNullDeliveryTypeOlder = Requisition.build(
                origin: origin,
                destination: destination,
                requestedBy: requestedBy,
                status: RequisitionStatus.CREATED,
                autoAllocationRequested: true,
                priority: 0,
                deliveryTypeCode: null,
        )

        and: 'a requisition that should not be included since it is not pending auto allocation'
        Requisition notCreated = Requisition.build(
                origin: origin,
                destination: destination,
                requestedBy: requestedBy,
                status: RequisitionStatus.PICKED,
                autoAllocationRequested: true,
                priority: 100,
        )

        and: 'pending inserts are flushed so the bulk update below can find them by id'
        Requisition.withSession { it.flush() }

        and: 'dateCreated is set directly in the database, bypassing GORM auto-timestamping on insert'
        setDateCreated(lowPriorityOlder, new Date() - 2)
        setDateCreated(highPriority, new Date())
        setDateCreated(samePriorityBetterDeliveryType, new Date() - 1)
        setDateCreated(samePriorityNullDeliveryTypeOlder, new Date() - 3)
        setDateCreated(notCreated, new Date())

        and: 'the session is cleared so the criteria query below re-reads current database state'
        Requisition.withSession { it.clear() }

        when:
        List<Requisition> requisitions = requisitionService.getRequisitionsPendingAutoAllocation(origin)

        then: 'highest priority is first, then lowest delivery type priority, then oldest date created, with null delivery type sorted last'
        requisitions*.id == [
                highPriority.id,
                samePriorityBetterDeliveryType.id,
                lowPriorityOlder.id,
                samePriorityNullDeliveryTypeOlder.id,
        ]
    }

    private void setDateCreated(Requisition requisition, Date dateCreated) {
        Requisition.executeUpdate(
                "update Requisition r set r.dateCreated = :dateCreated where r.id = :id",
                [dateCreated: dateCreated, id: requisition.id],
        )
    }
}
