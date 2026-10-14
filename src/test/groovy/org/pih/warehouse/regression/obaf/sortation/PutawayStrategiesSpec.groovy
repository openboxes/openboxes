package org.pih.warehouse.regression.obaf.sortation

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.allocation.BackorderMatch
import org.pih.warehouse.allocation.BackorderMatchingService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.inboundSortation.DemandService
import org.pih.warehouse.inboundSortation.PutawayContext
import org.pih.warehouse.inboundSortation.PutawayResult
import org.pih.warehouse.inboundSortation.PutawayStrategyService
import org.pih.warehouse.inboundSortation.strategy.CrossDockPutawayStrategy
import org.pih.warehouse.inboundSortation.strategy.CrossDockingStrategy
import org.pih.warehouse.inboundSortation.strategy.DirectedPutawayStrategy
import org.pih.warehouse.inboundSortation.strategy.PutawayStrategy
import org.pih.warehouse.inboundSortation.strategy.RandomPutawayStrategy
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

import static org.pih.warehouse.core.ActivityCode.*

/**
 * The inbound routing ("putaway") strategies on in-memory locations: DirectedPutawayStrategy
 * (preferred bin, else an UNDEFINED default location), CrossDockPutawayStrategy (backorder match
 * -> the cross-dock lane of its delivery type), the deprecated demand-based CrossDockingStrategy,
 * RandomPutawayStrategy, the container (cart) assignment of the PutawayStrategy trait, and the
 * PutawayStrategyService chain that splits a quantity across them.
 *
 * Locations carry their own supported activities, which is what Location.supports reads first.
 */
class PutawayStrategiesSpec extends Specification implements DataTest {

    static int seq = 0

    static LocationType binType = new LocationType(name: 'Bin', locationTypeCode: LocationTypeCode.BIN_LOCATION)

    Location facility(Collection<ActivityCode> activities = []) {
        Location f = new Location(name: "F${++seq}", active: true)
        f.id = "fac-${seq}"
        f.supportedActivities = activities*.id as Set<String>
        f.locations = [] as Set
        return f
    }

    Location loc(Location parent, String name, Collection<ActivityCode> activities, Location zone = null) {
        Location l = new Location(name: name, active: true, parentLocation: parent, locationType: binType, zone: zone)
        l.id = "${name}-${++seq}"
        l.supportedActivities = activities*.id as Set<String>
        parent.locations << l
        return l
    }

    Product product = new Product(name: 'P', productCode: 'P1')
    InventoryItem lot = new InventoryItem(product: product, lotNumber: 'L1')

    PutawayContext context(Location f, Map props = [:]) {
        Location sort = loc(f, 'SORT', [INBOUND_SORTATION])
        return new PutawayContext([facility: f, product: product, inventoryItem: lot, currentBinLocation: sort, quantity: 10] + props)
    }

    static List<List> summary(List<PutawayResult> results) {
        return results.collect { [it.destination?.name, it.container?.name, it.quantity, it.comment] }
    }

    // ------------------------------------------------------------------ Directed

    void 'directed: the preferred bin takes the whole remaining quantity'() {
        given:
        Location f = facility()
        Location stor = loc(f, 'STOR', [PICK_STOCK])
        PutawayContext ctx = context(f, [preferredBin: stor])

        expect:
        summary(new DirectedPutawayStrategy().execute(ctx, f.activeStorageLocations, 7, [])) == [['STOR', null, 7, 'Default Location']]
    }

    void 'directed: without a preferred bin, the default internal location only when it is UNDEFINED'() {
        given:
        Location f = facility()
        Location undefined = loc(f, 'UNDEF', [UNDEFINED_LOCATION])
        Location plain = loc(f, 'PLAIN', [PICK_STOCK])

        expect:
        summary(new DirectedPutawayStrategy().execute(context(f, [internalLocation: undefined]), [], 4, [])) == [['UNDEF', null, 4, 'Default Internal Location']]
        new DirectedPutawayStrategy().execute(context(f, [internalLocation: plain]), [], 4, []) == []
        new DirectedPutawayStrategy().execute(context(f), [], 4, []) == []
    }

