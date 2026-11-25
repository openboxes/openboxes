package org.pih.warehouse.regression.obaf.jobs

import grails.testing.gorm.DataTest
import grails.util.Holders
import spock.lang.Specification
import util.LiquibaseUtil

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.User
import org.pih.warehouse.integration.StateTransitionService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.jobs.AutomaticPutawayGenerationJob
import org.pih.warehouse.jobs.AutomaticReceiptJob
import org.pih.warehouse.jobs.AutomaticStateTransitionJob
import org.pih.warehouse.jobs.AutomaticStateTransitionPollingJob
import org.pih.warehouse.jobs.JobUtils
import org.pih.warehouse.jobs.PutawayLocationReassignmentJob
import org.pih.warehouse.regression.obaf.support.JobUnitSupport
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayService
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentService

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * The other five OBAF Quartz jobs, services mocked: AutomaticPutawayGenerationJob,
 * AutomaticReceiptJob, AutomaticStateTransitionJob, AutomaticStateTransitionPollingJob and
 * PutawayLocationReassignmentJob. Config gate, data-map path, cron path, system user use.
 */
class OperationsJobsUnitSpec extends Specification implements DataTest, JobUnitSupport {

    PutawayService putawayService = Mock()
    LocationService locationService = Mock()
    AuthService authService = Mock()
    ShipmentService shipmentService = Mock()
    ReceiptService receiptService = Mock()
    StateTransitionService stateTransitionService = Mock()
    StockMovementService stockMovementService = Mock()

    void setup() {
        LiquibaseUtil.metaClass.static.isRunningMigrations = { -> false }
        ['automaticPutawayGenerationJob', 'automaticReceiptJob', 'automaticStateTransitionJob',
         'automaticStateTransitionPollingJob', 'putawayLocationReassignmentJob'].each { jobs(it).enabled = true }
    }

    void cleanup() {
        resetMetaClasses(LiquibaseUtil, Requisition, InventoryLevel, StockMovement, AutomaticStateTransitionJob)
    }

    Map jobs(String key) {
        return Holders.config.openboxes.jobs[key] as Map
    }

    // ------------------------------------------------------------------ AutomaticPutawayGenerationJob

    AutomaticPutawayGenerationJob putawayJob() {
        return new AutomaticPutawayGenerationJob(putawayService: putawayService, locationService: locationService)
    }

    void 'AutomaticPutawayGenerationJob: disabled does nothing'() {
        given:
        jobs('automaticPutawayGenerationJob').enabled = false

        when:
        putawayJob().execute(context())

        then:
        0 * locationService._
        0 * putawayService._
    }

    void 'AutomaticPutawayGenerationJob generates putaways per slotting facility for its first putaway user; a facility without one is skipped'() {
        given:
        Location withUsers = facility('SLOT-1')
        Location noUsers = facility('SLOT-2')
        Location alsoWithUsers = facility('SLOT-3')
        User first = new User(username: 'first')
        User second = new User(username: 'second')

        when:
        putawayJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities([ActivityCode.DYNAMIC_SLOTTING, ActivityCode.STATIC_SLOTTING]) >>
                [withUsers, noUsers, alsoWithUsers]
        1 * putawayService.getPutawayUsers(withUsers) >> [first, second]
        1 * putawayService.getPutawayUsers(noUsers) >> []
        1 * putawayService.getPutawayUsers(alsoWithUsers) >> [second]
        1 * putawayService.generatePutaways(withUsers, first)
        0 * putawayService.generatePutaways(noUsers, _)
        1 * putawayService.generatePutaways(alsoWithUsers, second)
    }

    /**
     * The job itself never reads defaultPutawayAssignee: PutawayService.getPutawayUsers falls back to
     * it only when no user holds ROLE_PUTAWAY (a role no migration seeds). Corrects surface map section 5.
     */
    void 'AutomaticPutawayGenerationJob takes the first user getPutawayUsers answers, whatever defaultPutawayAssignee says'() {
        given:
        jobs('automaticPutawayGenerationJob').defaultPutawayAssignee = 'someone-else'
        Location f = facility('SLOT')
        User first = new User(username: 'first')

        when:
        putawayJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities(_) >> [f]
        1 * putawayService.getPutawayUsers(f) >> [first]
        1 * putawayService.generatePutaways(f, first)
    }

    // ------------------------------------------------------------------ AutomaticReceiptJob

    AutomaticReceiptJob receiptJob() {
        return new AutomaticReceiptJob(authService: authService, shipmentService: shipmentService, receiptService: receiptService,
                locationService: locationService)
    }

    void 'AutomaticReceiptJob: disabled does nothing'() {
        given:
        jobs('automaticReceiptJob').enabled = false

        when:
        receiptJob().execute(context(shipmentId: 's1'))
        receiptJob().execute(context())

        then:
        0 * authService._
        0 * receiptService._
        0 * locationService._
    }

