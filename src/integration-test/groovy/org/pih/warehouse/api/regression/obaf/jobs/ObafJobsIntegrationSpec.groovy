package org.pih.warehouse.api.regression.obaf.jobs

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationService
import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.obaf.support.QuartzRunner
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.PutawayTypeCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.jobs.AutomaticAllocationJob
import org.pih.warehouse.jobs.AutomaticBackorderReallocationJob
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.jobs.AutomaticPutawayGenerationJob
import org.pih.warehouse.jobs.AutomaticReceiptJob
import org.pih.warehouse.jobs.AutomaticStateTransitionJob
import org.pih.warehouse.jobs.AutomaticStateTransitionPollingJob
import org.pih.warehouse.jobs.PutawayLocationReassignmentJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.Product
import org.pih.warehouse.putaway.PutawayTask
import org.pih.warehouse.putaway.PutawayTaskService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * One end-to-end run of each of OBAF's eight Quartz jobs on seeded data, FIRED THROUGH THE
 * RUNNING SCHEDULER (QuartzRunner), so the job runs on a Quartz worker thread with the session
 * handling it has in production, and asserting the data effect. Where a job runs as the system
 * user (`openboxes.system.username`, default admin), the rows it writes are checked for that
 * author. Cron triggers are paused for the duration (ObafIntegrationSpec).
 */
class ObafJobsIntegrationSpec extends ObafIntegrationSpec {

    @Autowired
    AllocationService allocationService

    @Autowired
    PutawayTaskService putawayTaskService

    static final Date SOON = new Date() + 30

    QuartzRunner.Run fire(Class jobClass, Map data = [:]) {
        return QuartzRunner.fire(scheduler(), jobClass, data)
    }

    /** A CREATED order eligible for automatic allocation, made eligible AFTER insert (so the insert queues nothing). */
    String eligibleOrder(String dc, String productId, int quantity, Map props = [:]) {
        String req = requisition(dc, [[productId: productId, quantity: quantity]], props)
        tx { Requisition r = Requisition.get(req); r.autoAllocationRequested = true; r.save(flush: true, failOnError: true) }
        return req
    }

    // ------------------------------------------------------------------ AutomaticAllocationJob