    // ------------------------------------------------------------------ container assignment (trait)

    @Unroll
    void 'container assignment: #assignment'() {
        given:
        Location f = facility(assignment ? [assignment] : [])
        Location zone = new Location(name: 'Z')
        zone.id = 'zone-1'
        Location stor = loc(f, 'STOR', [PICK_STOCK], zone)
        Location zoneCart = loc(f, 'ZCART', [PICK_STOCK, PUTAWAY_CART], zone)
        Location ldCart = loc(f, 'LDCART', [PICK_STOCK, PUTAWAY_CART, DELIVERY_TYPE_LOCAL_DELIVERY])
        PutawayContext ctx = context(f, [preferredBin: stor, deliveryTypeCode: deliveryType])

        expect:
        new DirectedPutawayStrategy().execute(ctx, f.activeStorageLocations, 3, [])[0].container?.name == cart

        where:
        assignment                                    | deliveryType                    || cart
        null                                          | DeliveryTypeCode.LOCAL_DELIVERY || null
        PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE | DeliveryTypeCode.LOCAL_DELIVERY || 'LDCART'
        PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE | DeliveryTypeCode.PICK_UP        || null
        PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE | null                            || null
        PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE | DeliveryTypeCode.DEFAULT        || null
        PUTAWAY_CONTAINER_ASSIGNMENT_BY_ZONE          | null                            || 'ZCART'
    }

    void 'container assignment by delivery type wins over by zone when a facility has both'() {
        given:
        Location f = facility([PUTAWAY_CONTAINER_ASSIGNMENT_BY_ZONE, PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE])
        Location zone = new Location(name: 'Z')
        zone.id = 'zone-2'
        Location stor = loc(f, 'STOR', [PICK_STOCK], zone)
        loc(f, 'ZCART', [PICK_STOCK, PUTAWAY_CART], zone)

        expect:
        new DirectedPutawayStrategy().execute(context(f, [preferredBin: stor]), f.activeStorageLocations, 3, [])[0].container == null
    }

    // ------------------------------------------------------------------ CrossDockPutaway

    Requisition demandOrder(DeliveryTypeCode code) {
        return new Requisition(requestNumber: 'BO-1', deliveryTypeCode: code)
    }

    BackorderMatch match(int quantity, DeliveryTypeCode code = DeliveryTypeCode.LOCAL_DELIVERY) {
        RequisitionItem demand = new RequisitionItem(product: product)
        demand.requisition = demandOrder(code)
        return new BackorderMatch(demand: demand, quantityMatched: quantity)
    }

    void 'cross-dock: no backorder link, no match, or no lane for the delivery type gives nothing'() {
        given:
        Location f = facility()
        loc(f, 'LANE', [PICK_STOCK, CROSS_DOCKING, DELIVERY_TYPE_LOCAL_DELIVERY])
        BackorderMatchingService matching = Mock()
        CrossDockPutawayStrategy strategy = new CrossDockPutawayStrategy(backorderMatchingService: matching)

        expect:
        strategy.execute(context(f), f.activeStorageLocations, 10, []) == []

        when:
        List none = strategy.execute(context(f, [backorderReference: 'BO-1']), f.activeStorageLocations, 10, [])

        then:
        1 * matching.resolveCrossDockMatch('BO-1', null, product, 10) >> null
        none == []

        when:
        List noLane = strategy.execute(context(f, [backorderReference: 'BO-1']), f.activeStorageLocations, 10, [])

        then:
        1 * matching.resolveCrossDockMatch('BO-1', null, product, 10) >> match(3, DeliveryTypeCode.SHIP_TO)
        noLane == []
    }

