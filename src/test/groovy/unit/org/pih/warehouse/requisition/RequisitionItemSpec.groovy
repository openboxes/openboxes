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
        given:
            domain.requisition = new Requisition()
            domain.requisition.status = requisitionStatus

        expect:
            domain.displayStatus == requisitionItemStatus

        where:
            requisitionStatus                   || requisitionItemStatus
            RequisitionStatus.PENDING_APPROVAL  || RequisitionItemStatus.PENDING
            RequisitionStatus.PENDING           || RequisitionItemStatus.PENDING
            RequisitionStatus.CREATED           || RequisitionItemStatus.PENDING
            RequisitionStatus.REJECTED          || RequisitionItemStatus.CANCELED
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when at least one child item is type of #childItemType and RequisitionItem.isSubstituted() should return: #isSubstituted'() {
        given:
            RequisitionItem item = new RequisitionItem()
            item.requisitionItemType = childItemType
            domain.requisition = new Requisition()
            domain.requisition.status = RequisitionStatus.CHECKING
            domain.addToRequisitionItems(item)

        expect:
            domain.isSubstituted() == isSubstituted
            domain.displayStatus == status

        where:
            childItemType                    || isSubstituted | status
            RequisitionItemType.SUBSTITUTION || true          | RequisitionItemStatus.SUBSTITUTED
            null                             || false         | RequisitionItemStatus.PENDING
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when qty is #quantity and the qty approved is #quantityApproved and RequisitionItem.isApproved() should return: #isApproved'() {
        given:
            domain.requisition = new Requisition()
            domain.requisition.status = RequisitionStatus.CHECKING
            domain.quantity = quantity
            domain.quantityApproved = quantityApproved

        expect:
            domain.isApproved() == isApproved
            domain.displayStatus == status

        where:
            quantity | quantityApproved || isApproved | status
            2        | 2                || true       | RequisitionItemStatus.APPROVED
            1        | 0                || false      | RequisitionItemStatus.PENDING
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when item has child item and the quantity cancelled is #quantityCancelled and RequisitionItem.isChanged() is #isChanged'() {
        given:
            domain.requisition = new Requisition()
            domain.requisition.status = RequisitionStatus.CHECKING
            domain.quantityCanceled = quantityCancelled
            domain.modificationItem = new RequisitionItem()

        expect:
            domain.isChanged() == isChanged
            domain.displayStatus == status

        where:
            quantityCancelled || isChanged | status
            1                 || true      | RequisitionItemStatus.CHANGED
            0                 || false     | RequisitionItemStatus.PENDING
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when item has quantity cancelled #quantityCancelled and quantity #quantity and RequisitionItem.isCancelled() is #isCancelled'() {
        given:
            domain.requisition = new Requisition()
            domain.requisition.status = RequisitionStatus.CHECKING
            domain.quantityCanceled = quantityCancelled
            domain.quantity = quantity

        expect:
            domain.isCanceled() == isCancelled
            domain.displayStatus == status

        where:
            quantity | quantityCancelled || isCancelled | status
            1        | 1                 || true        | RequisitionItemStatus.CANCELED
            2        | 1                 || false       | RequisitionItemStatus.PENDING
    }
}
