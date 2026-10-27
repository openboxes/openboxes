package org.pih.warehouse.regression.obaf.jobs

import grails.testing.gorm.DataTest
import grails.util.Holders
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AutomaticAllocationEvent
import org.pih.warehouse.allocation.AutomaticAllocationEventService
import org.pih.warehouse.allocation.AutomaticBackorderAllocationEventService
import org.pih.warehouse.allocation.AutomaticBackorderReallocationEvent
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.RequisitionEvent
import org.pih.warehouse.core.RequisitionEventService
import org.pih.warehouse.core.WebhookEventType
import org.pih.warehouse.core.WebhookPublisherService
import org.pih.warehouse.integration.StateTransitionService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryLevelUpdatedEvent
import org.pih.warehouse.inventory.InventoryLevelUpdatedEventService
import org.pih.warehouse.inventory.InventorySnapshotService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.RefreshPicklistStatusEvent
import org.pih.warehouse.inventory.RefreshPicklistStatusEventService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.jobs.AutomaticAllocationJob
import org.pih.warehouse.jobs.AutomaticBackorderReallocationJob
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.jobs.AutomaticStateTransitionJob
import org.pih.warehouse.jobs.PutawayLocationReassignmentJob
import org.pih.warehouse.regression.obaf.support.JobUnitSupport
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * The listeners that put work on the job queue: AutomaticAllocationEventService,
 * AutomaticBackorderAllocationEventService, RequisitionEventService (STAGED -> issuance),
 * RefreshPicklistStatusEventService, InventoryLevelUpdatedEventService; plus
 * StateTransitionService (what AutomaticStateTransitionJob calls). Job statics are recorded, not run.
 */
class JobSchedulingListenersUnitSpec extends Specification implements DataTest, JobUnitSupport {

    void setupSpec() {
        mockDomain Requisition
        mockDomain Shipment
    }

    void setup() {
        Holders.config.openboxes.jobs.automaticAllocationJob.enabled = true
        Holders.config.openboxes.jobs.automaticBackorderReallocationJob.enabled = true
        Holders.config.openboxes.jobs.automaticIssuanceJob.delayInMilliseconds = 5000
        Holders.config.openboxes.jobs.automaticBackorderReallocationJob.delayInMilliseconds = 6000
    }

    void cleanup() {
        resetMetaClasses(Requisition, Shipment, AutomaticAllocationJob, AutomaticBackorderReallocationJob,
                AutomaticIssuanceJob, AutomaticStateTransitionJob, PutawayLocationReassignmentJob)
    }

    static long now() { System.currentTimeMillis() }

    // ------------------------------------------------------------------ AutomaticAllocationEventService

    Requisition eligibleRequisition() {
        Requisition r = new Requisition(status: RequisitionStatus.CREATED, autoAllocationRequested: true,
                origin: facility('AA', [ActivityCode.AUTOMATIC_ALLOCATION_ENABLED]))
        r.id = 'r1'
        r.requisitionItems = [new RequisitionItem(quantity: 1)] as Set
        Requisition.metaClass.static.get = { Serializable id -> id == 'r1' ? r : null }
        return r
    }

    void 'AutomaticAllocationEventService triggers the allocation job for an eligible requisition'() {
        given:
        eligibleRequisition()
        List<Map> triggered = recordTriggerNow(AutomaticAllocationJob)

        when:
        new AutomaticAllocationEventService().onAutomaticAllocationEvent(new AutomaticAllocationEvent('r1'))

        then:
        triggered == [[requisitionId: 'r1']]
    }

    @Unroll
    void 'AutomaticAllocationEventService does not trigger when #why'() {
        given:
        Requisition r = eligibleRequisition()
        mutate.call(r)
        List<Map> triggered = recordTriggerNow(AutomaticAllocationJob)

        when:
        new AutomaticAllocationEventService().onAutomaticAllocationEvent(new AutomaticAllocationEvent(id))

        then:
        triggered == []

        cleanup:
        Holders.config.openboxes.jobs.automaticAllocationJob.enabled = true

        where:
        why                         | id   | mutate
        'the job is disabled'       | 'r1' | { q -> Holders.config.openboxes.jobs.automaticAllocationJob.enabled = false }
        'the requisition is gone'   | 'r2' | { q -> }
        'it is not CREATED'         | 'r1' | { q -> q.status = RequisitionStatus.VERIFYING }
        'it is not flagged'         | 'r1' | { q -> q.autoAllocationRequested = null }
        'the facility does not opt in' | 'r1' | { q -> q.origin.supportedActivities.clear() }
    }

