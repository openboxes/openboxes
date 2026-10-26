package org.pih.warehouse.regression.platform.requisition

import org.pih.warehouse.requisition.*
import static org.pih.warehouse.requisition.RequisitionStatus.*

import grails.util.Holders
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.StatusType
import org.pih.warehouse.inventory.StockMovementStatusCode

@Unroll
class RequisitionStatusSpec extends Specification implements GrailsUnitTest {

    /**
     * Stands in for the application tag lib that mapToOption looks up by bean name, so the option label can be
     * asserted without loading the real message bundles.
     */
    static class LabelTagLib {
        String message(Map args) {
            return "label for ${args.code}"
        }
    }

    void setup() {
        Holders.grailsApplication = grailsApplication
    }

    void 'a #status status should be displayed as itself'() {
        expect:
        status.displayStatus == status

        where:
        status << [RequisitionStatus.CREATED, RequisitionStatus.PENDING, RequisitionStatus.ISSUED,
                   RequisitionStatus.CANCELED, RequisitionStatus.PENDING_APPROVAL]
    }

    void 'the checking status should be displayed as pending'() {
        expect:
        RequisitionStatus.CHECKING.displayStatus == RequisitionStatus.PENDING
    }

    void 'each status should carry its variant for the status badge'() {
        expect:
        RequisitionStatus.CREATED.variant == StatusType.SUCCESS
        RequisitionStatus.PENDING_APPROVAL.variant == StatusType.WARNING
        RequisitionStatus.REJECTED.variant == StatusType.DANGER
        RequisitionStatus.PENDING.variant == StatusType.PRIMARY
    }

    void 'compare should order statuses by their sort order, not by their declaration order'() {
        expect:
        RequisitionStatus.compare(RequisitionStatus.CREATED, RequisitionStatus.ISSUED) < 0
        RequisitionStatus.compare(RequisitionStatus.ISSUED, RequisitionStatus.CREATED) > 0
        RequisitionStatus.compare(RequisitionStatus.PICKED, RequisitionStatus.PICKED) == 0

        and: 'the outbound mapping statuses are declared last but sort before every workflow status'
        RequisitionStatus.compare(RequisitionStatus.DISPATCHED, RequisitionStatus.CREATED) < 0
    }

    void 'sorting with compare should follow the workflow from created to error'() {
        given:
        List<RequisitionStatus> shuffled = [RequisitionStatus.ERROR, RequisitionStatus.PICKING,
                                            RequisitionStatus.CREATED, RequisitionStatus.ISSUED,
                                            RequisitionStatus.EDITING]

        expect:
        shuffled.sort(false) { a, b -> RequisitionStatus.compare(a, b) } ==
                [RequisitionStatus.CREATED, RequisitionStatus.EDITING, RequisitionStatus.PICKING,
                 RequisitionStatus.ISSUED, RequisitionStatus.ERROR]
    }

    void 'the in progress outbound lists should be the outbound options without the issued and canceled statuses'() {
        expect:
        // OBAF: adds the STAGED status to the outbound options (between CHECKING and ISSUED); the in progress lists
        // stay as they were, so STAGED is listed but not counted as in progress
        RequisitionStatus.listOutboundOptions() == [CREATED, EDITING, VERIFYING, PICKING, PICKED, CHECKING, STAGED, ISSUED, CANCELED]
        RequisitionStatus.listOutboundInProgress() == [CREATED, EDITING, VERIFYING, PICKING, PICKED, CHECKING]
        RequisitionStatus.listOutboundOptionsWhenApprovalRequired() ==
                [CREATED, VERIFYING, APPROVED, EDITING, PICKING, PICKED, CHECKING, STAGED, ISSUED, CANCELED]
        RequisitionStatus.listOutboundInProgressWhenApprovalRequired() ==
                [CREATED, VERIFYING, APPROVED, EDITING, PICKING, PICKED, CHECKING]
    }

    void 'the outbound options should only add the approved status when approval is required'() {
        expect:
        (RequisitionStatus.listOutboundOptionsWhenApprovalRequired() as Set) ==
                (RequisitionStatus.listOutboundOptions() + [RequisitionStatus.APPROVED]) as Set
    }

    void 'the request options for a non approver should drop the waiting for approval and rejected statuses'() {
        expect:
        RequisitionStatus.listRequestOptionsWhenNonApprover() ==
                RequisitionStatus.listRequestOptionsWhenApprovalRequired() -
                [RequisitionStatus.PENDING_APPROVAL, RequisitionStatus.REJECTED]
        RequisitionStatus.listRequestOptions() ==
                [RequisitionStatus.VERIFYING, RequisitionStatus.PICKING, RequisitionStatus.PICKED,
                 RequisitionStatus.CHECKING]
    }

    void 'the pending, completed and canceled groups should not share any status'() {
        given:
        Set pending = RequisitionStatus.listPending() as Set
        Set completed = RequisitionStatus.listCompleted() as Set
        Set canceled = RequisitionStatus.listCanceled() as Set

        expect:
        pending.intersect(completed).empty
        pending.intersect(canceled).empty
        completed.intersect(canceled).empty
        RequisitionStatus.listCompleted() == [RequisitionStatus.ISSUED, RequisitionStatus.RECEIVED]
        RequisitionStatus.listCanceled() == [RequisitionStatus.CANCELED, RequisitionStatus.DELETED]
    }

