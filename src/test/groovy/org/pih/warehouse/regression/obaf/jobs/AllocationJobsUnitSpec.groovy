package org.pih.warehouse.regression.obaf.jobs

import grails.testing.gorm.DataTest
import grails.util.Holders
import spock.lang.Specification
import spock.lang.Unroll
import util.LiquibaseUtil

import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationRequest
import org.pih.warehouse.allocation.AllocationResult
import org.pih.warehouse.allocation.AllocationService
import org.pih.warehouse.allocation.BackorderMatch
import org.pih.warehouse.allocation.BackorderMatchingService
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.jobs.AutomaticAllocationJob
import org.pih.warehouse.jobs.AutomaticBackorderReallocationJob
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.jobs.JobUtils
import org.pih.warehouse.regression.obaf.support.JobUnitSupport
import org.pih.warehouse.core.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTaskService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * The three allocation-side Quartz jobs, with their services mocked:
 * AutomaticAllocationJob, AutomaticBackorderReallocationJob, AutomaticIssuanceJob. For each:
 * the config gate, the single-entity path (job data) and the bulk (cron) path, and what each
 * passes to its services. Integration runs of the same jobs: api.regression.obaf.jobs.
 */
class AllocationJobsUnitSpec extends Specification implements DataTest, JobUnitSupport {

    AllocationService allocationService = Mock()
    RequisitionService requisitionService = Mock()
    LocationService locationService = Mock()
    StockMovementService stockMovementService = Mock()
    AuthService authService = Mock()
    ShipmentService shipmentService = Mock()
    PutawayTaskService putawayTaskService = Mock()
    BackorderMatchingService backorderMatchingService = Mock()

    void setupSpec() {
        mockDomain RequisitionItem
    }

    void setup() {
        authService.withSystemUser(_) >> { Closure c -> c.call() }
        LiquibaseUtil.metaClass.static.isRunningMigrations = { -> false }
        jobs('automaticAllocationJob').enabled = true
        jobs('automaticBackorderReallocationJob').enabled = true
        jobs('automaticIssuanceJob').enabled = true
    }

    void cleanup() {
        resetMetaClasses(LiquibaseUtil, Requisition, RequisitionItem, AutomaticAllocationJob, AutomaticIssuanceJob)
    }

    /** The node the jobs read through Holders.config / Holders.flatConfig. */
    Map jobs(String key) {
        Map node = Holders.config.openboxes.jobs[key] as Map
        return node
    }

    AutomaticAllocationJob allocationJob() {
        return new AutomaticAllocationJob(allocationService: allocationService, requisitionService: requisitionService,
                locationService: locationService)
    }

    AutomaticBackorderReallocationJob backorderJob() {
        return new AutomaticBackorderReallocationJob(shipmentService: shipmentService, allocationService: allocationService,
                stockMovementService: stockMovementService, putawayTaskService: putawayTaskService,
                backorderMatchingService: backorderMatchingService)
    }

    AutomaticIssuanceJob issuanceJob() {
        return new AutomaticIssuanceJob(stockMovementService: stockMovementService, requisitionService: requisitionService,
                authService: authService, locationService: locationService)
    }

    void 'the config the jobs read is the config this spec writes (sanity)'() {
        when:
        jobs('automaticIssuanceJob').enabled = false

        then:
        !Holders.config.openboxes.jobs.automaticIssuanceJob.enabled
        !Holders.flatConfig['openboxes.jobs.automaticIssuanceJob.enabled']
        !JobUtils.shouldExecute(AutomaticIssuanceJob)
    }

    // ------------------------------------------------------------------ AutomaticAllocationJob

    void 'AutomaticAllocationJob: disabled does nothing at all'() {
        given:
        jobs('automaticAllocationJob').enabled = false

        when:
        allocationJob().execute(context(requisitionId: 'r1'))
        allocationJob().execute(context())

        then:
        0 * allocationService._
        0 * locationService._
        0 * requisitionService._
    }

    void 'AutomaticAllocationJob: a requisitionId allocates that requisition only'() {
        when:
        allocationJob().execute(context(requisitionId: 'r1'))

        then:
        1 * allocationService.allocateRequisition('r1')
        0 * locationService._
        0 * requisitionService._
    }