    // ------------------------------------------------------------------ AutomaticBackorderAllocationEventService

    AutomaticBackorderAllocationEventService backorderListener() {
        AutomaticBackorderAllocationEventService s = new AutomaticBackorderAllocationEventService()
        s.grailsApplication = grailsApplication
        return s
    }

    Shipment shipmentReceived(boolean fully) {
        ShipmentItem item = Spy(ShipmentItem) { isFullyReceived() >> fully }
        Shipment s = new Shipment(shipmentItems: [item] as Set)
        Shipment.metaClass.static.get = { Serializable id -> id == 'ship-1' ? s : null }
        return s
    }

    void 'AutomaticBackorderAllocationEventService schedules the delayed reallocation job once the shipment is fully received'() {
        given:
        shipmentReceived(true)
        List<Map> scheduled = recordSchedule(AutomaticBackorderReallocationJob)
        long before = now()

        when:
        backorderListener().onAutomaticBackorderReallocationEvent(new AutomaticBackorderReallocationEvent('ship-1'))

        then:
        scheduled.size() == 1
        scheduled[0].params == [shipmentId: 'ship-1']
        scheduled[0].runAt.time >= before + 6000
        scheduled[0].runAt.time <= now() + 6000
    }

    @Unroll
    void 'AutomaticBackorderAllocationEventService does not trigger when #why'() {
        given:
        shipmentReceived(fully)
        Holders.config.openboxes.jobs.automaticBackorderReallocationJob.enabled = enabled
        List<Map> scheduled = recordSchedule(AutomaticBackorderReallocationJob)

        when:
        backorderListener().onAutomaticBackorderReallocationEvent(new AutomaticBackorderReallocationEvent(id))

        then:
        scheduled == []

        cleanup:
        Holders.config.openboxes.jobs.automaticBackorderReallocationJob.enabled = true

        where:
        why                              | enabled | fully | id
        'the job is disabled'            | false   | true  | 'ship-1'
        'the shipment is gone'           | true    | true  | 'ship-2'
        'the shipment is partly received'| true    | false | 'ship-1'
    }

    // ------------------------------------------------------------------ RequisitionEventService

    RequisitionEventService requisitionEvents(WebhookPublisherService webhooks) {
        RequisitionEventService s = new RequisitionEventService()
        s.grailsApplication = grailsApplication
        s.webhookPublisherService = webhooks
        return s
    }

    void 'RequisitionEventService: STAGED schedules the issuance job after the issuance delay and publishes the webhook'() {
        given:
        Requisition r = eligibleRequisition()
        grailsApplication.config.openboxes.jobs.automaticIssuanceJob.delayInMilliseconds = 5000
        WebhookPublisherService webhooks = Mock()
        List<Map> scheduled = recordSchedule(AutomaticIssuanceJob)
        long before = now()

        when:
        requisitionEvents(webhooks).onRequisitionEvent(new RequisitionEvent('r1', WebhookEventType.REQUISITION_STAGED))

        then:
        1 * webhooks.publishRequisitionEvent(r, WebhookEventType.REQUISITION_STAGED)
        scheduled.size() == 1
        scheduled[0].params == [requisitionId: 'r1']
        scheduled[0].runAt.time >= before + 5000
        scheduled[0].runAt.time <= now() + 5000
    }

    void 'RequisitionEventService schedules issuance on STAGED even while the issuance job is disabled (the job then no-ops)'() {
        given:
        eligibleRequisition()
        Holders.config.openboxes.jobs.automaticIssuanceJob.enabled = false
        List<Map> scheduled = recordSchedule(AutomaticIssuanceJob)

        when:
        requisitionEvents(Mock(WebhookPublisherService)).onRequisitionEvent(new RequisitionEvent('r1', WebhookEventType.REQUISITION_STAGED))

        then:
        scheduled.size() == 1
    }

    void 'RequisitionEventService: CREATED only publishes the webhook; other events and unknown requisitions do nothing'() {
        given:
        Requisition r = eligibleRequisition()
        WebhookPublisherService webhooks = Mock()
        List<Map> scheduled = recordSchedule(AutomaticIssuanceJob)
        RequisitionEventService s = requisitionEvents(webhooks)

        when:
        s.onRequisitionEvent(new RequisitionEvent('r1', WebhookEventType.REQUISITION_CREATED))
        s.onRequisitionEvent(new RequisitionEvent('r1', WebhookEventType.REQUISITION_ISSUED))
        s.onRequisitionEvent(new RequisitionEvent('gone', WebhookEventType.REQUISITION_STAGED))

        then:
        1 * webhooks.publishRequisitionEvent(r, WebhookEventType.REQUISITION_CREATED)
        0 * webhooks._
        scheduled == []
    }

