package org.pih.warehouse.api.regression.obaf.support

import grails.core.GrailsApplication
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticBackorderReallocationJob
import org.pih.warehouse.jobs.AutomaticReceiptJob
import org.quartz.CronTrigger
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.matchers.EverythingMatcher
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.listeners.TriggerListenerSupport
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.User
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.api.regression.support.ConfigOverride
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Address
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.RecordInventoryCommand
import org.pih.warehouse.inventory.RefreshProductAvailabilityEventService
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType

/**
 * Base of the OBAF allocation / backorder / issuance / job integration specs (no HTTP).
 *
 * Data is real and committed (the database is shared by the whole run and never cleaned), each
 * feature builds its own depot, bins, products and orders with unique names, through GORM in
 * new transactions; stock is recorded with InventoryService.saveRecordInventoryCommand, the
 * service behind POST /api/facilities/{id}/inventories (what MobileContractSpec.stock calls over
 * HTTP), followed by the same synchronous availability refresh the API controller runs.
 *
 * The Quartz scheduler IS running in the integration app (BootStrap starts it). While a spec of
 * this family runs, every CRON trigger is paused (and resumed in cleanupSpec) so the five-minute
 * polling jobs cannot act on the fixtures mid-feature; triggerNow/schedule triggers still fire,
 * which is what the listener specs rely on. Fixtures are built so that no listener queues work
 * the feature did not ask for (e.g. autoAllocationRequested=false on insert).
 *
 * Depot creation defers the availability refresh exactly like MobileContractSpec.regressionDc (the
 * synchronous refresh fires inside Location.afterInsert before the Inventory row exists).
 */
abstract class ObafIntegrationSpec extends IntegrationSpec {

    static final String BIN_TYPE = 'cab2b4f35ba2d867015ba2e17e390001'

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    InventoryService inventoryService

    @Autowired
    AuthService authService

    @Autowired
    ShipmentService shipmentService

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Autowired
    RefreshProductAvailabilityEventService refreshProductAvailabilityEventService

    ConfigOverride config

    private static final List<TriggerKey> pausedCronTriggers = []

    void setup() {
        config = new ConfigOverride(grailsApplication)
        pauseCronTriggers()
        vetoEventScheduledJobs()
        actAs('admin')
    }

    void cleanup() {
        config?.restore()
        actAs(null)
    }

    /**
     * Sets the AuthService thread-local user for this (the test) thread, as a signed-in web request
     * would: audited rows (comments, event logs, cycle count requests) need one. null clears it.
     */
    void actAs(String username) {
        actingAs = username
        if (!username) {
            Requisition.withNewTransaction { authService.setCurrentUser(null) }
        }
    }

    /** Username {@link #tx} binds as the current user at the start of each transaction (null: none). */
    String actingAs

    /** Runs {@code c} with a mock web request bound (services that render links/messages need one). */
    def <T> T withWebRequest(Closure<T> c) {
        grails.util.GrailsWebMockUtil.bindMockWebRequest(Holders.applicationContext)
        try {
            return c.call()
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes()
        }
    }

    void cleanupSpec() {
        resumeCronTriggers()
    }

    // ------------------------------------------------------------------ Quartz

    static Scheduler scheduler() {
        return Holders.applicationContext.getBean('quartzScheduler', Scheduler)
    }

    static synchronized void pauseCronTriggers() {
        if (pausedCronTriggers) {
            return
        }
        Scheduler s = scheduler()
        s.getTriggerKeys(GroupMatcher.anyTriggerGroup()).each { TriggerKey key ->
            Trigger t = s.getTrigger(key)
            if (t instanceof CronTrigger && s.getTriggerState(key) != Trigger.TriggerState.PAUSED) {
                s.pauseTrigger(key)
                pausedCronTriggers << key
            }
        }
    }

    /**
     * Jobs that application events schedule a second after a shipment changes. Run by the scheduler on its own, they
     * would race the feature that changed the shipment - for example receive the shipment it is about to receive
     * itself. Only the runs a feature fires through {@link QuartzRunner} are let through.
     */
    static final List<Class> EVENT_SCHEDULED_JOBS = [AutomaticReceiptJob, AutomaticBackorderReallocationJob]

