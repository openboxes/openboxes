package org.pih.warehouse.regression.obaf.allocation

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.AllocationFallbackResolution
import org.pih.warehouse.allocation.AllocationFallbackService
import org.pih.warehouse.allocation.AllocationMode
import org.pih.warehouse.allocation.AllocationRequest
import org.pih.warehouse.allocation.AllocationResult
import org.pih.warehouse.allocation.AllocationService
import org.pih.warehouse.allocation.AllocationSourceStrategy
import org.pih.warehouse.allocation.AllocationStep
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.inventory.LotStatusCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatus

import java.time.Instant

import static org.pih.warehouse.regression.obaf.support.UnitAllocationData.*

/**
 * AllocationService (OBAF's allocation engine) with every collaborator mocked. Available stock
 * is what StockMovementService.getAvailableItems answers; suggestions are computed by the REAL
 * StockMovementService.getSuggestedItems / getPrepickSuggestedItems (pure functions), so the
 * quantities below are the engine's, not the mock's.
 *
 * Covered: strategy selection (explicit list, requisition, config, default), rotation, the
 * pickable / quantity / fallback-bin filters, multi-strategy fall-through, insufficient stock,
 * backordered lines, negative allocation and the fallback location, cross-dock release, pre-pick,
 * the save path, the requisition-wide two-phase allocate, deallocate, the automatic allocation
 * entry point (attempt cap, system user, error swallowing) and completeAllocation.
 */
class AllocationServiceSpec extends Specification implements ServiceUnitTest<AllocationService>, DataTest {

    StockMovementService stockMovementService = Mock()
    StockMovementService realStockMovementService = new StockMovementService()
    RequisitionService requisitionService = Mock()
    AllocationFallbackService allocationFallbackService = Mock()
    CycleCountService cycleCountService = Mock()
    InventoryService inventoryService = Mock()
    ProductAvailabilityService productAvailabilityService = Mock()
    AuthService authService = Mock()

    Location facility
    Location preferred, storage, storage2, display
    Product product

    void setupSpec() {
        mockDomain Requisition
    }

    void setup() {
        service.stockMovementService = stockMovementService
        service.requisitionService = requisitionService
        service.allocationFallbackService = allocationFallbackService
        service.cycleCountService = cycleCountService
        service.inventoryService = inventoryService
        service.productAvailabilityService = productAvailabilityService
        service.authService = authService

        // the pure suggestion functions run for real
        stockMovementService.getSuggestedItems(_, _) >> { List items, Integer q -> realStockMovementService.getSuggestedItems(items, q) }
        stockMovementService.getPrepickSuggestedItems(_, _) >> { List items, Integer q -> realStockMovementService.getPrepickSuggestedItems(items, q) }
        authService.withSystemUser(_) >> { Closure c -> c.call() }

        config.openboxes.order.allocation.remove('source')
        config.openboxes.order.allocation.remove('rotation')
        config.openboxes.jobs.automaticAllocationJob.maxAttempts = 3

        facility = facility('DC')
        preferred = bin(facility, 'PREF')
        storage = bin(facility, 'STOR')
        storage2 = bin(facility, 'STOR2')
        display = displayBin(facility, 'DISP')
        product = product('ALLOC')
        preferredBin(null)
    }

    void cleanup() {
        resetPreferredBin()
        GroovySystem.metaClassRegistry.removeMetaClass(Requisition)
    }

    // ------------------------------------------------------------------ fixtures

    Requisition requisition(Map props = [:]) {
        Requisition r = new Requisition([origin: facility, requestNumber: nextId('REQ'), status: RequisitionStatus.CREATED,
                                         deliveryTypeCode: DeliveryTypeCode.LOCAL_DELIVERY] + props)
        r.id = nextId('req')
        r.requisitionItems = [] as LinkedHashSet
        r.eventLogs = [] as Set
        return r
    }

    RequisitionItem line(Requisition r, int quantity, Map props = [:]) {
        RequisitionItem ri = new RequisitionItem([product: product, quantity: quantity, requisition: r] + props)
        ri.id = nextId('ri')
        r.requisitionItems << ri
        return ri
    }

    void stockIs(RequisitionItem ri, List<AvailableItem> items) {
        stockMovementService.getAvailableItems(_, ri, false, _) >> { args -> new ArrayList(items) }
    }

    AllocationResult auto(RequisitionItem ri, Integer quantity = null, List<AllocationSourceStrategy> strategies = [], boolean crossDock = false) {
        return service.allocate(new AllocationRequest(requisitionItem: ri, quantityRequired: quantity, allocationMode: AllocationMode.AUTO,
                allocationStrategies: strategies, crossDockRelease: crossDock), false)
    }

    static List<List> picks(List<SuggestedItem> items) {
        return items.collect { [it.binLocation?.name, it.inventoryItem?.lotNumber, it.quantityPicked as Integer] }
    }

    // ------------------------------------------------------------------ AUTO: ordering

