package org.pih.warehouse.regression.platform.requisition

import spock.lang.PendingFeature

import org.pih.warehouse.requisition.*
import org.pih.warehouse.inventory.LocalTransfer
import org.pih.warehouse.inventory.Transaction
import org.joda.time.LocalDate
import org.joda.time.DateTimeUtils
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.product.Product

/**
 * The requisition service logic that works on requisition items in memory: normalizing the changes recorded on
 * items and deciding which item a picklist is generated for.
 */
@Unroll
class RequisitionServiceLogicSpec extends Specification implements ServiceUnitTest<RequisitionService>, DataTest {

    void setupSpec() {
        mockDomains(Requisition, RequisitionItem, Product, Transaction, LocalTransfer)
    }

    private static RequisitionItem itemWithChange(RequisitionItemType changeType) {
        RequisitionItem item = new RequisitionItem(quantity: 10)
        item.addToRequisitionItems(new RequisitionItem(quantity: 5, requisitionItemType: changeType))
        return item
    }

    void 'normalizeRequisition records a #changeType change as the #field of its parent'() {
        given:
        RequisitionItem item = itemWithChange(changeType)
        Requisition requisition = new Requisition(requestNumber: 'R-1')
        requisition.addToRequisitionItems(item)
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 3))

        when:
        service.normalizeRequisition(requisition)

        then:
        item."${field}".is(item.requisitionItems.first())

        where:
        changeType                          | field
        RequisitionItemType.SUBSTITUTION    | 'substitutionItem'
        RequisitionItemType.PACKAGE_CHANGE  | 'modificationItem'
        RequisitionItemType.QUANTITY_CHANGE | 'modificationItem'
    }

    void 'normalizeRequisition refuses a change of type #changeType'() {
        given:
        Requisition requisition = new Requisition(requestNumber: 'R-2')
        requisition.addToRequisitionItems(itemWithChange(changeType))

        when:
        service.normalizeRequisition(requisition)

        then:
        Exception exception = thrown()
        exception.message == message

        where:
        changeType                   | message
        RequisitionItemType.ORIGINAL | 'Original requisition item cannot be modified for requisition R-2'
        RequisitionItemType.ADDITION | 'Addition operation not supported for requisition R-2'
    }

    void 'normalizeRequisition refuses an item with more than one change'() {
        given:
        RequisitionItem item = itemWithChange(RequisitionItemType.SUBSTITUTION)
        item.addToRequisitionItems(new RequisitionItem(quantity: 2, requisitionItemType: RequisitionItemType.QUANTITY_CHANGE))
        Requisition requisition = new Requisition(requestNumber: 'R-3')
        requisition.addToRequisitionItems(item)

        when:
        service.normalizeRequisition(requisition)

        then:
        Exception exception = thrown()
        exception.message == 'Cannot have more than one change per requisition item'
    }

    void 'generatePicklist accepts approved, canceled, changed and substituted items'() {
        given:
        Product product = new Product(productCode: 'AS1', name: 'Aspirin')
        Requisition requisition = new Requisition()
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 4, quantityApproved: 4, quantityCanceled: 0))
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 4, quantityApproved: 0, quantityCanceled: 4))
        RequisitionItem changed = new RequisitionItem(quantity: 4, quantityCanceled: 4)
        changed.modificationItem = new RequisitionItem(quantity: 2, product: product)
        requisition.addToRequisitionItems(changed)
        RequisitionItem substituted = new RequisitionItem(quantity: 4, quantityCanceled: 0)
        substituted.addToRequisitionItems(new RequisitionItem(quantity: 4, product: product,
                requisitionItemType: RequisitionItemType.SUBSTITUTION))
        substituted.substitutionItem = substituted.requisitionItems.first()
        requisition.addToRequisitionItems(substituted)
        // OBAF: RequisitionItem.getStatus() (via toString in generatePicklistItem) now checks isReduced() first, which
        // reads requisition.status, so the modification and substitution items carry their requisition as in production
        changed.modificationItem.requisition = requisition
        substituted.substitutionItem.requisition = requisition

        when:
        service.generatePicklist(requisition)

        then:
        noExceptionThrown()
    }

    void 'generatePicklist refuses an item that was neither approved nor changed'() {
        given:
        Requisition requisition = new Requisition()
        requisition.addToRequisitionItems(new RequisitionItem(quantity: 4, quantityApproved: 0, quantityCanceled: 0))

        when:
        service.generatePicklist(requisition)

        then:
        thrown(UnsupportedOperationException)
    }

    void cleanup() {
        DateTimeUtils.setCurrentMillisSystem()
    }

    @PendingFeature(reason = 'OBAF removes transactions while iterating them: ConcurrentModificationException')
    void 'rollbackRequisition removes and deletes every transaction of an issued requisition'() {
        given:
        Requisition requisition = new Requisition(requestNumber: 'R-9', status: RequisitionStatus.ISSUED, dateIssued: new Date())
        requisition.addToTransactions(new Transaction(transactionNumber: 'T-1'))
        requisition.addToTransactions(new Transaction(transactionNumber: 'T-2'))
        requisition.addToTransactions(new Transaction(transactionNumber: 'T-3'))

        when:
        service.rollbackRequisition(requisition)

        then:
        requisition.status == RequisitionStatus.CHECKING
        !requisition.dateIssued
        requisition.transactions.isEmpty()
    }

    void 'isDateOneWeekFromNow works on #today, in the last week of a month'() {
        given:
        DateTimeUtils.setCurrentMillisFixed(today.toDateTimeAtStartOfDay().millis)

        expect:
        service.isDateOneWeekFromNow(today.plusDays(8))
        !service.isDateOneWeekFromNow(today.plusDays(7))
        !service.isDateOneWeekFromNow(today.plusDays(2))

        where:
        today << [new LocalDate(2026, 1, 28), new LocalDate(2026, 2, 25), new LocalDate(2026, 12, 31)]
    }
}