    void 'AutomaticReceiptJob receives one shipment as the system user and skips the bulk pass'() {
        given:
        Shipment s = new Shipment(); s.id = 's1'

        when:
        receiptJob().execute(context(shipmentId: 's1'))

        then:
        1 * authService.withSystemUser(_) >> { Closure c -> c.call() }
        1 * shipmentService.getShipmentInstance('s1') >> s
        1 * receiptService.receiveInboundShipment(s)
        0 * receiptService.receiveInboundShipments(_)
        0 * locationService._
    }

    void 'AutomaticReceiptJob: a missing shipment or a receipt failure is swallowed'() {
        when:
        receiptJob().execute(context(shipmentId: 'missing'))
        receiptJob().execute(context(shipmentId: 's1'))

        then:
        2 * authService.withSystemUser(_) >> { Closure c -> c.call() }
        1 * shipmentService.getShipmentInstance('missing') >> null
        1 * shipmentService.getShipmentInstance('s1') >> new Shipment()
        1 * receiptService.receiveInboundShipment(_) >> { throw new IllegalStateException('boom') }
        notThrown(Exception)
    }

    void 'AutomaticReceiptJob cron pass receives every AUTO_RECEIVING facility, NOT as the system user'() {
        given:
        Location f1 = facility('AR-1')
        Location f2 = facility('AR-2')

        when:
        receiptJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities([ActivityCode.AUTO_RECEIVING]) >> [f1, f2]
        1 * receiptService.receiveInboundShipments(f1)
        1 * receiptService.receiveInboundShipments(f2)
        0 * authService.withSystemUser(_)
    }

    void 'AutomaticReceiptJob: no facility has the activity: the cron run is inert'() {
        when:
        receiptJob().execute(context())

        then:
        1 * locationService.getLocationsSupportingActivities([ActivityCode.AUTO_RECEIVING]) >> []
        0 * receiptService._
    }

    // ------------------------------------------------------------------ AutomaticStateTransitionJob

    AutomaticStateTransitionJob transitionJob() {
        return new AutomaticStateTransitionJob(stateTransitionService: stateTransitionService)
    }

    void 'AutomaticStateTransitionJob: disabled does nothing; no cron trigger is declared'() {
        given:
        jobs('automaticStateTransitionJob').enabled = false

        when:
        transitionJob().execute(context(id: 'r1'))

        then:
        0 * stateTransitionService._
    }

    void 'AutomaticStateTransitionJob builds the stock movement for the requisition and asks for a status update'() {
        given:
        Requisition r = new Requisition(); r.id = 'r1'
        StockMovement sm = new StockMovement(identifier: 'SM-1')
        Requisition.metaClass.static.get = { Serializable id -> id == 'r1' ? r : null }
        StockMovement.metaClass.static.createFromRequisition = { Requisition q, Boolean items -> q.is(r) && items ? sm : null }

        when:
        transitionJob().execute(context(id: 'r1'))

        then:
        1 * stateTransitionService.triggerStockMovementStatusUpdate(sm)
    }

    void 'AutomaticStateTransitionJob: an unknown requisition is skipped'() {
        given:
        Requisition.metaClass.static.get = { Serializable id -> null }

        when:
        transitionJob().execute(context(id: 'nope'))

        then:
        0 * stateTransitionService._
    }

    // ------------------------------------------------------------------ AutomaticStateTransitionPollingJob

    void 'AutomaticStateTransitionPollingJob triggers a state transition for every PICKING outbound movement, with no facility or activity gate'() {
        given:
        List<Map> triggered = recordTriggerNow(AutomaticStateTransitionJob)
        AutomaticStateTransitionPollingJob job = new AutomaticStateTransitionPollingJob(stockMovementService: stockMovementService)

        when:
        job.execute(context())

        then:
        1 * stockMovementService.getOutboundStockMovements({ StockMovement c -> c.requisitionStatusCodes == [RequisitionStatus.PICKING] }, [:]) >>
                [new StockMovement(id: 'm1'), new StockMovement(id: 'm2')]
        0 * locationService._
        triggered == [[id: 'm1'], [id: 'm2']]
    }

    void 'AutomaticStateTransitionPollingJob: disabled does nothing'() {
        given:
        jobs('automaticStateTransitionPollingJob').enabled = false
        List<Map> triggered = recordTriggerNow(AutomaticStateTransitionJob)

        when:
        new AutomaticStateTransitionPollingJob(stockMovementService: stockMovementService).execute(context())

        then:
        0 * stockMovementService._
        triggered == []
    }

    // ------------------------------------------------------------------ PutawayLocationReassignmentJob

    PutawayLocationReassignmentJob reassignJob() {
        return new PutawayLocationReassignmentJob(putawayService: putawayService, locationService: locationService)
    }