    void 'AUTO with the default strategy (STORAGE_FIRST) and rotation (FEFO) takes the earliest expiry in storage first, then splits'() {
        given:
        RequisitionItem ri = line(requisition(), 7)
        stockIs(ri, [available(lot(product, 'LATE', days(200)), storage, 5),
                     available(lot(product, 'EARLY', days(20)), storage2, 5),
                     available(lot(product, 'DISP-EARLIEST', days(1)), display, 50)])

        when:
        AllocationResult result = auto(ri)

        then: 'display stock is last for STORAGE_FIRST even though it expires first'
        picks(result.suggestedItems) == [['STOR2', 'EARLY', 5], ['STOR', 'LATE', 2]]
        result.prepickItems == []
    }

    void 'the source group order outranks FEFO: the preferred bin is drained before an earlier lot elsewhere'() {
        given:
        preferredBin(preferred)
        RequisitionItem ri = line(requisition(), 4)
        stockIs(ri, [available(lot(product, 'EARLY', days(5)), storage, 10),
                     available(lot(product, 'LATE', days(300)), preferred, 3)])

        expect:
        picks(auto(ri).suggestedItems) == [['PREF', 'LATE', 3], ['STOR', 'EARLY', 1]]
    }

    void 'FEFO applies inside the preferred bin'() {
        given:
        preferredBin(preferred)
        RequisitionItem ri = line(requisition(), 4)
        stockIs(ri, [available(lot(product, 'P-LATE', days(90)), preferred, 3),
                     available(lot(product, 'P-EARLY', days(9)), preferred, 3)])

        expect:
        picks(auto(ri).suggestedItems) == [['PREF', 'P-EARLY', 3], ['PREF', 'P-LATE', 1]]
    }

    void 'rotation NONE (config) keeps the order StockMovementService returned'() {
        given:
        config.openboxes.order.allocation.rotation = 'NONE'
        RequisitionItem ri = line(requisition(), 6)
        stockIs(ri, [available(lot(product, 'LATE', days(200)), storage, 5),
                     available(lot(product, 'EARLY', days(20)), storage2, 5)])

        expect:
        picks(auto(ri).suggestedItems) == [['STOR', 'LATE', 5], ['STOR2', 'EARLY', 1]]
    }

    @Unroll
    void 'strategy resolution: explicit #explicit / requisition #onRequisition / config #configured -> takes from #expectedBin'() {
        given:
        if (configured) {
            config.openboxes.order.allocation.source = configured
        }
        RequisitionItem ri = line(requisition(allocationSourceStrategy: onRequisition), 2)
        stockIs(ri, [available(lot(product, 'S', days(100)), storage, 5),
                     available(lot(product, 'D', days(100)), display, 5)])

        expect:
        picks(auto(ri, null, explicit).suggestedItems)*.getAt(0) == [expectedBin]

        where:
        explicit                                  | onRequisition                          | configured      || expectedBin
        []                                        | null                                   | null            || 'STOR'
        []                                        | null                                   | 'DISPLAY_FIRST' || 'DISP'
        []                                        | AllocationSourceStrategy.DISPLAY_ONLY  | 'STORAGE_FIRST' || 'DISP'
        [AllocationSourceStrategy.STORAGE_ONLY]   | AllocationSourceStrategy.DISPLAY_ONLY  | 'DISPLAY_FIRST' || 'STOR'
        null                                      | AllocationSourceStrategy.DISPLAY_FIRST | null            || 'DISP'
    }

    void 'several explicit strategies: the first one that can cover the whole quantity wins'() {
        given:
        RequisitionItem ri = line(requisition(), 6)
        stockIs(ri, [available(lot(product, 'D', days(10)), display, 4),
                     available(lot(product, 'S', days(100)), storage, 10)])

        expect: 'DISPLAY_ONLY holds 4 < 6, so STORAGE_ONLY is used in full, never a mix'
        picks(auto(ri, null, [AllocationSourceStrategy.DISPLAY_ONLY, AllocationSourceStrategy.STORAGE_ONLY]).suggestedItems) ==
                [['STOR', 'S', 6]]
    }

    void 'DISPLAY_ONLY never takes storage stock, STORAGE_ONLY never takes display stock'() {
        given:
        RequisitionItem ri = line(requisition(), 3)
        stockIs(ri, [available(lot(product, 'D', days(10)), display, 5),
                     available(lot(product, 'S', days(10)), storage, 5)])

        expect:
        picks(auto(ri, null, [AllocationSourceStrategy.DISPLAY_ONLY]).suggestedItems)*.getAt(0) == ['DISP']
        picks(auto(ri, null, [AllocationSourceStrategy.STORAGE_ONLY]).suggestedItems)*.getAt(0) == ['STOR']
    }

    // ------------------------------------------------------------------ AUTO: filters