    void 'cross-dock: the matched quantity (at most what is left) goes to the lane of the backorder delivery type, which is written into the context'() {
        given:
        Location f = facility([PUTAWAY_CONTAINER_ASSIGNMENT_BY_DELIVERY_TYPE])
        loc(f, 'PULANE', [PICK_STOCK, CROSS_DOCKING, DELIVERY_TYPE_PICKUP])
        loc(f, 'LDLANE', [PICK_STOCK, CROSS_DOCKING, DELIVERY_TYPE_LOCAL_DELIVERY])
        loc(f, 'NOTXD', [PICK_STOCK, DELIVERY_TYPE_LOCAL_DELIVERY])
        loc(f, 'LDCART', [PICK_STOCK, PUTAWAY_CART, DELIVERY_TYPE_LOCAL_DELIVERY])
        RequisitionItem link = new RequisitionItem(product: product)
        BackorderMatchingService matching = Mock()
        CrossDockPutawayStrategy strategy = new CrossDockPutawayStrategy(backorderMatchingService: matching)
        PutawayContext ctx = context(f, [backorderItem: link])

        when:
        List<PutawayResult> results = strategy.execute(ctx, f.activeStorageLocations, 5, [])

        then:
        1 * matching.resolveCrossDockMatch(null, link, product, 5) >> match(matched)
        summary(results) == [['LDLANE', 'LDCART', expected, 'Cross-Docking']]
        ctx.deliveryTypeCode == DeliveryTypeCode.LOCAL_DELIVERY

        where:
        matched || expected
        3       || 3
        9       || 5
    }

    // ------------------------------------------------------------------ CrossDocking (deprecated, demand based; not wired)

    void 'demand-based cross-docking: unmet demand per delivery type, in priority order, to non-cart lanes; DEFAULT and lane-less types are skipped'() {
        given:
        Location f = facility()
        loc(f, 'PULANE', [PICK_STOCK, DELIVERY_TYPE_PICKUP])
        loc(f, 'PUCART', [PICK_STOCK, DELIVERY_TYPE_PICKUP, PUTAWAY_CART])
        loc(f, 'LDLANE', [PICK_STOCK, DELIVERY_TYPE_LOCAL_DELIVERY])
        DemandService demand = Mock()
        CrossDockingStrategy strategy = new CrossDockingStrategy(demandService: demand)

        when:
        List<PutawayResult> results = strategy.execute(context(f), f.activeStorageLocations, 10, [])

        then:
        1 * demand.calculateUnmetDemand(f, product) >> [(DeliveryTypeCode.PICK_UP): 3, (DeliveryTypeCode.LOCAL_DELIVERY): 4,
                                                         (DeliveryTypeCode.SHIP_TO): 2, (DeliveryTypeCode.DEFAULT): 50]
        summary(results) == [['PULANE', 'PUCART', 3, 'Cross-Docking'], ['LDLANE', null, 4, 'Cross-Docking']]
    }

    void 'demand-based cross-docking: stops once the quantity is used up, and nothing at all for no quantity'() {
        given:
        Location f = facility()
        loc(f, 'PULANE', [PICK_STOCK, DELIVERY_TYPE_PICKUP])
        loc(f, 'LDLANE', [PICK_STOCK, DELIVERY_TYPE_LOCAL_DELIVERY])
        DemandService demand = Mock()
        CrossDockingStrategy strategy = new CrossDockingStrategy(demandService: demand)

        when:
        List<PutawayResult> results = strategy.execute(context(f), f.activeStorageLocations, 5, [])
        List<PutawayResult> none = strategy.execute(context(f), f.activeStorageLocations, 0, [])

        then:
        1 * demand.calculateUnmetDemand(f, product) >> [(DeliveryTypeCode.PICK_UP): 5, (DeliveryTypeCode.LOCAL_DELIVERY): 4]
        summary(results) == [['PULANE', null, 5, 'Cross-Docking']]
        none == []
    }