    void 'the approval group should hold the three approval statuses'() {
        expect:
        RequisitionStatus.listApproval() as Set ==
                [RequisitionStatus.APPROVED, RequisitionStatus.REJECTED, RequisitionStatus.PENDING_APPROVAL] as Set
    }

    void 'the status filter list should leave out the removed statuses'() {
        expect:
        !RequisitionStatus.list().any { it in [RequisitionStatus.OPEN, RequisitionStatus.FULFILLED] }
        !RequisitionStatus.listAll().contains(RequisitionStatus.OPEN)
    }

    void 'toStockMovementStatus should map #requisitionStatus to #expected'() {
        expect:
        RequisitionStatus.toStockMovementStatus(requisitionStatus) == expected

        where:
        requisitionStatus                  | expected
        RequisitionStatus.EDITING          | StockMovementStatusCode.REQUESTING
        RequisitionStatus.VERIFYING        | StockMovementStatusCode.REQUESTED
        RequisitionStatus.CHECKING         | StockMovementStatusCode.PACKED
        RequisitionStatus.ISSUED           | StockMovementStatusCode.DISPATCHED
        null                               | StockMovementStatusCode.REQUESTING
        RequisitionStatus.CREATED          | StockMovementStatusCode.CREATED
        RequisitionStatus.PICKING          | StockMovementStatusCode.PICKING
        RequisitionStatus.PICKED           | StockMovementStatusCode.PICKED
        RequisitionStatus.PENDING_APPROVAL | StockMovementStatusCode.PENDING_APPROVAL
        RequisitionStatus.APPROVED         | StockMovementStatusCode.APPROVED
        RequisitionStatus.REJECTED         | StockMovementStatusCode.REJECTED
        RequisitionStatus.CANCELED         | StockMovementStatusCode.CANCELED
    }

    void 'toStockMovementStatus should reject the #requisitionStatus status, which has no stock movement counterpart'() {
        when:
        RequisitionStatus.toStockMovementStatus(requisitionStatus)

        then:
        thrown(IllegalArgumentException)

        where:
        requisitionStatus << [RequisitionStatus.RECEIVED, RequisitionStatus.DELETED, RequisitionStatus.ERROR]
    }

    void 'fromStockMovementStatus should map #stockMovementStatus to #expected'() {
        expect:
        RequisitionStatus.fromStockMovementStatus(stockMovementStatus) == expected

        where:
        stockMovementStatus                      | expected
        StockMovementStatusCode.REQUESTING       | RequisitionStatus.EDITING
        StockMovementStatusCode.REQUESTED        | RequisitionStatus.VERIFYING
        StockMovementStatusCode.VALIDATED        | RequisitionStatus.VERIFYING
        StockMovementStatusCode.PENDING_APPROVAL | RequisitionStatus.PENDING_APPROVAL
        StockMovementStatusCode.PACKED           | RequisitionStatus.CHECKING
        StockMovementStatusCode.DISPATCHED       | RequisitionStatus.ISSUED
        StockMovementStatusCode.CREATED          | RequisitionStatus.CREATED
        StockMovementStatusCode.PICKING          | RequisitionStatus.PICKING
        StockMovementStatusCode.CANCELED         | RequisitionStatus.CANCELED
    }

    void 'fromStockMovementStatus should reject the #stockMovementStatus status, which has no requisition counterpart'() {
        when:
        RequisitionStatus.fromStockMovementStatus(stockMovementStatus)

        then:
        thrown(IllegalArgumentException)

        where:
        stockMovementStatus << [StockMovementStatusCode.IN_PROGRESS, StockMovementStatusCode.VALIDATING,
                                StockMovementStatusCode.CHECKED]
    }

    void 'the explicitly mapped #requisitionStatus status should survive a round trip through the stock movement status'() {
        expect:
        RequisitionStatus.fromStockMovementStatus(RequisitionStatus.toStockMovementStatus(requisitionStatus)) ==
                requisitionStatus

        where:
        requisitionStatus << [RequisitionStatus.EDITING, RequisitionStatus.VERIFYING, RequisitionStatus.CHECKING,
                              RequisitionStatus.ISSUED, RequisitionStatus.PICKED]
    }

    void 'the sort order formula should be a sql case expression over every workflow status'() {
        when:
        String formula = RequisitionStatus.statusSortOrderFormula

        then:
        formula.startsWith("(case status ")
        formula.endsWith("else 0 end)")
        formula.contains("when 'CREATED' then 1 ")
        formula.contains("when 'CHECKING' then 10 ")
        formula.contains("when 'REJECTED' then 5 ")
        // OBAF: STAGED takes sort order 11, so every later status moves up one (DELETED 14 -> 15)
        formula.contains("when 'DELETED' then 15 ")

        and: 'the statuses that only exist for mapping fall through to the default'
        !formula.contains("'DISPATCHED'")
        !formula.contains("'OPEN'")
    }

    void 'mapToOption should build a select option labelled through the message bundle'() {
        given:
        defineBeans {
            "org.grails.plugins.web.taglib.ApplicationTagLib"(LabelTagLib)
        }

        expect:
        RequisitionStatus.mapToOption(RequisitionStatus.PICKED) == [
                id   : "PICKED",
                value: "PICKED",
                label: "label for enum.RequisitionStatus.PICKED",
        ]
    }

    void 'toString should return the plain status name'() {
        expect:
        RequisitionStatus.PENDING_APPROVAL.toString() == "PENDING_APPROVAL"
        "${RequisitionStatus.ISSUED}" == "ISSUED"
    }
}
