package org.pih.warehouse.api.regression.obaf.issuance

import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationService
import org.pih.warehouse.PaginatedList
import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.AutoIssuanceTransactionDto
import org.pih.warehouse.inventory.AutoIssuanceTransactionReportService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.AutoIssuanceTransactionsReportCommand
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

/**
 * Auto-issuance on a real database: allocation with autoIssuanceRequested issues at once
 * (AllocationService.completeAllocation), and the auto-issuance transaction report
 * (AutoIssuanceTransactionReportService) lists the outbound lines of auto-issued orders only,
 * with filters, sorting and paging.
 */
class AutoIssuanceIntegrationSpec extends ObafIntegrationSpec {

    @Autowired
    AllocationService allocationService

    @Autowired
    StockMovementService stockMovementService

    @Autowired
    AutoIssuanceTransactionReportService autoIssuanceTransactionReportService

    static final Date SOON = new Date() + 30

    /** An order allocated (picked: non-mobile depot) and issued; autoIssuanceRequested as given. */
    String issued(String dc, List<Map> lines, boolean autoIssuance) {
        String req = requisition(dc, lines, [deliveryTypeCode: DeliveryTypeCode.PICK_UP, autoIssuanceRequested: autoIssuance])
        tx { allocationService.allocate(Requisition.get(req), AllocationMode.AUTO, []) }
        tx { stockMovementService.issueRequisition(Requisition.get(req)) }
        assert status(req) == RequisitionStatus.ISSUED
        return req
    }

    PaginatedList<AutoIssuanceTransactionDto> report(String dc, Map props = [:]) {
        return tx {
            AutoIssuanceTransactionsReportCommand cmd = new AutoIssuanceTransactionsReportCommand(
                    facility: Location.get(dc), startDate: new Date() - 1, endDate: new Date() + 1)
            props.each { k, v -> cmd[k] = v instanceof Closure ? v.call() : v }
            autoIssuanceTransactionReportService.getAutoIssuanceTransactions(cmd)
        } as PaginatedList<AutoIssuanceTransactionDto>
    }

    static List<List> rows(PaginatedList<AutoIssuanceTransactionDto> list) {
        return list.collect { [prefixOf(it.binLocationName), it.quantityReduced, it.quantityOnHand] }
    }

    void 'allocating an order with autoIssuanceRequested (automatic allocation) issues it straight away'() {
        given:
        String dc = depot('AI-NOW', [ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
        String b1 = bin(dc, 'B1')
        String p = product('AINOW')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 3]], [deliveryTypeCode: DeliveryTypeCode.PICK_UP, autoIssuanceRequested: true])
        tx { Requisition r = Requisition.get(req); r.autoAllocationRequested = true; r.save(flush: true, failOnError: true) }

        when:
        tx { allocationService.allocateRequisition(req) }

        then:
        status(req) == RequisitionStatus.ISSUED
        availability(dc, p)[['B1', 'L']] == [7, 7]
    }

    void 'the report lists auto-issued orders only, one row per product, bin and transaction'() {
        given:
        String dc = depot('AI-REP')
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('AIREP')
        stock(dc, p, [[b1, 3, 'E', SOON], [b2, 10, 'L', SOON + 100]])
        String auto = issued(dc, [[productId: p, quantity: 5]], true)
        issued(dc, [[productId: p, quantity: 1]], false)
        String autoNumber = tx { Requisition.get(auto).requestNumber }

        when:
        PaginatedList<AutoIssuanceTransactionDto> result = report(dc)

        then:
        result.totalCount == 2
        result*.requisitionNumber.unique() == [autoNumber]
        rows(result)*.take(2).sort() == [['B1', 3], ['B2', 2]]
        // quantityOnHand is current availability; issuing refreshes it asynchronously
        eventually(15000) { rows(report(dc)).sort() == [['B1', 3, 0], ['B2', 2, 7]] }
        result.every { it.productId == p && it.transactionId && it.transactionDate }
    }

    void 'the report filters by product and bin, sorts by bin and pages'() {
        given:
        String dc = depot('AI-FILT')
        String b1 = bin(dc, 'B1')
        String b2 = bin(dc, 'B2')
        String p = product('AIFA')
        String other = product('AIFB')
        stock(dc, p, [[b1, 2, 'E', SOON], [b2, 10, 'L', SOON + 100]])
        stock(dc, other, [[b1, 5, 'O', SOON]])
        issued(dc, [[productId: p, quantity: 4], [productId: other, quantity: 1]], true)

        expect:
        report(dc).totalCount == 3
        report(dc, [products: { [Product.get(p)] }]).totalCount == 2
        report(dc, [binLocations: { [Location.get(b2)] }])*.productId == [p]
        report(dc, [sort: 'binLocation', order: 'desc', products: { [Product.get(p)] }]).collect { prefixOf(it.binLocationName) } == ['B2', 'B1']

        and: 'paging: max 1, offset 1 still reports the full count'
        PaginatedList<AutoIssuanceTransactionDto> page = report(dc, [max: 1, offset: 1])
        page.size() == 1
        page.totalCount == 3
    }

    void 'the report is per facility and per date window'() {
        given:
        String dc = depot('AI-WIN')
        String b1 = bin(dc, 'B1')
        String p = product('AIWIN')
        stock(dc, p, [[b1, 5, 'L', SOON]])
        issued(dc, [[productId: p, quantity: 1]], true)

        expect:
        report(dc).totalCount == 1
        report(dc, [startDate: new Date() + 1, endDate: new Date() + 2]).totalCount == 0
        report(depot('AI-OTHER')).totalCount == 0
    }

    void 'characterization: a report with no date range returns nothing (startDate/endDate are nullable but the query requires both)'() {
        given:
        String dc = depot('AI-NODATE')
        String b1 = bin(dc, 'B1')
        String p = product('AINODATE')
        stock(dc, p, [[b1, 5, 'L', SOON]])
        issued(dc, [[productId: p, quantity: 1]], true)

        expect:
        report(dc, [startDate: null, endDate: null]).totalCount == 0
    }

    void 'the CSV export path (max = -1) returns every row'() {
        given:
        String dc = depot('AI-CSV')
        String b1 = bin(dc, 'B1')
        String p = product('AICSV')
        stock(dc, p, [[b1, 5, 'L', SOON]])
        issued(dc, [[productId: p, quantity: 1]], true)
        issued(dc, [[productId: p, quantity: 1]], true)

        expect:
        report(dc, [max: -1]).size() == 2
    }

    void 'characterization: the auto-issuance JOB issues STAGED orders that never asked for auto-issuance, and the auto-issuance report then omits them'() {
        given:
        config.set('openboxes.jobs.automaticIssuanceJob.enabled', true)
        String dc = depot('AI-JOB')
        String b1 = bin(dc, 'B1')
        String p = product('AIJOB')
        stock(dc, p, [[b1, 5, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 2]], [deliveryTypeCode: DeliveryTypeCode.PICK_UP, autoIssuanceRequested: false])
        tx { allocationService.allocate(Requisition.get(req), AllocationMode.AUTO, []) }
        tx { Requisition r = Requisition.get(req); r.status = RequisitionStatus.STAGED; r.save(flush: true, failOnError: true) }

        when:
        QuartzRunner.fire(scheduler(), AutomaticIssuanceJob, [requisitionId: req])

        then:
        status(req) == RequisitionStatus.ISSUED
        report(dc).totalCount == 0
    }
}