    void 'AutomaticAllocationJob (cron pass): allocates a pending order at an AUTOMATIC_ALLOCATION_ENABLED facility to PICKING'() {
        given:
        String dc = depot('JOB-AA', [ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
        String b1 = bin(dc, 'B1')
        String p = product('AA')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = eligibleOrder(dc, p, 4)

        when:
        QuartzRunner.Run run = fire(AutomaticAllocationJob)

        then:
        run.exception == null
        picks(req) == [['B1', 'L', 4, 4]]
        status(req) == RequisitionStatus.PICKING
    }

    void 'AutomaticAllocationJob (event path): inserting an eligible order queues the job, which allocates it'() {
        given:
        String dc = depot('JOB-AAE', [ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
        String b1 = bin(dc, 'B1')
        String p = product('AAE')
        stock(dc, p, [[b1, 10, 'L', SOON]])

        when: 'the order is inserted already eligible: Requisition.afterInsert -> AutomaticAllocationEvent -> triggerNow'
        String req = requisition(dc, [[productId: p, quantity: 3]], [autoAllocationRequested: true])

        then:
        eventually { status(req) == RequisitionStatus.PICKING }
        picks(req) == [['B1', 'L', 3, 3]]
    }

    void 'AutomaticAllocationJob stops after maxAttempts failed attempts, each recorded by the system user'() {
        given:
        String dc = depot('JOB-AAX', [ActivityCode.AUTOMATIC_ALLOCATION_ENABLED])
        String b1 = bin(dc, 'B1')
        String p = product('AAX')
        stock(dc, p, [[b1, 1, 'L', SOON]])
        String req = eligibleOrder(dc, p, 5)

        when:
        List<QuartzRunner.Run> runs = (1..4).collect { fire(AutomaticAllocationJob, [requisitionId: req]) }

        then:
        runs*.exception == [null] * 4
        status(req) == RequisitionStatus.CREATED
        picks(req) == []
        tx {
            Requisition r = Requisition.get(req)
            def failures = r.eventLogs.findAll { it.eventLogCode == EventLogCode.ERROR_OCCURRED && it.message.startsWith(Constants.ALLOCATION_FAILED) }
            assert failures.size() == 3
            assert failures*.createdBy*.username.unique() == ['admin']
            assert r.comments*.comment.count { it.startsWith('Max attempts (3)') } == 1
            assert r.allocationAttemptCount == 3
            true
        }
    }

    // ------------------------------------------------------------------ AutomaticBackorderReallocationJob

    /**
     * A depot that receives into a sortation bin and creates the putaways itself, with a LOCAL_DELIVERY
     * cross-dock lane (XDOCK); a sales order of {@code ordered} units of a new product, all backordered,
     * with automatic allocation requested; and an inbound of {@code delivered} units referencing it.
     * The job is disabled while the inbound is received, so neither the receipt nor the putaway
     * completion schedules it: the feature fires it itself.
     */
    Map crossDockScenario(String prefix, int ordered, int delivered) {
        config.set('openboxes.jobs.automaticBackorderReallocationJob.enabled', false)
        String dc = depot(prefix, [ActivityCode.AUTOMATED_PUTAWAY_CREATION, ActivityCode.AUTO_RECEIVING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION, ActivityCode.RECEIVE_STOCK], false)
        String xdock = bin(dc, 'XDOCK', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_LOCAL_DELIVERY])
        String p = product(prefix)
        String so = requisition(dc, [[productId: p, quantity: ordered, quantityBackordered: ordered, backorderedReasonCode: ReasonCode.BACKORDER.toString()]],
                [autoAllocationRequested: true])
        String soNumber = tx { Requisition.get(so).requestNumber }
        String shipment = inboundShipment(dc, [[productId: p, quantity: delivered, lotNumber: 'XD', backorderReference: soNumber]])
        assert fire(AutomaticReceiptJob, [shipmentId: shipment]).exception == null
        List<String> crossDockTasks = tx {
            PutawayTask.findAllByFacility(Location.get(dc)).findAll { it.putawayTypeCode == PutawayTypeCode.CROSS_DOCK }*.id
        } as List<String>
        assert crossDockTasks.size() == 1
        return [dc: dc, xdock: xdock, product: p, so: so, shipment: shipment, task: crossDockTasks[0]]
    }

    /**
     * Completes the cross-dock putaway into the lane, then re-enables and fires the job for the shipment.
     * The completion refreshes availability after commit, off this thread; the refresh is done here as
     * well so the job does not race it.
     */
    QuartzRunner.Run completeAndRelease(Map s) {
        tx { putawayTaskService.patch(s.task as String, [action: 'complete', destination: s.xdock, isDirectPutaway: true]) }
        tx { productAvailabilityService.refreshProductsAvailability(s.dc as String, [s.product as String], false) }
        return release(s)
    }

    QuartzRunner.Run release(Map s) {
        config.set('openboxes.jobs.automaticBackorderReallocationJob.enabled', true)
        return fire(AutomaticBackorderReallocationJob, [shipmentId: s.shipment])
    }

    void 'AutomaticBackorderReallocationJob releases a backordered sales order from its cross-dock lane once the cross-dock putaway is completed'() {
        given:
        Map s = crossDockScenario('JOB-BO', 6, 6)

        when: 'the cross-dock putaway is still open'
        QuartzRunner.Run early = release(s)

        then: 'the release waits'
        early.exception == null
        picks(s.so) == []
        status(s.so) == RequisitionStatus.CREATED

        when:
        QuartzRunner.Run run = completeAndRelease(s)

        then:
        run.exception == null
        picks(s.so) == [['XDOCK', 'XD', 6, 6]]
        tx { Requisition.get(s.so).requisitionItems*.quantityBackordered } == [null]
        status(s.so) == RequisitionStatus.PICKING
    }

    void 'AutomaticBackorderReallocationJob: a partial delivery allocates what arrived and leaves the rest backordered'() {
        given:
        Map s = crossDockScenario('JOB-BOP', 10, 4)

        when:
        completeAndRelease(s)

        then:
        picks(s.so) == [['XDOCK', 'XD', 4, 4]]
        tx { RequisitionItem ri = Requisition.get(s.so).requisitionItems[0]; [ri.quantityBackordered, ri.backorderedReasonCode] } ==
                [6, ReasonCode.BACKORDER.toString()]
    }

    // ------------------------------------------------------------------ AutomaticIssuanceJob

    /** A STAGED order whose picks are complete (non-mobile depot: allocation picks). */
    String stagedOrder(String dc, String productId, String binId, int quantity, DeliveryTypeCode delivery) {
        String req = requisition(dc, [[productId: productId, quantity: quantity]], [deliveryTypeCode: delivery])
        tx { allocationService.allocate(Requisition.get(req), AllocationMode.AUTO, []) }
        tx { Requisition r = Requisition.get(req); r.status = RequisitionStatus.STAGED; r.save(flush: true, failOnError: true) }
        return req
    }

    List<Map> outboundTransactions(String requisitionId) {
        return tx {
            Transaction.findAllByRequisition(Requisition.get(requisitionId)).findAll { it.transactionType?.transactionCode == TransactionCode.DEBIT }
                    .collect { [id: it.id, createdBy: it.createdBy?.username] }
        } as List<Map>
    }

    void 'AutomaticIssuanceJob issues a STAGED order as the system user: shipment shipped, stock debited, status ISSUED'() {
        given:
        config.set('openboxes.jobs.automaticIssuanceJob.enabled', true)
        String dc = depot('JOB-AI')
        String b1 = bin(dc, 'B1')
        String p = product('AI')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = stagedOrder(dc, p, b1, 4, DeliveryTypeCode.PICK_UP)

        when:
        QuartzRunner.Run run = fire(AutomaticIssuanceJob, [requisitionId: req])

        then:
        run.exception == null
        status(req) == RequisitionStatus.ISSUED
        tx { Requisition.get(req).shipment?.currentStatus } == ShipmentStatusCode.SHIPPED
        outboundTransactions(req)*.createdBy == ['admin']
        eventually(15000) { availability(dc, p)[['B1', 'L']] == [6, 6] }
    }

    void 'AutomaticIssuanceJob refuses a delivery type outside its list (SERVICE): the order stays STAGED'() {
        given:
        config.set('openboxes.jobs.automaticIssuanceJob.enabled', true)
        String dc = depot('JOB-AIS')
        String b1 = bin(dc, 'B1')
        String p = product('AIS')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = stagedOrder(dc, p, b1, 2, DeliveryTypeCode.SERVICE)

        when:
        QuartzRunner.Run run = fire(AutomaticIssuanceJob, [requisitionId: req])

        then:
        run.exception == null
        status(req) == RequisitionStatus.STAGED
        outboundTransactions(req) == []
    }

    void 'AutomaticIssuanceJob is off by default: a STAGED order is left alone'() {
        given:
        String dc = depot('JOB-AIOFF')
        String b1 = bin(dc, 'B1')
        String p = product('AIOFF')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = stagedOrder(dc, p, b1, 2, DeliveryTypeCode.PICK_UP)

        when:
        fire(AutomaticIssuanceJob, [requisitionId: req])

        then:
        status(req) == RequisitionStatus.STAGED
    }

    /**
     * The job does not check the order is still STAGED (AutomaticIssuanceJob.issueRequisition), so a
     * second run re-attempts issuance; ShipmentService.sendShipment then refuses ("Shipment has
     * already shipped"), which fails the job run but debits nothing twice.
     */
    void 'AutomaticIssuanceJob run again on an ISSUED order: refused by the shipment (job run fails), stock not debited twice'() {
        given:
        config.set('openboxes.jobs.automaticIssuanceJob.enabled', true)
        String dc = depot('JOB-AI2')
        String b1 = bin(dc, 'B1')
        String p = product('AI2')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = stagedOrder(dc, p, b1, 4, DeliveryTypeCode.PICK_UP)
        fire(AutomaticIssuanceJob, [requisitionId: req])
        assert status(req) == RequisitionStatus.ISSUED

        when:
        QuartzRunner.Run second = fire(AutomaticIssuanceJob, [requisitionId: req])

        then:
        second.exception?.cause?.message?.contains('Failed to send shipment')
        status(req) == RequisitionStatus.ISSUED
        outboundTransactions(req).size() == 1
        eventually(15000) { availability(dc, p)[['B1', 'L']] == [6, 6] }
    }

    void 'AutomaticIssuanceJob bulk pass issues STAGED orders of the allowed delivery types only'() {
        given:
        config.set('openboxes.jobs.automaticIssuanceJob.enabled', true)
        String dc = depot('JOB-AIB', [ActivityCode.AUTOMATIC_ISSUANCE_ENABLED])
        String b1 = bin(dc, 'B1')
        String p = product('AIB')
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String pickUp = stagedOrder(dc, p, b1, 2, DeliveryTypeCode.PICK_UP)
        String service = stagedOrder(dc, p, b1, 2, DeliveryTypeCode.SERVICE)

        when:
        QuartzRunner.Run run = fire(AutomaticIssuanceJob)

        then:
        run.exception == null
        status(pickUp) == RequisitionStatus.ISSUED
        status(service) == RequisitionStatus.STAGED
    }

    // ------------------------------------------------------------------ AutomaticPutawayGenerationJob

    @PendingFeature(reason = 'PutawayService.groovy:80 generatePutaways logs new JSONObject(candidate.toJson()) eagerly; PutawayItem.toJson -> Product.getDisplayName -> LocalizationUtil.getCurrentLocale needs a web request, so on the Quartz thread the job throws IllegalStateException (No thread-bound request) for any candidate: it can never generate a putaway')
    void 'AutomaticPutawayGenerationJob creates a putaway order from the receiving bin to the static slot'() {
        given:
        config.set('openboxes.jobs.automaticPutawayGenerationJob.enabled', true)
        String dc = depot('JOB-PG', [ActivityCode.STATIC_SLOTTING])
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK])
        String slot = bin(dc, 'SLOT')
        String p = product('PG')
        stock(dc, p, [[recv, 7, 'L', SOON]])
        tx {
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory,
                    preferredBinLocation: Location.get(slot)).save(flush: true, failOnError: true)
        }

        when:
        QuartzRunner.Run run = fire(AutomaticPutawayGenerationJob)

        then:
        run.exception == null
        putawayItems(dc) == [[product: p, from: 'RECV', to: 'SLOT', quantity: 7]]
        tx { putawayOrders(dc)*.orderedBy*.id.every { it } }
    }