    static final String EVENT_JOB_VETO = 'regression-event-scheduled-job-veto'

    /**
     * Installs, once per JVM, a trigger listener that vetoes every run of the {@link #EVENT_SCHEDULED_JOBS} not fired
     * by {@link QuartzRunner}. Quartz asks it on the worker thread just before the job runs, so a run scheduled at
     * any time (by an event, a resumed cron trigger or an earlier spec) is stopped.
     */
    static synchronized void vetoEventScheduledJobs() {
        Scheduler s = scheduler()
        if (s.listenerManager.getTriggerListener(EVENT_JOB_VETO)) {
            return
        }
        Set<String> jobNames = EVENT_SCHEDULED_JOBS*.name as Set
        s.listenerManager.addTriggerListener(new TriggerListenerSupport() {
            @Override
            String getName() { return EVENT_JOB_VETO }

            @Override
            boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
                return context.jobDetail.key.name in jobNames && !context.mergedJobDataMap.containsKey(QuartzRunner.RUN_ID)
            }
        }, EverythingMatcher.allTriggers())
    }

    static synchronized void resumeCronTriggers() {
        Scheduler s = scheduler()
        pausedCronTriggers.each { s.resumeTrigger(it) }
        pausedCronTriggers.clear()
    }

    // ------------------------------------------------------------------ transactions

    static String uid() {
        return Long.toString(System.nanoTime(), 36).toUpperCase()
    }

    /**
     * A new transaction (and session) on this thread. The current user (see {@link #actAs}) is
     * re-read in this session, as a web request's user is in its own session.
     */
    def <T> T tx(Closure<T> c) {
        return Requisition.withNewTransaction {
            authService.setCurrentUser(actingAs ? User.findByUsername(actingAs) : null)
            c.call()
        } as T
    }

    def <T> T withDeferredAvailabilityRefresh(Closure<T> c) {
        Boolean before = refreshProductAvailabilityEventService.alwaysSynchronous
        refreshProductAvailabilityEventService.alwaysSynchronous = false
        try {
            return c.call()
        } finally {
            refreshProductAvailabilityEventService.alwaysSynchronous = before
        }
    }

    // ------------------------------------------------------------------ locations, products, stock

    /** A depot with the depot type's activities plus {@code extra}, minus {@code without}. */
    String depot(String prefix, Collection<ActivityCode> extra = [], Collection<ActivityCode> without = []) {
        String name = "${prefix}-${uid()}".toString()
        return withDeferredAvailabilityRefresh {
            tx {
                LocationType depotType = LocationType.get('2')
                Location l = new Location(name: name, locationNumber: name, locationType: depotType,
                        organization: Location.get('1').organization, active: true,
                        address: new Address(address: '1 Dock Road', city: 'Boston', stateOrProvince: 'MA', postalCode: '02110', country: 'US'))
                Set<String> activities = ((depotType.supportedActivities ?: []) as Set<String>) + (extra*.id as Set<String>)
                activities.removeAll(without*.id)
                l.supportedActivities = activities
                l.inventory = new Inventory(warehouse: l)
                l.save(flush: true, failOnError: true)
                return l.id
            }
        } as String
    }

    /**
     * A bin under the depot. Its activities REPLACE the bin type's, so PICK_STOCK and
     * PUTAWAY_STOCK are added unless {@code allocable} is false.
     */
    String bin(String depotId, String prefix, Collection<ActivityCode> activities = [], boolean allocable = true) {
        String name = "${prefix}-${uid()}".toString()
        return tx {
            Set<String> ids = activities*.id as Set<String>
            if (allocable) {
                ids << ActivityCode.PICK_STOCK.id
                ids << ActivityCode.PUTAWAY_STOCK.id
            }
            Location b = new Location(name: name, locationNumber: name, locationType: LocationType.get(BIN_TYPE),
                    parentLocation: Location.get(depotId), active: true)
            b.supportedActivities = ids
            b.save(flush: true, failOnError: true)
            return b.id
        } as String
    }

    String product(String prefix) {
        String code = "${prefix}-${uid()}".toString()
        return tx {
            Category category = Category.findByName('Test OBAF') ?:
                    new Category(name: 'Test OBAF', description: 'regression test fixtures').save(flush: true, failOnError: true)
            Product p = new Product(name: "Test ${code}".toString(), productCode: code, category: category,
                    productType: ProductType.get('DEFAULT'), unitOfMeasure: 'each', active: true)
            p.save(flush: true, failOnError: true)
            return p.id
        } as String
    }

    /** Record stock for one product: rows are [binId, quantity] or [binId, quantity, lotNumber, expirationDate]. */
    void stock(String depotId, String productId, List<List> rows) {
        tx {
            Location facility = Location.get(depotId)
            RecordInventoryCommandTestBuilder b = new RecordInventoryCommandTestBuilder()
                    .product(Product.get(productId)).inventory(facility.inventory).transactionDateNow()
            rows.each { List r ->
                b.row(r.size() > 2 ? r[2] as String : null, r.size() > 3 ? r[3] as Date : null, Location.get(r[0] as String), r[1] as int)
            }
            RecordInventoryCommand cmd = b.build()
            // record stock reads the facility from the session thread-local, like the web request does
            authService.setCurrentLocation(facility)
            authService.setCurrentUser(User.findByUsername('admin'))
            try {
                inventoryService.saveRecordInventoryCommand(cmd, [:])
            } finally {
                authService.setCurrentLocation(null)
            }
            assert !cmd.hasErrors(): "record stock failed: ${cmd.errors.allErrors.collect { "${it.code} ${it.defaultMessage}" }} ${cmd.recordInventoryRows.collect { it.errors?.allErrors?.collect { e -> e.code } }}"
        }
        tx { productAvailabilityService.refreshProductsAvailability(depotId, [productId], false) }
    }

    String lotId(String productId, String lotNumber) {
        return tx { InventoryItem.findByProductAndLotNumber(Product.get(productId), lotNumber)?.id } as String
    }

    // ------------------------------------------------------------------ orders

    private static String destinationId

    String destination() {
        if (!destinationId) {
            destinationId = depot('TEST-OBAF-DEST')
        }
        return destinationId
    }

    /**
     * A requisition (outbound order) from the depot; {@code lines} are maps with productId and
     * quantity plus any RequisitionItem property. Inserted with autoAllocationRequested=false
     * unless overridden, so the insert does not queue the allocation job.
     */
    String requisition(String depotId, List<Map> lines, Map props = [:]) {
        String destinationDepotId = destination()
        return tx {
            String n = uid()
            Requisition r = new Requisition([
                    name                   : "Test obaf ${n}".toString(),
                    requestNumber          : "LO${n}".toString(),
                    origin                 : Location.get(depotId),
                    destination            : Location.get(destinationDepotId),
                    requestedBy            : Person.get('1'),
                    dateRequested          : new Date(),
                    requestedDeliveryDate  : new Date() + 1,
                    status                 : RequisitionStatus.CREATED,
                    type                   : RequisitionType.DEFAULT,
                    deliveryTypeCode       : DeliveryTypeCode.LOCAL_DELIVERY,
                    autoAllocationRequested: false,
            ] + props)
            lines.eachWithIndex { Map line, int i ->
                Map p = new LinkedHashMap(line)
                String productId = p.remove('productId')
                RequisitionItem ri = new RequisitionItem([product: Product.get(productId), quantity: p.quantity,
                                                          quantityApproved: p.quantity, substitutable: false, orderIndex: i] + p)
                r.addToRequisitionItems(ri)
            }
            r.save(flush: true, failOnError: true)
            return r.id
        } as String
    }

    List<String> itemIds(String requisitionId) {
        return tx { Requisition.get(requisitionId).requisitionItems.sort { it.orderIndex }*.id } as List<String>
    }

    /** The picks of a requisition as [binName-prefix, lotNumber, quantity, quantityPicked], sorted. */
    List<List> picks(String requisitionId) {
        return tx {
            PicklistItem.createCriteria().list {
                requisitionItem { requisition { eq('id', requisitionId) } }
            }.collect { PicklistItem pi ->
                [prefixOf(pi.binLocation?.name), pi.inventoryItem?.lotNumber, pi.quantity, pi.quantityPicked]
            }.sort { a, b -> (a[0] <=> b[0]) ?: (a[1] <=> b[1]) ?: (a[2] <=> b[2]) }
        } as List<List>
    }

    /** "STOR-1AB2C3" -> "STOR": fixture bin names are prefix-uid. */
    static String prefixOf(String name) {
        return name ? name.replaceFirst(/-[0-9A-Z]+$/, '') : null
    }

    /** Availability per [binPrefix, lot] -> [onHand, availableToPromise] for one product at the depot. */
    Map<List, List<Integer>> availability(String depotId, String productId) {
        return tx {
            productAvailabilityService.getAvailableItems(Location.get(depotId), [productId]).collectEntries { AvailableItem a ->
                [([prefixOf(a.binLocation?.name), a.inventoryItem?.lotNumber]): [a.quantityOnHand as Integer, a.quantityAvailable as Integer]]
            }
        } as Map<List, List<Integer>>
    }

    RequisitionStatus status(String requisitionId) {
        return tx { Requisition.get(requisitionId).status } as RequisitionStatus
    }

    // ------------------------------------------------------------------ inbound shipments, putaway orders

    /**
     * An inbound shipment from Main Supplier (Location 2) to the depot; items are maps with
     * productId, quantity, lotNumber and any ShipmentItem property (e.g. backorderReference).
     * {@code shipped} adds the SHIPPED event, as ShippingContractSpec.inbound does.
     */
    String inboundShipment(String depotId, List<Map> items, boolean shipped = true) {
        return tx {
            String n = uid()
            Shipment shipment = new Shipment(name: "Test inbound ${n}".toString(), shipmentNumber: "LI${n}".toString(),
                    origin: Location.get('2'), destination: Location.get(depotId),
                    expectedShippingDate: new Date() - 1, expectedDeliveryDate: new Date() + 1, shipmentType: ShipmentType.get('5'))
            items.each { Map m ->
                Map p = new LinkedHashMap(m)
                Product product = Product.get(p.remove('productId'))
                String lotNumber = p.remove('lotNumber')
                InventoryItem lot = lotNumber ? (InventoryItem.findByProductAndLotNumber(product, lotNumber) ?:
                        new InventoryItem(product: product, lotNumber: lotNumber).save(flush: true, failOnError: true)) : null
                shipment.addToShipmentItems(new ShipmentItem([product: product, inventoryItem: lot, lotNumber: lotNumber] + p))
            }
            shipment.save(flush: true, failOnError: true)
            if (shipped) {
                shipmentService.createShipmentEvent(shipment, new Date(), EventCode.SHIPPED, shipment.origin)
                shipment.save(flush: true, failOnError: true)
            }
            return shipment.id
        } as String
    }

    /** A PENDING putaway order (PUTAWAY_ORDER) at the depot: one item from {@code fromBinId} to {@code toBinId}. */
    Map putawayOrder(String depotId, String productId, String lotNumber, int quantity, String fromBinId, String toBinId) {
        return tx {
            String n = uid()
            Location facility = Location.get(depotId)
            Product product = Product.get(productId)
            Order order = new Order(name: "Test putaway ${n}".toString(), orderNumber: "LPA${n}".toString(),
                    orderType: OrderType.findByCode(Constants.PUTAWAY_ORDER), status: OrderStatus.PENDING,
                    origin: facility, destination: facility, orderedBy: Person.get('1'), dateOrdered: new Date())
            OrderItem item = new OrderItem(product: product, inventoryItem: InventoryItem.findByProductAndLotNumber(product, lotNumber),
                    quantity: quantity, quantityPerUom: 1, originBinLocation: Location.get(fromBinId),
                    destinationBinLocation: Location.get(toBinId), orderItemStatusCode: OrderItemStatusCode.PENDING)
            order.addToOrderItems(item)
            order.save(flush: true, failOnError: true)
            return [orderId: order.id, itemId: item.id]
        } as Map
    }

    /** Polls {@code condition} until it is truthy or the timeout passes; returns the last value. */
    static <T> T eventually(long timeoutMs = 30000, Closure<T> condition) {
        long deadline = System.currentTimeMillis() + timeoutMs
        T value = condition.call()
        while (!value && System.currentTimeMillis() < deadline) {
            Thread.sleep(100)
            value = condition.call()
        }
        return value
    }
}