    void 'stock that cannot be allocated is skipped: zero available, a held bin, a recalled lot, the negative-inventory fallback bin'() {
        given:
        Location held = bin(facility, 'HELD', [ActivityCode.HOLD_STOCK])
        Location fallbackBin = bin(facility, 'SHORTFALL', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK])
        InventoryItem recalled = lot(product, 'RECALLED', days(1))
        recalled.lotStatus = LotStatusCode.RECALLED
        RequisitionItem ri = line(requisition(), 4)
        stockIs(ri, [available(lot(product, 'ZERO', days(1)), storage, 0, 5),
                     available(lot(product, 'H', days(1)), held, 10),
                     available(recalled, storage, 10),
                     available(lot(product, 'FB', days(1)), fallbackBin, 10),
                     available(lot(product, 'OK', days(400)), storage2, 4)])

        expect:
        picks(auto(ri).suggestedItems) == [['STOR2', 'OK', 4]]
    }

    void 'the quantity required defaults to calculateQuantityRequired (ordered less canceled)'() {
        given:
        RequisitionItem ri = line(requisition(), 10, [quantityCanceled: 4])
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 50)])

        expect:
        picks(auto(ri).suggestedItems) == [['STOR', 'S', 6]]
    }

    void 'exactly enough stock is allocated in full'() {
        given:
        RequisitionItem ri = line(requisition(), 10)
        stockIs(ri, [available(lot(product, 'A', days(10)), storage, 4), available(lot(product, 'B', days(20)), storage2, 6)])

        expect:
        picks(auto(ri).suggestedItems) == [['STOR', 'A', 4], ['STOR2', 'B', 6]]
    }

    void 'insufficient stock throws with the required and the best available quantity'() {
        given:
        RequisitionItem ri = line(requisition(), 10)
        stockIs(ri, [available(lot(product, 'D', days(10)), display, 2), available(lot(product, 'S', days(10)), storage, 7)])

        when:
        auto(ri, null, [AllocationSourceStrategy.DISPLAY_ONLY, AllocationSourceStrategy.STORAGE_ONLY])

        then:
        IllegalArgumentException e = thrown()
        e.message.startsWith('Insufficient stock for product ALLOC')
        e.message.contains('Required quantity: 10, Available quantity: 7')
    }

    void 'no stock at all throws (partialAllocationAllowed does not apply to a line that is not backordered)'() {
        given:
        RequisitionItem ri = line(requisition(partialAllocationAllowed: true), 3)
        stockIs(ri, [])

        when:
        auto(ri)

        then:
        thrown(IllegalArgumentException)
    }

    // ------------------------------------------------------------------ backorders

    void 'a fully backordered line is skipped by ordinary allocation, without looking at stock'() {
        given:
        RequisitionItem ri = line(requisition(), 5, [quantityBackordered: 5, backorderedReasonCode: ReasonCode.BACKORDER.toString()])

        when:
        AllocationResult result = auto(ri, 5)

        then:
        result.suggestedItems == []
        0 * stockMovementService.getAvailableItems(*_)
    }

    @Unroll
    void 'a partly backordered line is skipped by ordinary allocation as a whole (partial allocation #partial, #stock in stock), without looking at stock'() {
        given: 'a backordered line is a cross-dock line: only the cross-dock release allocates it'
        RequisitionItem ri = line(requisition(partialAllocationAllowed: partial), 10,
                [quantityBackordered: 4, backorderedReasonCode: ReasonCode.BACKORDER.toString()])
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, stock)])

        when:
        AllocationResult result = auto(ri, 10)

        then:
        notThrown(IllegalArgumentException)
        result.suggestedItems == []
        0 * stockMovementService.getAvailableItems(*_)

        where:
        partial | stock
        false   | 50
        true    | 2
        false   | 2
    }

    // ------------------------------------------------------------------ negative allocation

    Location negativeFacility() {
        facility.supportedActivities << ActivityCode.ALLOW_NEGATIVE_INVENTORY.id
        return facility
    }

    void 'negative allocation: real stock first, the shortfall to the resolved negative-inventory bin, a count requested and logged'() {
        given:
        negativeFacility()
        Location negBin = bin(facility, 'NEG', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        InventoryItem defaultLot = lot(product, null, null)
        Requisition r = requisition(negativeAllocationAllowed: true)
        RequisitionItem ri = line(r, 5)
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 3)])

        when:
        AllocationResult result = auto(ri)

        then:
        1 * allocationFallbackService.resolve(facility, product, AllocationSourceStrategy.STORAGE_FIRST) >>
                new AllocationFallbackResolution(negBin, AllocationStep.NEGATIVE_INVENTORY)
        1 * inventoryService.findOrCreateDefaultInventoryItem(product) >> defaultLot
        1 * cycleCountService.getOrCreateCycleCountRequest(facility, product)
        1 * requisitionService.addSystemComment(r, { it.startsWith('Negative inventory: allocated quantity 2') })
        1 * requisitionService.addSystemEventLog(r, { it.startsWith('Negative inventory: allocated quantity 2') })
        picks(result.suggestedItems) == [['STOR', 'S', 3], ['NEG', null, 2]]
        result.suggestedItems[1].quantityAvailable == 0
    }

    void 'negative allocation falls back to the shortfall location with an "Inventory shortfall" message'() {
        given:
        negativeFacility()
        Location shortfall = bin(facility, 'SHORTFALL', [ActivityCode.NEGATIVE_INVENTORY_FALLBACK])
        Requisition r = requisition(negativeAllocationAllowed: true)
        RequisitionItem ri = line(r, 4)
        stockIs(ri, [])
        inventoryService.findOrCreateDefaultInventoryItem(product) >> lot(product, null, null)

        when:
        AllocationResult result = auto(ri)

        then:
        1 * allocationFallbackService.resolve(*_) >> new AllocationFallbackResolution(shortfall, AllocationStep.FALLBACK_LOCATION)
        1 * requisitionService.addSystemComment(r, { it.startsWith('Inventory shortfall: no location at DC could supply quantity 4') })
        picks(result.suggestedItems) == [['SHORTFALL', null, 4]]
    }

    void 'negative allocation adds the shortfall to an existing suggestion for the same lot and bin instead of a duplicate row'() {
        given:
        negativeFacility()
        Location negBin = bin(facility, 'NEG', [ActivityCode.ALLOW_NEGATIVE_INVENTORY])
        InventoryItem defaultLot = lot(product, null, null)
        RequisitionItem ri = line(requisition(negativeAllocationAllowed: true), 5)
        stockIs(ri, [available(defaultLot, negBin, 3)])
        allocationFallbackService.resolve(*_) >> new AllocationFallbackResolution(negBin, AllocationStep.NEGATIVE_INVENTORY)
        inventoryService.findOrCreateDefaultInventoryItem(product) >> defaultLot

        expect:
        picks(auto(ri).suggestedItems) == [['NEG', null, 5]]
    }

    void 'negative allocation with nowhere to put the shortfall fails like insufficient stock'() {
        given:
        negativeFacility()
        RequisitionItem ri = line(requisition(negativeAllocationAllowed: true), 5)
        stockIs(ri, [])
        allocationFallbackService.resolve(*_) >> null

        when:
        auto(ri)

        then:
        IllegalArgumentException e = thrown()
        e.message.contains('Negative allocation also failed')
        0 * cycleCountService.getOrCreateCycleCountRequest(*_)
    }

    @Unroll
    void 'negative allocation is not attempted when #why'() {
        given:
        if (facilityAllows) {
            negativeFacility()
        }
        RequisitionItem ri = line(requisition(negativeAllocationAllowed: orderAllows), 5)
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 1)])

        when:
        service.allocate(new AllocationRequest(requisitionItem: ri, allocationMode: mode, availableItems: []), false)

        then:
        thrown(IllegalArgumentException)
        0 * allocationFallbackService.resolve(*_)

        where:
        why                               | facilityAllows | orderAllows | mode
        'the order does not allow it'     | true           | false       | AllocationMode.AUTO
        'the order flag is unset'         | true           | null        | AllocationMode.AUTO
        'the facility does not allow it'  | false          | true        | AllocationMode.AUTO
        'the allocation is MANUAL'        | true           | true        | AllocationMode.MANUAL
    }

    // ------------------------------------------------------------------ cross-dock release

    void 'cross-dock release: the order own cross-dock zone first, then ordinary stock; other cross-dock zones left out; own allocation not released, pre-pick skipped, picklist kept'() {
        given:
        Location crossDock = bin(facility, 'XDOCK', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_SERVICE])
        Location otherZone = bin(facility, 'XDOCK-PU', [ActivityCode.CROSS_DOCKING, ActivityCode.DELIVERY_TYPE_PICKUP])
        Location untypedZone = bin(facility, 'XDOCK-ANY', [ActivityCode.CROSS_DOCKING])
        Requisition r = requisition(deliveryTypeCode: DeliveryTypeCode.SERVICE)
        RequisitionItem ri = line(r, 10, [quantityBackordered: 6, backorderedReasonCode: ReasonCode.BACKORDER.toString()])

        when:
        AllocationResult result = service.allocate(new AllocationRequest(requisitionItem: ri, quantityRequired: 6,
                allocationMode: AllocationMode.AUTO, allocationStrategies: [], crossDockRelease: true), true)

        then:
        1 * stockMovementService.getAvailableItems(facility, ri, false, false) >>
                [available(lot(product, 'OTHER', days(1)), otherZone, 9), available(lot(product, 'STOCK', days(5)), storage, 50),
                 available(lot(product, 'ANY', days(2)), untypedZone, 9), available(lot(product, 'INBOUND', days(300)), crossDock, 4)]
        0 * stockMovementService.getPrepickAvailableItems(*_)
        0 * stockMovementService.clearPicklist(_ as RequisitionItem)
        1 * stockMovementService.allocateSuggestedItems(ri, _, true)
        picks(result.suggestedItems) == [['XDOCK', 'INBOUND', 4], ['STOR', 'STOCK', 2]]
    }

    void 'cross-dock release ignores a backorder (it IS the backorder being released)'() {
        given:
        RequisitionItem ri = line(requisition(), 5, [quantityBackordered: 5, backorderedReasonCode: ReasonCode.BACKORDER.toString()])
        stockMovementService.getAvailableItems(facility, ri, false, false) >> [available(lot(product, 'X', days(5)), storage, 9)]

        expect:
        picks(auto(ri, 5, [], true).suggestedItems) == [['STOR', 'X', 5]]
    }

    // ------------------------------------------------------------------ pre-pick

    void 'pre-pick (SERVICE orders): pre-pick bin stock is consumed first and ordinary allocation covers the rest'() {
        given:
        Location prepickBin = bin(facility, 'PREPICK', [ActivityCode.PREPICK_STOCK, ActivityCode.HOLD_STOCK])
        RequisitionItem ri = line(requisition(deliveryTypeCode: DeliveryTypeCode.SERVICE), 5)
        stockMovementService.getPrepickAvailableItems(facility, ri) >> [available(lot(product, 'PP', days(30)), prepickBin, 0, 2)]
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 10)])

        when:
        AllocationResult result = auto(ri)

        then:
        picks(result.prepickItems) == [['PREPICK', 'PP', 2]]
        picks(result.suggestedItems) == [['STOR', 'S', 3]]
    }

    void 'pre-pick covering the whole line leaves nothing for ordinary allocation'() {
        given:
        Location prepickBin = bin(facility, 'PREPICK', [ActivityCode.PREPICK_STOCK, ActivityCode.HOLD_STOCK])
        RequisitionItem ri = line(requisition(deliveryTypeCode: DeliveryTypeCode.SERVICE), 3)
        stockMovementService.getPrepickAvailableItems(facility, ri) >> [available(lot(product, 'PP', days(30)), prepickBin, 0, 8)]

        when:
        AllocationResult result = auto(ri)

        then:
        picks(result.prepickItems) == [['PREPICK', 'PP', 3]]
        result.suggestedItems == []
        0 * stockMovementService.getAvailableItems(*_)
    }

    @Unroll
    void 'pre-pick is not consulted for #deliveryType orders'() {
        given:
        RequisitionItem ri = line(requisition(deliveryTypeCode: deliveryType), 2)
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 10)])

        when:
        auto(ri)

        then:
        0 * stockMovementService.getPrepickAvailableItems(*_)

        where:
        deliveryType << (DeliveryTypeCode.values() - DeliveryTypeCode.SERVICE).toList() + [null]
    }

    // ------------------------------------------------------------------ save path

    void 'saving an AUTO allocation clears the line picklist, then writes suggestions (auto-allocated) and pre-picks'() {
        given:
        RequisitionItem ri = line(requisition(), 2)
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 10)])

        when:
        service.allocate(new AllocationRequest(requisitionItem: ri, allocationMode: AllocationMode.AUTO, allocationStrategies: []))

        then:
        1 * stockMovementService.clearPicklist(ri)

        then:
        1 * stockMovementService.allocateSuggestedItems(ri, { picks(it) == [['STOR', 'S', 2]] }, true)
        1 * stockMovementService.allocatePrepickItems(ri, [])
    }

    void 'saveAllocation=false writes nothing'() {
        given:
        RequisitionItem ri = line(requisition(), 2)
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 10)])

        when:
        auto(ri)

        then:
        0 * stockMovementService.clearPicklist(_)
        0 * stockMovementService.allocateSuggestedItems(*_)
        0 * stockMovementService.allocatePrepickItems(*_)
    }

    void 'allocating the same line twice yields the same suggestions (idempotent: the picklist is rebuilt, not added to)'() {
        given:
        RequisitionItem ri = line(requisition(), 4)
        stockIs(ri, [available(lot(product, 'S', days(10)), storage, 10)])
        List<List> captured = []
        stockMovementService.allocateSuggestedItems(ri, _, _) >> { RequisitionItem i, List items, Boolean a -> captured << picks(items) }

        when:
        2.times { service.allocate(new AllocationRequest(requisitionItem: ri, allocationMode: AllocationMode.AUTO, allocationStrategies: [])) }

        then:
        2 * stockMovementService.clearPicklist(ri)
        captured == [[['STOR', 'S', 4]], [['STOR', 'S', 4]]]
    }

    void 'MIXED is not supported by the core engine'() {
        when:
        service.allocate(new AllocationRequest(requisitionItem: line(requisition(), 1), allocationMode: AllocationMode.MIXED))

        then:
        thrown(UnsupportedOperationException)
    }

    // ------------------------------------------------------------------ MANUAL

    void 'MANUAL: the chosen items are used (other products dropped), not marked auto-allocated'() {
        given:
        RequisitionItem ri = line(requisition(), 3)
        Product other = product('OTHER')
        List<AvailableItem> chosen = [available(lot(other, 'O', days(1)), storage, 10), available(lot(product, 'M', days(400)), storage2, 10)]

        when:
        AllocationResult result = service.allocate(ri, 3, AllocationMode.MANUAL, chosen)

        then:
        picks(result.suggestedItems) == [['STOR2', 'M', 3]]
        1 * stockMovementService.allocateSuggestedItems(ri, _, false)
        0 * stockMovementService.getAvailableItems(*_)
    }

    void 'MANUAL with too little chosen stock tops up automatically from other stock'() {
        given:
        RequisitionItem ri = line(requisition(), 8)
        InventoryItem chosenLot = lot(product, 'CHOSEN', days(400))
        stockIs(ri, [available(lot(product, 'OTHER', days(500)), storage, 10)])

        when:
        AllocationResult result = service.allocate(ri, 8, AllocationMode.MANUAL, [available(chosenLot, storage2, 5)])

        then:
        picks(result.suggestedItems) == [['STOR2', 'CHOSEN', 5], ['STOR', 'OTHER', 3]]
    }

    @PendingFeature(reason = 'AllocationService.groovy:169/384 the MANUAL top-up passes the SuggestedItems as excludeList but compares them to fresh AvailableItems by identity, so it re-suggests the bin/lot the manual pick already exhausted')
    void 'MANUAL top-up never takes more from a bin/lot than it holds (the manual selection is excluded)'() {
        given: 'the manual pick takes all 5 of the early lot in STOR2; the top-up must come from STOR'
        RequisitionItem ri = line(requisition(), 8)
        InventoryItem early = lot(product, 'EARLY', days(10))
        stockIs(ri, [available(early, storage2, 5), available(lot(product, 'LATE', days(500)), storage, 10)])

        when:
        AllocationResult result = service.allocate(ri, 8, AllocationMode.MANUAL, [available(early, storage2, 5)])
        Map<List, Integer> byBinAndLot = result.suggestedItems.groupBy { [it.binLocation.name, it.inventoryItem.lotNumber] }
                .collectEntries { k, v -> [(k): v.sum { it.quantityPicked } as Integer] }

        then:
        byBinAndLot[['STOR2', 'EARLY']] == 5
        byBinAndLot[['STOR', 'LATE']] == 3
    }

    // ------------------------------------------------------------------ requisition-wide AUTO

    void 'allocate(Requisition) computes every line before writing any, and writes nothing when one line fails'() {
        given:
        Requisition r = requisition()
        RequisitionItem ok = line(r, 2)
        Product scarce = product('SCARCE')
        RequisitionItem short1 = line(r, 9, [product: scarce])
        stockIs(ok, [available(lot(product, 'S', days(10)), storage, 10)])
        stockIs(short1, [available(lot(scarce, 'X', days(10)), storage, 1)])

        when:
        service.allocate(r, AllocationMode.AUTO, [])

        then:
        thrown(IllegalArgumentException)
        0 * stockMovementService.clearPicklist(_)
        0 * stockMovementService.allocateSuggestedItems(*_)
        1 * requisitionService.logRequisitionComment(r.id, { it.startsWith(Constants.ALLOCATION_FAILED + ' Insufficient stock for product SCARCE') })
        1 * requisitionService.logRequisitionEvent(r.id, { it.startsWith(Constants.ALLOCATION_FAILED) })
    }

    void 'allocate(Requisition) writes each line with suggestions and leaves a line with nothing to allocate untouched'() {
        given:
        Requisition r = requisition()
        RequisitionItem a = line(r, 2)
        RequisitionItem b = line(r, 3)
        RequisitionItem backordered = line(r, 4, [quantityBackordered: 4, backorderedReasonCode: ReasonCode.BACKORDER.toString()])
        stockIs(a, [available(lot(product, 'S', days(10)), storage, 10)])
        stockIs(b, [available(lot(product, 'S', days(10)), storage, 10)])

        when:
        List<AllocationResult> results = service.allocate(r, AllocationMode.AUTO, [])

        then:
        results.size() == 3
        1 * stockMovementService.clearPicklist(a)
        1 * stockMovementService.clearPicklist(b)
        0 * stockMovementService.clearPicklist(backordered)
        1 * stockMovementService.allocateSuggestedItems(a, _, true)
        1 * stockMovementService.allocateSuggestedItems(b, _, true)
    }

    @PendingFeature(reason = 'AllocationService.groovy:237-251 allocate(Requisition) suggests each line against the same stock snapshot before any line is written, so two lines of one product can both be given the same units')
    void 'allocate(Requisition): two lines of the same product never share the same units'() {
        given:
        Requisition r = requisition()
        RequisitionItem first = line(r, 4)
        RequisitionItem second = line(r, 4)
        List<AvailableItem> stock = [available(lot(product, 'S', days(10)), storage, 5)]
        stockIs(first, stock)
        stockIs(second, stock)

        when: 'only 5 units exist for 8 ordered'
        List<AllocationResult> results = service.allocate(r, AllocationMode.AUTO, [])

        then: 'either it fails or the two lines together take at most 5'
        results.sum { it.suggestedItems.sum { s -> s.quantityPicked } ?: 0 } <= 5
    }

    // ------------------------------------------------------------------ deallocate

    @Unroll
    void 'deallocate(Requisition) refuses when a pick is #status (nothing cleared, status kept)'() {
        given:
        Requisition r = requisition(status: RequisitionStatus.PICKING)
        line(r, 2)
        r.picklist = new Picklist(picklistItems: [new PicklistItem(status: status)] as Set)

        when:
        service.deallocate(r)

        then:
        thrown(Exception)
        0 * stockMovementService.clearPicklist(_)
        r.status == RequisitionStatus.PICKING

        where:
        status << ['PICKED', 'STAGED']
    }

    // Not data-driven on purpose: Spock 1.3 does not report @PendingFeature per unrolled iteration.
    @PendingFeature(reason = 'AllocationService.groovy:213,219 new ValidationException(String) has no such constructor in Grails 3 (needs Errors): the refusal surfaces as GroovyRuntimeException, i.e. a 500 instead of a validation error')
    void 'deallocate refuses a PICKED or STAGED pick with a ValidationException (requisition and line)'() {
        given:
        List<Class> thrownTypes = []

        when:
        [['PICKED', 'STAGED'], ['requisition', 'line']].combinations().each { String status, String target ->
            Requisition r = requisition(status: RequisitionStatus.PICKING)
            RequisitionItem ri = line(r, 2)
            ri.picklistItems = [new PicklistItem(status: status)] as Set
            r.picklist = new Picklist(picklistItems: ri.picklistItems)
            try {
                target == 'requisition' ? service.deallocate(r) : service.deallocate(ri)
                thrownTypes << null
            } catch (Throwable t) {
                thrownTypes << t.getClass()
            }
        }

        then:
        thrownTypes == [ValidationException] * 4
    }

    void 'deallocate(Requisition) clears every line, resets autoAllocated and returns the order to CREATED'() {
        given:
        Requisition r = requisition(status: RequisitionStatus.PICKING)
        RequisitionItem a = line(r, 2, [autoAllocated: true])
        RequisitionItem b = line(r, 3, [autoAllocated: true])
        r.picklist = new Picklist(picklistItems: [new PicklistItem(status: 'PENDING')] as Set)

        when:
        Boolean result = service.deallocate(r)

        then:
        result
        1 * stockMovementService.clearPicklist(a)
        1 * stockMovementService.clearPicklist(b)
        a.autoAllocated == null
        b.autoAllocated == null
        r.status == RequisitionStatus.CREATED
    }

    void 'deallocate(RequisitionItem) refuses a line with a picked pick and clears an unpicked one'() {
        given:
        Requisition r = requisition()
        RequisitionItem picked = line(r, 2)
        picked.picklistItems = [new PicklistItem(status: 'PICKED')] as Set
        RequisitionItem pending = line(r, 2, [autoAllocated: true])
        pending.picklistItems = [new PicklistItem(status: 'PENDING')] as Set

        when:
        service.deallocate(picked)

        then:
        thrown(Exception)
        0 * stockMovementService.clearPicklist(picked)

        when:
        boolean ok = service.deallocate(pending)

        then:
        ok
        1 * stockMovementService.clearPicklist(pending)
        pending.autoAllocated == null
    }

    // ------------------------------------------------------------------ automatic allocation entry point

    Requisition eligible(Map props = [:], List<AvailableItem> stock = null) {
        facility.supportedActivities << ActivityCode.AUTOMATIC_ALLOCATION_ENABLED.id
        Requisition r = requisition([autoAllocationRequested: true] + props)
        line(r, 2)
        stockIs(r.requisitionItems.first(), stock != null ? stock : [available(lot(product, 'S', days(10)), storage, 10)])
        Requisition.metaClass.static.get = { Serializable id -> id == r.id ? r : null }
        return r
    }

    void 'allocateRequisition runs as the system user, allocates and moves the order to PICKING'() {
        given:
        Requisition r = eligible()

        when:
        service.allocateRequisition(r.id)

        then:
        1 * authService.withSystemUser(_) >> { Closure c -> c.call() }
        1 * stockMovementService.allocateSuggestedItems(r.requisitionItems.first(), _, true)
        1 * stockMovementService.updateRequisitionStatus(r.id, RequisitionStatus.PICKING)
        0 * stockMovementService.issueRequisition(_)
    }

    void 'allocateRequisition with auto-issuance requested issues instead of moving to PICKING, then refreshes availability'() {
        given:
        Requisition r = eligible(autoIssuanceRequested: true)

        when:
        service.allocateRequisition(r.id)

        then:
        1 * stockMovementService.issueRequisition(r)
        1 * productAvailabilityService.refreshProductsAvailability(facility.id, [product.id], false)
        0 * stockMovementService.updateRequisitionStatus(*_)
    }

    @Unroll
    void 'allocateRequisition skips a requisition that is #why'() {
        given:
        Requisition r = eligible()
        mutate.call(r)

        when:
        service.allocateRequisition(r.id)

        then:
        0 * stockMovementService.allocateSuggestedItems(*_)
        0 * stockMovementService.updateRequisitionStatus(*_)
        notThrown(Exception)

        where:
        why                                    | mutate
        'not CREATED'                          | { Requisition q -> q.status = RequisitionStatus.PICKING }
        'not flagged autoAllocationRequested'  | { Requisition q -> q.autoAllocationRequested = false }
        'at a facility without the activity'   | { Requisition q -> q.origin.supportedActivities.remove(ActivityCode.AUTOMATIC_ALLOCATION_ENABLED.id) }
        'without lines'                        | { Requisition q -> q.requisitionItems.clear() }
    }

    void 'allocateRequisition of an unknown id does nothing and does not throw'() {
        when:
        service.allocateRequisition('no-such-id')

        then:
        notThrown(Exception)
        0 * stockMovementService._
    }

    @Unroll
    void 'allocateRequisition stops at the attempt cap (#attempts attempts, cap #cap)'() {
        given:
        config.openboxes.jobs.automaticAllocationJob.maxAttempts = cap
        Requisition r = eligible()
        attempts.times {
            r.eventLogs << new EventLog(eventLogCode: EventLogCode.ERROR_OCCURRED, message: "${Constants.ALLOCATION_FAILED} try ${it}".toString(),
                    dateCreated: Instant.now())
        }
        assert r.allocationAttemptCount == attempts

        when:
        service.allocateRequisition(r.id)

        then:
        (attempts >= cap ? 0 : 1) * stockMovementService.allocateSuggestedItems(*_)
        (attempts >= cap ? 1 : 0) * requisitionService.logRequisitionComment(r.id, { it.startsWith("Max attempts (${cap})") })

        where:
        attempts | cap
        2        | 3
        3        | 3
        4        | 3
        0        | 1
        1        | 1
    }

    void 'allocateRequisition swallows an allocation failure after recording it as an attempt'() {
        given:
        Requisition r = eligible([:], [])

        when:
        service.allocateRequisition(r.id)

        then:
        notThrown(Exception)
        1 * requisitionService.logRequisitionEvent(r.id, { it.startsWith(Constants.ALLOCATION_FAILED) })
        0 * stockMovementService.updateRequisitionStatus(*_)
    }

    void 'allocateRequisition swallows a missing system user (nothing allocated, nothing recorded)'() {
        given:
        Requisition r = eligible()

        when:
        service.allocateRequisition(r.id)

        then:
        1 * authService.withSystemUser(_) >> { throw new IllegalStateException('Unable to find system user with username admin') }
        notThrown(Exception)
        0 * stockMovementService.allocateSuggestedItems(*_)
        0 * requisitionService.logRequisitionEvent(*_)
    }

    void 'allocateRequisition moves an order covered entirely by pre-picked stock to PICKING'() {
        given:
        Location prepickBin = bin(facility, 'PREPICK', [ActivityCode.PREPICK_STOCK, ActivityCode.HOLD_STOCK])
        Requisition r = eligible(deliveryTypeCode: DeliveryTypeCode.SERVICE)
        stockMovementService.getPrepickAvailableItems(*_) >> [available(lot(product, 'PP', days(30)), prepickBin, 0, 5)]

        when:
        service.allocateRequisition(r.id)

        then:
        1 * stockMovementService.allocatePrepickItems(r.requisitionItems.first(), { it*.quantityPicked == [2] })
        1 * stockMovementService.updateRequisitionStatus(r.id, RequisitionStatus.PICKING)
    }

    // ------------------------------------------------------------------ completeAllocation

    void 'completeAllocation: nothing allocated and no auto-issuance leaves the status alone'() {
        when:
        service.completeAllocation(requisition(), false)

        then:
        0 * stockMovementService.updateRequisitionStatus(*_)
        0 * stockMovementService.issueRequisition(_)
    }

    void 'completeAllocation: an issuance failure is recorded as an issuance attempt and rethrown'() {
        given:
        Requisition r = requisition(autoIssuanceRequested: true)

        when:
        service.completeAllocation(r, true)

        then:
        1 * stockMovementService.issueRequisition(r) >> { throw new IllegalStateException('boom') }
        1 * requisitionService.logRequisitionEvent(r.id, "${Constants.ISSUANCE_FAILED} boom".toString())
        IllegalStateException e = thrown()
        e.message == 'boom'
        0 * productAvailabilityService.refreshProductsAvailability(*_)
    }
}