    @PendingFeature(reason = 'PutawayService.groovy:84-86 generatePutaways RETURNS (not continue) on the first candidate without a putaway location, so every later candidate at the facility is skipped (currently masked by the PutawayService.groovy:80 web-request failure)')
    void 'AutomaticPutawayGenerationJob: a product without a slot does not stop the others getting one'() {
        given:
        config.set('openboxes.jobs.automaticPutawayGenerationJob.enabled', true)
        String dc = depot('JOB-PGX', [ActivityCode.STATIC_SLOTTING])
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK])
        String slot = bin(dc, 'SLOT')
        String unslotted = product('PGA')
        String slotted = product('PGB')
        stock(dc, unslotted, [[recv, 2, 'A', SOON]])
        stock(dc, slotted, [[recv, 3, 'B', SOON]])
        tx {
            new InventoryLevel(product: Product.get(slotted), inventory: Location.get(dc).inventory,
                    preferredBinLocation: Location.get(slot)).save(flush: true, failOnError: true)
        }

        when:
        fire(AutomaticPutawayGenerationJob)

        then:
        putawayItems(dc) == [[product: slotted, from: 'RECV', to: 'SLOT', quantity: 3]]
    }

    List<Order> putawayOrders(String dc) {
        return Order.findAllByOriginAndOrderType(Location.get(dc), OrderType.findByCode(Constants.PUTAWAY_ORDER))
    }

    List<Map> putawayItems(String dc) {
        return tx {
            putawayOrders(dc).collectMany { it.orderItems as List }.collect { OrderItem i ->
                [product: i.product.id, from: prefixOf(i.originBinLocation?.name), to: prefixOf(i.destinationBinLocation?.name), quantity: i.quantity as Integer]
            }
        } as List<Map>
    }

    // ------------------------------------------------------------------ AutomaticReceiptJob

    void 'AutomaticReceiptJob receives a shipped inbound shipment at an AUTO_RECEIVING facility into its sortation bin, as the system user'() {
        given:
        String dc = depot('JOB-AR', [ActivityCode.AUTO_RECEIVING])
        bin(dc, 'SORT', [ActivityCode.INBOUND_SORTATION])
        String p = product('AR')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 5, lotNumber: 'AR']])

        when:
        QuartzRunner.Run run = fire(AutomaticReceiptJob, [shipmentId: shipment])

        then:
        run.exception == null
        tx { Shipment.get(shipment).currentStatus } == ShipmentStatusCode.RECEIVED
        availability(dc, p)[['SORT', 'AR']][0] == 5
        tx {
            Transaction.findAllByIncomingShipment(Shipment.get(shipment)).collect { it.createdBy?.username }
        } == ['admin']
    }

    void 'AutomaticReceiptJob leaves a shipment to a facility without AUTO_RECEIVING alone'() {
        given:
        String dc = depot('JOB-ARNO')
        String p = product('ARNO')
        String shipment = inboundShipment(dc, [[productId: p, quantity: 5, lotNumber: 'AR']])

        when:
        fire(AutomaticReceiptJob, [shipmentId: shipment])

        then:
        tx { Shipment.get(shipment).currentStatus } == ShipmentStatusCode.SHIPPED
    }

    // ------------------------------------------------------------------ state transition jobs

    String fullyPickedOrder(String prefix) {
        String dc = depot(prefix)
        String b1 = bin(dc, 'B1')
        String p = product(prefix)
        stock(dc, p, [[b1, 10, 'L', SOON]])
        String req = requisition(dc, [[productId: p, quantity: 3]])
        tx { allocationService.allocate(Requisition.get(req), AllocationMode.AUTO, []) }
        tx { Requisition r = Requisition.get(req); r.status = RequisitionStatus.PICKING; r.save(flush: true, failOnError: true) }
        assert picks(req) == [['B1', 'L', 3, 3]]
        return req
    }

    void 'AutomaticStateTransitionJob moves a fully picked PICKING order to PICKED'() {
        given:
        String req = fullyPickedOrder('JOB-ST')

        when:
        QuartzRunner.Run run = fire(AutomaticStateTransitionJob, [id: req])

        then:
        run.exception == null
        status(req) == RequisitionStatus.PICKED
    }

    void 'AutomaticStateTransitionPollingJob finds PICKING orders (no facility gate) and has each transitioned'() {
        given:
        String req = fullyPickedOrder('JOB-STP')

        when:
        QuartzRunner.Run run = fire(AutomaticStateTransitionPollingJob)

        then:
        run.exception == null
        eventually { status(req) == RequisitionStatus.PICKED }
    }

    // ------------------------------------------------------------------ PutawayLocationReassignmentJob

    void 'PutawayLocationReassignmentJob (via the InventoryLevel update listener) moves a pending putaway off the UNDEFINED location onto the new preferred bin'() {
        given:
        String dc = depot('JOB-RA')
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK])
        String undefined = bin(dc, 'UNDEF', [ActivityCode.UNDEFINED_LOCATION])
        String slot = bin(dc, 'SLOT')
        String p = product('RA')
        stock(dc, p, [[recv, 4, 'L', SOON]])
        String level = tx {
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory,
                    preferredBinLocation: Location.get(undefined)).save(flush: true, failOnError: true).id
        }
        Map putaway = putawayOrder(dc, p, 'L', 4, recv, undefined)

        when: 'the level gets a real preferred bin: InventoryLevel.afterUpdate -> InventoryLevelUpdatedEvent -> triggerNow'
        tx { InventoryLevel il = InventoryLevel.get(level); il.preferredBinLocation = Location.get(slot); il.save(flush: true, failOnError: true) }

        then:
        eventually { tx { prefixOf(OrderItem.get(putaway.itemId).destinationBinLocation?.name) } == 'SLOT' }
    }

    void 'PutawayLocationReassignmentJob fired directly with the level id does the same'() {
        given:
        String dc = depot('JOB-RAD')
        String recv = bin(dc, 'RECV', [ActivityCode.RECEIVE_STOCK])
        String undefined = bin(dc, 'UNDEF', [ActivityCode.UNDEFINED_LOCATION])
        String slot = bin(dc, 'SLOT')
        String p = product('RAD')
        stock(dc, p, [[recv, 4, 'L', SOON]])
        Map putaway = putawayOrder(dc, p, 'L', 4, recv, undefined)
        String level = tx {
            new InventoryLevel(product: Product.get(p), inventory: Location.get(dc).inventory,
                    preferredBinLocation: Location.get(slot)).save(flush: true, failOnError: true).id
        }

        when:
        QuartzRunner.Run run = fire(PutawayLocationReassignmentJob, [inventoryLevelId: level])

        then:
        run.exception == null
        tx { prefixOf(OrderItem.get(putaway.itemId).destinationBinLocation?.name) } == 'SLOT'
    }
}