    void 'AutomaticAllocationJob: the cron run walks every facility with AUTOMATIC_ALLOCATION_ENABLED, pending requisitions in the order given'() {
        given:
        Location f1 = facility('F1')
        Location f2 = facility('F2')
        Requisition a = new Requisition(); a.id = 'a'
        Requisition b = new Requisition(); b.id = 'b'
        Requisition c = new Requisition(); c.id = 'c'

        when:
        allocationJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities([ActivityCode.AUTOMATIC_ALLOCATION_ENABLED]) >> [f1, f2]
        1 * requisitionService.getRequisitionsPendingAutoAllocation(f1) >> [b, a]

        then:
        1 * allocationService.allocateRequisition('b')

        then:
        1 * allocationService.allocateRequisition('a')

        then:
        1 * requisitionService.getRequisitionsPendingAutoAllocation(f2) >> [c]

        then:
        1 * allocationService.allocateRequisition('c')
    }

    void 'AutomaticAllocationJob: no facility has the activity: the cron run is inert'() {
        when:
        allocationJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities(_) >> []
        0 * requisitionService._
        0 * allocationService._
    }

    void 'AutomaticAllocationJob is not concurrent and reads its cron from openboxes.jobs.automaticAllocationJob'() {
        expect:
        AutomaticAllocationJob.concurrent == false
        new AutomaticAllocationJob().sessionRequired == false
        JobUtils.getCronName(AutomaticAllocationJob) == 'automaticAllocationJobCronTrigger'
    }

    // ------------------------------------------------------------------ AutomaticBackorderReallocationJob

    Product product = product('BOJ')

    Map backorderScenario(Map opts = [:]) {
        Requisition so = new Requisition(requestNumber: 'SO-9', autoAllocationRequested: opts.containsKey('auto') ? opts.auto : true)
        so.id = 'so-9'
        RequisitionItem line = new RequisitionItem(product: product, quantity: 10, requisition: so,
                quantityBackordered: 6, backorderedReasonCode: ReasonCode.BACKORDER.toString())
        line.id = 'line-1'
        line.metaClass.calculateQuantityAllocated = { -> opts.allocatedBefore ?: 4 }
        so.requisitionItems = [line] as LinkedHashSet
        ShipmentItem inboundItem = new ShipmentItem(product: product, quantity: 6, backorderReference: 'SO-9')
        Shipment shipment = new Shipment(shipmentNumber: 'IN-1', shipmentItems: [inboundItem] as LinkedHashSet)
        shipment.id = 'ship-1'
        Requisition.metaClass.static.findByRequestNumber = { String n -> n == 'SO-9' ? so : null }
        return [so: so, line: line, shipment: shipment, inboundItem: inboundItem]
    }

    static AllocationResult allocated(int quantity) {
        return new AllocationResult(suggestedItems: quantity ? [new SuggestedItem(quantityPicked: quantity)] : [])
    }

    void 'AutomaticBackorderReallocationJob: disabled, or a cron run without shipmentId, does nothing'() {
        when:
        jobs('automaticBackorderReallocationJob').enabled = enabled
        backorderJob().execute(context(data))

        then:
        0 * shipmentService._
        0 * allocationService._

        where:
        enabled | data
        false   | [shipmentId: 'ship-1']
        true    | [:]
    }

    void 'AutomaticBackorderReallocationJob releases the backorder from the cross-dock stock and updates the backordered quantity'() {
        given:
        Map s = backorderScenario()

        when:
        backorderJob().execute(context(shipmentId: 'ship-1'))

        then:
        1 * shipmentService.getShipmentInstance('ship-1') >> s.shipment
        1 * putawayTaskService.isCrossDockPutawayCompleted(s.shipment) >> true
        1 * backorderMatchingService.findInboundItemsForRequisition(s.shipment, s.so) >> [s.inboundItem]
        1 * backorderMatchingService.match(s.so, [s.inboundItem]) >> [new BackorderMatch(inboundItem: s.inboundItem, demand: s.line, quantityMatched: 6)]
        1 * backorderMatchingService.remainingDemand(s.line) >> 6
        1 * allocationService.allocate({ AllocationRequest r ->
            r.requisitionItem == s.line && r.quantityRequired == 6 && r.allocationMode == AllocationMode.AUTO && r.crossDockRelease
        }) >> allocated(6)
        1 * stockMovementService.updateRequisitionStatus('so-9', RequisitionStatus.PICKING)
        s.line.quantityBackordered == null
        s.line.backorderedReasonCode == null
    }