    // ------------------------------------------------------------------ RefreshPicklistStatusEventService

    void 'RefreshPicklistStatusEventService triggers the state transition job'() {
        given:
        List<Map> triggered = recordTriggerNow(AutomaticStateTransitionJob)

        when:
        new RefreshPicklistStatusEventService().onRefreshPicklistStatusEvent(new RefreshPicklistStatusEvent('r1'))

        then:
        triggered == [[id: 'r1']]
    }

    // ------------------------------------------------------------------ InventoryLevelUpdatedEventService

    void 'InventoryLevelUpdatedEventService refreshes availability and snapshot for a facility-scoped update and triggers reassignment'() {
        given:
        ProductAvailabilityService pa = Mock()
        InventorySnapshotService snapshots = Mock()
        InventoryLevelUpdatedEventService s = new InventoryLevelUpdatedEventService(productAvailabilityService: pa, inventorySnapshotService: snapshots)
        InventoryLevel level = new InventoryLevel(); level.id = 'il-1'
        List<Map> triggered = recordTriggerNow(PutawayLocationReassignmentJob)

        when:
        s.onInventoryLevelUpdated(new InventoryLevelUpdatedEvent(level, 'fac-1', ['prod-1'], true))

        then:
        1 * pa.triggerRefreshProductAvailability('fac-1', ['prod-1'], true)
        1 * snapshots.triggerRefreshInventorySnapshot('fac-1', ['prod-1'], true)
        triggered == [[inventoryLevelId: 'il-1']]
    }

    void 'InventoryLevelUpdatedEventService from the domain hook (no facility) only triggers reassignment'() {
        given:
        ProductAvailabilityService pa = Mock()
        InventorySnapshotService snapshots = Mock()
        InventoryLevelUpdatedEventService s = new InventoryLevelUpdatedEventService(productAvailabilityService: pa, inventorySnapshotService: snapshots)
        InventoryLevel level = new InventoryLevel(); level.id = 'il-2'
        List<Map> triggered = recordTriggerNow(PutawayLocationReassignmentJob)

        when:
        s.onInventoryLevelUpdated(new InventoryLevelUpdatedEvent(level))

        then:
        0 * pa._
        0 * snapshots._
        triggered == [[inventoryLevelId: 'il-2']]
    }

    // ------------------------------------------------------------------ StateTransitionService

    StockMovement movementWith(RequisitionStatus status, List<Map> picks) {
        Requisition r = new Requisition(status: status)
        if (picks != null) {
            r.picklist = new Picklist(picklistItems: picks.collect { new PicklistItem(it) } as Set)
        }
        return new StockMovement(identifier: 'SM', requisition: r)
    }

    @Unroll
    void 'StateTransitionService: #why -> transition to PICKED: #expected'() {
        given:
        StockMovementService sms = Mock()
        StateTransitionService s = new StateTransitionService(stockMovementService: sms)
        StockMovement sm = movementWith(status, picks)

        when:
        s.triggerStockMovementStatusUpdate(sm)

        then:
        (expected ? 1 : 0) * sms.transitionRequisitionBasedStockMovement(sm, { it.status == StockMovementStatusCode.PICKED })

        where:
        why                               | status                     | picks                                                       || expected
        'PICKING, every pick done'        | RequisitionStatus.PICKING  | [[quantity: 2, quantityPicked: 2], [quantity: 1, quantityPicked: 1]] || true
        'PICKING, one pick outstanding'   | RequisitionStatus.PICKING  | [[quantity: 2, quantityPicked: 2], [quantity: 1, quantityPicked: 0]] || false
        'PICKING, empty picklist'         | RequisitionStatus.PICKING  | []                                                          || false
        'PICKING, no picklist'            | RequisitionStatus.PICKING  | null                                                        || false
        'already PICKED'                  | RequisitionStatus.PICKED   | [[quantity: 2, quantityPicked: 2]]                          || false
        'still CREATED'                   | RequisitionStatus.CREATED  | [[quantity: 2, quantityPicked: 2]]                          || false
    }

    void 'StateTransitionService tolerates a stock movement without a requisition'() {
        given:
        StockMovementService sms = Mock()

        when:
        new StateTransitionService(stockMovementService: sms).triggerStockMovementStatusUpdate(new StockMovement())

        then:
        0 * sms._
        notThrown(Exception)
    }
}