    void 'demand-based cross-docking: a result already in the list with the same quantity suppresses a duplicate (and does not use up quantity)'() {
        given:
        Location f = facility()
        loc(f, 'PULANE', [PICK_STOCK, DELIVERY_TYPE_PICKUP])
        DemandService demand = Mock()
        CrossDockingStrategy strategy = new CrossDockingStrategy(demandService: demand)
        PutawayContext ctx = context(f)
        PutawayResult earlier = new PutawayResult(facility: f, product: product, inventoryItem: lot, location: ctx.currentBinLocation, quantity: 3)

        when:
        List<PutawayResult> results = strategy.execute(ctx, f.activeStorageLocations, 10, [earlier])

        then:
        1 * demand.calculateUnmetDemand(f, product) >> [(DeliveryTypeCode.PICK_UP): 3]
        results == []
    }

    // ------------------------------------------------------------------ Random

    void 'random: one of the locations, with the whole quantity; none without locations'() {
        given:
        Location f = facility()
        Location a = loc(f, 'A', [PICK_STOCK])
        Location b = loc(f, 'B', [PICK_STOCK])

        when:
        List<PutawayResult> results = new RandomPutawayStrategy().execute(context(f), [a, b], 6, [])

        then:
        results.size() == 1
        results[0].destination in [a, b]
        results[0].quantity == 6
        results[0].comment == 'Random'
        new RandomPutawayStrategy().execute(context(f), [], 6, []) == []
    }

    // ------------------------------------------------------------------ the chain

    PutawayStrategy fixed(Integer quantity, String destination) {
        return new PutawayStrategy() {
            List<PutawayResult> execute(PutawayContext c, List<Location> l, Integer remaining, List<PutawayResult> results) {
                quantity == null ? [] : [new PutawayResult(destination: new Location(name: destination), quantity: Math.min(quantity, remaining))]
            }
        }
    }

    void 'the chain: each strategy gets what is left; it stops once everything is placed; the rest is left unplaced'() {
        given:
        Location f = facility()
        PutawayStrategyService service = new PutawayStrategyService(strategies: strategies)

        expect:
        service.execute(context(f, [quantity: 10])).collect { [it.destination.name, it.quantity] } == expected

        where:
        strategies                                                   || expected
        [fixed(3, 'LANE'), fixed(100, 'STOR')]                       || [['LANE', 3], ['STOR', 7]]
        [fixed(10, 'LANE'), fixed(100, 'STOR')]                      || [['LANE', 10]]
        [fixed(null, 'LANE'), fixed(4, 'STOR')]                      || [['STOR', 4]]
        [fixed(null, 'LANE'), fixed(null, 'STOR')]                   || []
        []                                                           || []
    }

    void 'the chain wired in resources.groovy (cross-dock, then directed) splits a backordered receipt'() {
        given:
        Location f = facility()
        loc(f, 'LANE', [PICK_STOCK, CROSS_DOCKING, DELIVERY_TYPE_LOCAL_DELIVERY])
        Location stor = loc(f, 'STOR', [PICK_STOCK])
        BackorderMatchingService matching = Mock() {
            resolveCrossDockMatch('BO-1', null, product, 10) >> match(4)
        }
        PutawayStrategyService service = new PutawayStrategyService(strategies: [
                new CrossDockPutawayStrategy(backorderMatchingService: matching), new DirectedPutawayStrategy()])

        expect:
        summary(service.execute(context(f, [backorderReference: 'BO-1', preferredBin: stor]))) ==
                [['LANE', null, 4, 'Cross-Docking'], ['STOR', null, 6, 'Default Location']]
    }

    void 'activeStorageLocations (the chain candidates) are the active internal PICK_STOCK locations only'() {
        given:
        Location f = facility()
        loc(f, 'PICK', [PICK_STOCK])
        loc(f, 'LANE-NOPICK', [CROSS_DOCKING, DELIVERY_TYPE_LOCAL_DELIVERY])
        Location inactive = loc(f, 'OFF', [PICK_STOCK])
        inactive.active = false
        Location notInternal = new Location(name: 'DEPOT', active: true, parentLocation: f,
                locationType: new LocationType(locationTypeCode: LocationTypeCode.DEPOT))
        notInternal.supportedActivities = [PICK_STOCK.id] as Set
        f.locations << notInternal

        expect:
        f.activeStorageLocations*.name == ['PICK']
    }
}
