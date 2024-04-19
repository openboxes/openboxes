package unit.org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RequisitionItemSpec extends Specification implements DomainUnitTest<RequisitionItem> {

    void 'RequisitionItem.getDisplayStatus() should return: #requisitionItemStatus for requisition status #requisitionStatus'() {
        when:
        domain.requisition = new Requisition()
        domain.requisition.status = requisitionStatus

        then:
        domain.displayStatus == requisitionItemStatus

        where:
        requisitionItemStatus          || requisitionStatus
        RequisitionItemStatus.PENDING  || RequisitionStatus.PENDING_APPROVAL
        RequisitionItemStatus.PENDING  || RequisitionStatus.PENDING
        RequisitionItemStatus.CANCELED || RequisitionStatus.REJECTED
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when at least one child item is type of #childItemType and RequisitionItem.isSubstituted() should return: #isSubstituted'() {
        when:
        RequisitionItem item = new RequisitionItem()
        item.requisitionItemType = childItemType
        domain.requisition = new Requisition()
        domain.requisition.status = RequisitionStatus.CHECKING
        domain.addToRequisitionItems(item)

        then:
        domain.isSubstituted() == isSubstituted
        domain.displayStatus == status

        where:
        status                            || childItemType                    | isSubstituted
        RequisitionItemStatus.SUBSTITUTED || RequisitionItemType.SUBSTITUTION | true
        RequisitionItemStatus.PENDING     || null                             | false
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when qty is #quantity and the qty approved is #quantityApproved and RequisitionItem.isApproved() should return: #isApproved'() {
        when:
        domain.requisition = new Requisition()
        domain.requisition.status = RequisitionStatus.CHECKING
        domain.quantity = quantity
        domain.quantityApproved = quantityApproved

        then:
        domain.isApproved() == isApproved
        domain.displayStatus == status

        where:
        status                         || quantity | quantityApproved | isApproved
        RequisitionItemStatus.APPROVED || 2        | 2                | true
        RequisitionItemStatus.PENDING  || 1        | 0                | false
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when item has child item and the quantity cancelled is #quantityCancelled and RequisitionItem.isChanged() is #isChanged'() {
        when:
        domain.requisition = new Requisition()
        domain.requisition.status = RequisitionStatus.CHECKING
        domain.quantityCanceled = quantityCancelled
        domain.modificationItem = new RequisitionItem()

        then:
        domain.isChanged() == isChanged
        domain.displayStatus == status

        where:
        status                        || quantityCancelled | isChanged
        RequisitionItemStatus.CHANGED || 1                 | true
        RequisitionItemStatus.PENDING || 0                 | false
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when item has quantity cancelled #quantityCancelled and quantity #quantity and RequisitionItem.isCancelled() is #isCancelled'() {
        when:
            domain.requisition = new Requisition()
            domain.requisition.status = RequisitionStatus.CHECKING
            domain.quantityCanceled = quantityCancelled
            domain.quantity = quantity

        then:
            domain.isCanceled() == isCancelled
            domain.displayStatus == status

        where:
            status                          || quantity | quantityCancelled | isCancelled
            RequisitionItemStatus.CANCELED  || 1        | 1                 | true
            RequisitionItemStatus.PENDING   || 2        | 1                 | false
    }
}