    void 'AutomaticBackorderReallocationJob: a partial delivery leaves the rest backordered (reason BACKORDER)'() {
        given:
        Map s = backorderScenario()
        shipmentService.getShipmentInstance('ship-1') >> s.shipment
        putawayTaskService.isCrossDockPutawayCompleted(s.shipment) >> true
        backorderMatchingService.findInboundItemsForRequisition(*_) >> [s.inboundItem]
        backorderMatchingService.match(*_) >> [new BackorderMatch(inboundItem: s.inboundItem, demand: s.line, quantityMatched: 2)]
        backorderMatchingService.remainingDemand(s.line) >> 6

        when:
        backorderJob().execute(context(shipmentId: 'ship-1'))

        then:
        1 * allocationService.allocate(_) >> allocated(2)
        s.line.quantityBackordered == 4
        s.line.backorderedReasonCode == ReasonCode.BACKORDER.toString()
    }

    void 'AutomaticBackorderReallocationJob: nothing allocated keeps the order out of PICKING'() {
        given:
        Map s = backorderScenario()
        shipmentService.getShipmentInstance('ship-1') >> s.shipment
        putawayTaskService.isCrossDockPutawayCompleted(s.shipment) >> true
        backorderMatchingService.findInboundItemsForRequisition(*_) >> [s.inboundItem]
        backorderMatchingService.match(*_) >> [new BackorderMatch(inboundItem: s.inboundItem, demand: s.line, quantityMatched: 6)]
        backorderMatchingService.remainingDemand(s.line) >> 6

        when:
        backorderJob().execute(context(shipmentId: 'ship-1'))

        then:
        1 * allocationService.allocate(_) >> allocated(0)
        0 * stockMovementService.updateRequisitionStatus(*_)
        s.line.quantityBackordered == 6
    }

    void 'AutomaticBackorderReallocationJob defers until the cross-dock putaway is completed (still open, or not created yet)'() {
        given:
        Map s = backorderScenario()
        shipmentService.getShipmentInstance('ship-1') >> s.shipment

        when:
        backorderJob().execute(context(shipmentId: 'ship-1'))

        then:
        1 * putawayTaskService.isCrossDockPutawayCompleted(s.shipment) >> false
        0 * backorderMatchingService._
        0 * allocationService._
    }

    void 'AutomaticBackorderReallocationJob ignores orders without autoAllocationRequested'() {
        given:
        Map s = backorderScenario(auto: false)
        shipmentService.getShipmentInstance('ship-1') >> s.shipment
        putawayTaskService.isCrossDockPutawayCompleted(_) >> true

        when:
        backorderJob().execute(context(shipmentId: 'ship-1'))

        then:
        0 * backorderMatchingService.match(*_)
        0 * allocationService._
    }

    void 'AutomaticBackorderReallocationJob ignores a shipment that carries no backorder link, or is missing'() {
        given:
        Shipment plain = new Shipment(shipmentItems: [new ShipmentItem(product: product, quantity: 1)] as LinkedHashSet)

        when:
        backorderJob().execute(context(shipmentId: 'plain'))
        backorderJob().execute(context(shipmentId: 'missing'))

        then:
        1 * shipmentService.getShipmentInstance('plain') >> plain
        1 * shipmentService.getShipmentInstance('missing') >> null
        0 * putawayTaskService._
        0 * allocationService._
    }

    void 'AutomaticBackorderReallocationJob swallows an allocation failure (logged, not rethrown)'() {
        given:
        Map s = backorderScenario()
        shipmentService.getShipmentInstance('ship-1') >> s.shipment
        putawayTaskService.isCrossDockPutawayCompleted(s.shipment) >> true
        backorderMatchingService.findInboundItemsForRequisition(*_) >> [s.inboundItem]
        backorderMatchingService.match(*_) >> [new BackorderMatch(inboundItem: s.inboundItem, demand: s.line, quantityMatched: 6)]
        backorderMatchingService.remainingDemand(s.line) >> 6
        allocationService.allocate(_) >> { throw new IllegalArgumentException('Insufficient stock') }

        when:
        backorderJob().execute(context(shipmentId: 'ship-1'))

        then:
        notThrown(Exception)
        0 * stockMovementService.updateRequisitionStatus(*_)
    }

    // ------------------------------------------------------------------ AutomaticIssuanceJob

    Requisition staged(Map props) {
        Requisition r = new Requisition([status: RequisitionStatus.STAGED, requestNumber: nextId('SR')] + props)
        r.id = nextId('req')
        return r
    }

    void stubGet(Requisition... rs) {
        Map<String, Requisition> byId = rs.collectEntries { [(it.id): it] }
        Requisition.metaClass.static.get = { Serializable id -> byId[id] }
    }