    Map reassignScenario(Map opts = [:]) {
        Location dc = facility('DC')
        Location undefined = bin(dc, 'UNDEFINED', [ActivityCode.UNDEFINED_LOCATION])
        Location preferred = opts.containsKey('preferred') ? (opts.preferred ?: null) as Location : bin(dc, 'PREFERRED')
        Product product = product('RA')
        InventoryLevel level = new InventoryLevel(product: product, inventory: new Inventory(warehouse: dc),
                preferredBinLocation: preferred, internalLocation: opts.internal)
        level.id = 'il-1'
        InventoryLevel.metaClass.static.read = { Serializable id -> id == 'il-1' ? level : null }
        return [dc: dc, undefined: undefined, preferred: preferred, product: product, level: level]
    }

    void 'PutawayLocationReassignmentJob moves pending items off UNDEFINED onto the new preferred bin, sparing completed/canceled items, through split items'() {
        given:
        Map s = reassignScenario()
        Location elsewhere = bin(s.dc, 'ELSEWHERE')
        PutawayItem pending = new PutawayItem(putawayLocation: s.undefined, putawayStatus: PutawayStatus.PENDING)
        PutawayItem done = new PutawayItem(putawayLocation: s.undefined, putawayStatus: PutawayStatus.COMPLETED)
        PutawayItem canceled = new PutawayItem(putawayLocation: s.undefined, putawayStatus: PutawayStatus.CANCELED)
        PutawayItem split = new PutawayItem(putawayLocation: s.undefined, putawayStatus: PutawayStatus.PENDING)
        PutawayItem parentDone = new PutawayItem(putawayLocation: s.undefined, putawayStatus: PutawayStatus.COMPLETED, splitItems: [split])
        PutawayItem notUndefined = new PutawayItem(putawayLocation: elsewhere, putawayStatus: PutawayStatus.PENDING)
        Putaway putaway = new Putaway(putawayItems: [pending, done, canceled, parentDone, notUndefined])

        when:
        reassignJob().execute(context(inventoryLevelId: 'il-1'))

        then:
        1 * locationService.getLocationsSupportingActivity(ActivityCode.UNDEFINED_LOCATION) >> [s.undefined]
        1 * putawayService.getPendingPutawayOrders(s.product, [s.undefined], s.dc) >> [putaway]
        1 * putawayService.savePutaway(putaway)
        pending.putawayLocation == s.preferred
        done.putawayLocation == s.undefined
        canceled.putawayLocation == s.undefined
        parentDone.putawayLocation == s.undefined
        split.putawayLocation == s.preferred
        notUndefined.putawayLocation == elsewhere
    }

    void 'PutawayLocationReassignmentJob uses the internal location when the level has no preferred bin'() {
        given:
        Location dc = facility('DC2')
        Location internal = bin(dc, 'INTERNAL')
        Map s = reassignScenario(preferred: false, internal: internal)
        s.level.preferredBinLocation = null
        PutawayItem pending = new PutawayItem(putawayLocation: s.undefined, putawayStatus: PutawayStatus.PENDING)
        locationService.getLocationsSupportingActivity(_) >> [s.undefined]
        putawayService.getPendingPutawayOrders(*_) >> [new Putaway(putawayItems: [pending])]

        when:
        reassignJob().execute(context(inventoryLevelId: 'il-1'))

        then:
        pending.putawayLocation == internal
    }

    void 'PutawayLocationReassignmentJob skips a level whose preferred bin or internal location is still UNDEFINED'() {
        given:
        Location dc = facility('DC3')
        Location undefined = bin(dc, 'U', [ActivityCode.UNDEFINED_LOCATION])
        Map s = reassignScenario(preferred: field == 'preferred' ? undefined : false, internal: field == 'internal' ? undefined : null)
        if (field == 'internal') {
            s.level.preferredBinLocation = null
        }

        when:
        reassignJob().execute(context(inventoryLevelId: 'il-1'))

        then:
        0 * putawayService._
        0 * locationService._

        where:
        field << ['preferred', 'internal']
    }

    void 'PutawayLocationReassignmentJob: disabled, a run during migrations or an unknown level does nothing'() {
        given:
        reassignScenario()
        jobs('putawayLocationReassignmentJob').enabled = enabled
        LiquibaseUtil.metaClass.static.isRunningMigrations = { -> migrating }

        when:
        reassignJob().execute(context(inventoryLevelId: levelId))

        then:
        0 * putawayService._
        0 * locationService._

        where:
        enabled | migrating | levelId
        false   | false     | 'il-1'
        true    | true      | 'il-1'
        true    | false     | 'unknown'
    }

    void 'job key and trigger metadata'() {
        expect:
        JobUtils.getCronName(AutomaticPutawayGenerationJob) == 'automaticPutawayGenerationJobCronTrigger'
        JobUtils.getCronName(PutawayLocationReassignmentJob) == 'putawayLocationReassignmentJobCronTrigger'
        [AutomaticPutawayGenerationJob, AutomaticReceiptJob, AutomaticStateTransitionJob, AutomaticStateTransitionPollingJob,
         PutawayLocationReassignmentJob].every { it.newInstance().sessionRequired == false }
    }
}
