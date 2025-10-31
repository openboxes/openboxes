package unit.org.pih.warehouse.requisition

import grails.testing.gorm.DomainUnitTest
import grails.validation.ValidationException
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemStatus
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class RequisitionItemSpec extends Specification implements DomainUnitTest<RequisitionItem> {

    void setup() {
        domain.requisition = new Requisition()
    }

    void 'RequisitionItem.getDisplayStatus() should return: #requisitionItemStatus for requisition status #requisitionStatus'() {
        given:
        domain.requisition.status = requisitionStatus

        expect:
        domain.displayStatus == requisitionItemStatus

        where:
        requisitionStatus                  || requisitionItemStatus
        RequisitionStatus.PENDING_APPROVAL || RequisitionItemStatus.PENDING
        RequisitionStatus.PENDING          || RequisitionItemStatus.PENDING
        RequisitionStatus.CREATED          || RequisitionItemStatus.PENDING
        RequisitionStatus.REJECTED         || RequisitionItemStatus.CANCELED
    }

    void 'RequisitionItem.getDisplayStatus() should return: #status when at least one child item is type of #childItemType and RequisitionItem.isSubstituted() should return: #isSubstituted'() {
        given:
        RequisitionItem item = new RequisitionItem()
        item.requisitionItemType = childItemType
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

    void 'RequisitionItem.changeQuantity() should cancel old #quantity and assign #reasonCode and #comment'() {
        given:
        domain.product = new Product()
        domain.quantity = quantity

        when:
        domain.changeQuantity(3, reasonCode.name(), comment)

        then:
        domain.quantityCanceled == quantity
        domain.cancelReasonCode == reasonCode.name()
        domain.cancelComments == comment

        where:
        quantity | reasonCode                       | comment
        1        | ReasonCode.SUBSTITUTION          | "cancel comment"
        5        | ReasonCode.REJECTED              | "cancel comment 2"
        10       | ReasonCode.CANCELED_BY_REQUESTER | "cancel comment 3"
    }

    void 'RequisitionItem.changeQuantity() should reject applied #quantity and #reasonCode with error message including #fieldName'() {
        given:
        domain.product = new Product()
        domain.quantity = 1

        when:
        domain.changeQuantity(quantity, reasonCode?.name(), "comment")

        then:
        final ValidationException exception = thrown()
        exception.message.contains(fieldName)

        where:
        quantity | reasonCode              || fieldName
        -1       | ReasonCode.SUBSTITUTION || "quantity"
        null     | ReasonCode.SUBSTITUTION || "quantity"
        2        | null                    || "cancelReasonCode"
        0        | null                    || "cancelReasonCode"
    }

    void "RequisitionItem.approveQuantity() should calculate #quantityApproved when #quantity and #quantityCanceled are passed"() {
        given:
        domain.quantity = quantity
        domain.quantityCanceled = quantityCanceled

        when:
        domain.approveQuantity()

        then:
        domain.quantityApproved == quantityApproved
        notThrown(ValidationException)

        where:
        quantity | quantityCanceled || quantityApproved
        2        | 1                || 1
        10       | 5                || 5
        0        | null             || 0
    }

    void "RequisitionItem.approveQuantity() should throw validation exception when #quantity and #quantityCanceled are passed"() {
        given:
        domain.quantity = quantity
        domain.quantityCanceled = quantityCanceled

        when:
        domain.approveQuantity()

        then:
        thrown(ValidationException)

        where:
        quantity | quantityCanceled
        10       | 11
        20       | 20
        21       | 50
        0        | 0
    }

    void "RequisitionItem.quantityNotCanceled() should calculate #quantityNotCanceled when #quantity and #quantityCanceled are passed"() {
        given:
        domain.quantity = quantity
        domain.quantityCanceled = quantityCanceled

        expect:
        quantityNotCanceled == domain.quantityNotCanceled()

        where:
        quantity | quantityCanceled || quantityNotCanceled
        null     | null             || 0
        2        | null             || 2
        null     | 4                || 0
        10       | 5                || 5
    }

    void "RequisitionItem.totalQuantity() should calculate #totalQuantity when #quantity and #productPackageQuantity are passed"() {
        given:
        domain.quantity = quantity
        domain.productPackage = Stub(ProductPackage)
        domain.productPackage.quantity >> productPackageQuantity

        expect:
        totalQuantity == domain.totalQuantity()

        where:
        quantity | productPackageQuantity || totalQuantity
        null     | null                   || 0
        1        | null                   || 1
        null     | 1                      || 0
        10       | 5                      || 50
    }

    void "RequisitionItem.totalQuantityCanceled() should calculate #totalQuantityCanceled when #quantityCanceled and #productPackageQuantity are passed"() {
        given:
        domain.quantityCanceled = quantityCanceled
        domain.productPackage = Stub(ProductPackage)
        domain.productPackage.quantity >> productPackageQuantity

        expect:
        totalQuantityCanceled == domain.totalQuantityCanceled()

        where:
        quantityCanceled | productPackageQuantity || totalQuantityCanceled
        null             | null                   || 0
        1                | null                   || 1
        null             | 1                      || 0
        10               | 5                      || 50
    }

    void "RequisitionItem.totalQuantityApproved() should calculate #totalQuantityApproved when #quantityApproved and #productPackageQuantity are passed"() {
        given:
        domain.quantityApproved = quantityApproved
        domain.productPackage = Stub(ProductPackage)
        domain.productPackage.quantity >> productPackageQuantity

        expect:
        totalQuantityApproved == domain.totalQuantityApproved()

        where:
        quantityApproved | productPackageQuantity || totalQuantityApproved
        null             | null                   || 0
        1                | null                   || 1
        null             | 1                      || 0
        10               | 5                      || 50
    }

    void "RequisitionItem.totalQuantityNotCanceled() should calculate #totalNotCanceledQuantity when #notCanceledQuantity and #productPackageQuantity are passed"() {
        given:
        RequisitionItem requisitionItem = Spy(RequisitionItem) {
            quantityNotCanceled() >> notCanceledQuantity
        }
        requisitionItem.productPackage = Stub(ProductPackage)
        requisitionItem.productPackage.quantity >> productPackageQuantity

        expect:
        totalNotCanceledQuantity == requisitionItem.totalQuantityNotCanceled()

        where:
        notCanceledQuantity | productPackageQuantity || totalNotCanceledQuantity
        null                | null                   || 0
        1                   | null                   || 1
        null                | 1                      || 0
        10                  | 5                      || 50
    }

    void "RequisitionItem.isCanceled() should return: #isCanceled when #quantity and #quantityCanceled and #modificationItem and #substitutionItem and #requisitionItems are passed"() {
        given:
        RequisitionItem requisitionItem = Spy(RequisitionItem) {
            totalQuantityCanceled() >> quantityCanceled
            totalQuantity() >> quantity
        }

        requisitionItem.modificationItem = modificationItem
        requisitionItem.substitutionItem = substitutionItem
        requisitionItem.setRequisitionItems(requisitionItems as Set)

        expect:
        isCanceled == requisitionItem.isCanceled()

        where:
        quantityCanceled | quantity | modificationItem      | substitutionItem      | requisitionItems        || isCanceled
        3                | 3        | null                  | null                  | null                    || true
        3                | 3        | Mock(RequisitionItem) | null                  | null                    || false
        3                | 3        | null                  | Mock(RequisitionItem) | null                    || false
        3                | 3        | null                  | null                  | [Mock(RequisitionItem)] || false
        3                | 2        | null                  | null                  | null                    || false
    }

    void "RequisitionItem.isCanceledDuringPick() should return: #isCanceled when requisition status is #status and quantityPicked of modificationItem is #modificationItemQuantityPicked"() {
        given:
        RequisitionItem modificationItem = Stub(RequisitionItem) {
            calculateQuantityPicked() >> modificationItemQuantityPicked
        }

        domain.modificationItem = modificationItem
        domain.requisition.status = status

        expect:
        isCanceled == domain.isCanceledDuringPick()

        where:
        status                      | modificationItemQuantityPicked || isCanceled
        RequisitionStatus.PICKED    | 0                              || true
        RequisitionStatus.ISSUED    | 0                              || true
        RequisitionStatus.VERIFYING | null                           || false
        RequisitionStatus.PICKING   | null                           || false
    }

    void "RequisitionItem.isSubstitute() should return: #isSubstituted when quantityCanceled #quantityCanceled and substitutionItem #substitutionItem"() {
        given:
        domain.quantityCanceled = quantityCanceled
        domain.substitutionItem = substitutionItem
        domain.requisitionItems = []

        expect:
        isSubstituted == domain.isSubstituted()

        where:
        quantityCanceled | substitutionItem      || isSubstituted
        0                | null                  || false
        1                | Mock(RequisitionItem) || true
        1                | null                  || false
    }

    void "RequisitionItem.isSubstituted() should return true when at least one requisitionItem is substituted"() {
        given:
        RequisitionItem requisitionItem = Stub(RequisitionItem)
        requisitionItem.requisitionItemType >> RequisitionItemType.SUBSTITUTION
        domain.requisitionItems = [Mock(RequisitionItem),
                                   requisitionItem,
                                   Mock(RequisitionItem),]

        expect:
        true == domain.isSubstituted()
    }
}