    @Unroll
    void 'AutomaticIssuanceJob issues a requisition with delivery type #delivery / order type #orderType: #issued'() {
        given:
        Requisition r = staged(deliveryTypeCode: delivery, orderTypeCode: orderType)
        stubGet(r)

        when:
        issuanceJob().execute(context(requisitionId: r.id))

        then:
        1 * authService.withSystemUser(_) >> { Closure c -> c.call() }
        (issued ? 1 : 0) * stockMovementService.issueRequisition(r)

        where:
        delivery                              | orderType                    || issued
        DeliveryTypeCode.PICK_UP              | null                         || true
        DeliveryTypeCode.LOCAL_DELIVERY       | null                         || true
        DeliveryTypeCode.WILL_CALL            | null                         || true
        DeliveryTypeCode.SHIP_TO              | null                         || true
        DeliveryTypeCode.DEFAULT              | null                         || true
        DeliveryTypeCode.STOCK_TRANSFER_IBT   | null                         || true
        DeliveryTypeCode.SERVICE              | null                         || false
        null                                  | null                         || false
        DeliveryTypeCode.SERVICE              | OrderTypeCode.TRANSFER_ORDER || true
        DeliveryTypeCode.SERVICE              | OrderTypeCode.SALES_ORDER    || true
        null                                  | OrderTypeCode.PURCHASE_ORDER || false
    }

    void 'AutomaticIssuanceJob: disabled (JobUtils.shouldExecute) does nothing; so does a run during migrations'() {
        when:
        jobs('automaticIssuanceJob').enabled = enabled
        LiquibaseUtil.metaClass.static.isRunningMigrations = { -> migrating }
        issuanceJob().execute(context(requisitionId: 'r1'))
        issuanceJob().execute(context())

        then:
        0 * authService._
        0 * requisitionService._
        0 * stockMovementService._

        where:
        enabled | migrating
        false   | false
        true    | true
    }

    void 'AutomaticIssuanceJob: a missing requisition is skipped'() {
        given:
        stubGet()

        when:
        issuanceJob().execute(context(requisitionId: 'gone'))

        then:
        notThrown(Exception)
        0 * stockMovementService.issueRequisition(_)
    }

    void 'AutomaticIssuanceJob bulk mode issues every STAGED requisition of AUTOMATIC_ISSUANCE_ENABLED facilities, and one failure does not stop the batch'() {
        given:
        Location f = facility('AI-1')
        Requisition a = staged(deliveryTypeCode: DeliveryTypeCode.PICK_UP)
        Requisition b = staged(deliveryTypeCode: DeliveryTypeCode.PICK_UP)
        Requisition c = staged(deliveryTypeCode: DeliveryTypeCode.PICK_UP)
        stubGet(a, b, c)

        when:
        issuanceJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities([ActivityCode.AUTOMATIC_ISSUANCE_ENABLED]) >> [f]
        1 * requisitionService.findStagedRequisitionIds([f]) >> [a.id, b.id, c.id]
        1 * stockMovementService.issueRequisition(a)
        1 * stockMovementService.issueRequisition(b) >> { throw new IllegalStateException('cannot issue') }
        1 * stockMovementService.issueRequisition(c)
        notThrown(Exception)
    }

    void 'AutomaticIssuanceJob: no facility has the activity: the cron run is inert'() {
        when:
        issuanceJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities([ActivityCode.AUTOMATIC_ISSUANCE_ENABLED]) >> []
        0 * requisitionService._
        0 * stockMovementService._
    }

    void 'AutomaticIssuanceJob single path lets an issuance failure escape execute (no catch, unlike the bulk path)'() {
        given:
        Requisition a = staged(deliveryTypeCode: DeliveryTypeCode.PICK_UP)
        stubGet(a)
        stockMovementService.issueRequisition(a) >> { throw new IllegalStateException('cannot issue') }

        when:
        issuanceJob().execute(context(requisitionId: a.id))

        then:
        thrown(IllegalStateException)
    }

    void 'AutomaticIssuanceJob: a missing system user stops issuance'() {
        given:
        Requisition a = staged(deliveryTypeCode: DeliveryTypeCode.PICK_UP)
        stubGet(a)

        when:
        issuanceJob().execute(context(requisitionId: a.id))

        then:
        1 * authService.withSystemUser(_) >> { throw new IllegalStateException('Unable to find system user with username admin') }
        thrown(IllegalStateException)
        0 * stockMovementService.issueRequisition(_)
    }

    void 'AutomaticIssuanceJob is not concurrent'() {
        expect:
        AutomaticIssuanceJob.concurrent == false
        new AutomaticIssuanceJob().sessionRequired == false
    }
}
