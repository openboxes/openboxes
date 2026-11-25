package org.pih.warehouse.api.regression.platform.core

import org.pih.warehouse.core.*
import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.dashboard.IndicatorDataService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.InventoryReportCommand

import java.time.LocalDate

/**
 * The expiry lists, fast movers and latest-count export of the dashboard against a depot with three lots of one
 * product: one expired, one expiring within a month and one expiring in more than a year.
 */
class DashboardServiceIntegrationSpec extends ServiceIntegrationSpec {

    DashboardService dashboardService
    IndicatorDataService indicatorDataService

    Location depot
    Product product
    InventoryItem expired
    InventoryItem expiringSoon
    InventoryItem expiringLater
    // The date of the baseline counts, computed once so the fixture and the assertions agree across midnight
    Date countedOn

    void setup() {
        depot = newDepot('Dashboard depot')
        product = newProduct()
        Date today = new Date()
        countedOn = today - 1
        expired = stock(depot, product, uniqueName('EXPIRED'), today - 10, 4, null, countedOn)
        expiringSoon = stock(depot, product, uniqueName('SOON'), today + 20, 6, null, countedOn)
        expiringLater = stock(depot, product, uniqueName('LATER'), today + 400, 2, null, countedOn)
    }

    private List<String> lotIds(List<InventoryItem> items) {
        return items*.id
    }

    void 'expired and expiring stock of the depot, filtered by window and status'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    expired      : lotIds(dashboardService.getExpiredStock(new InventoryReportCommand(location: location))),
                    expiredRange : lotIds(dashboardService.getExpiredStock(new InventoryReportCommand(location: location,
                            startDate: LocalDate.now().minusDays(30), endDate: LocalDate.now(), category: Product.get(product.id).category))),
                    expiring     : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location))),
                    within30     : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location, status: 'within30Days'))),
                    beyondYear   : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location, status: 'greaterThan365Days'))),
                    windowed     : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location,
                            startDate: LocalDate.now(), endDate: LocalDate.now().plusDays(100), category: Product.get(product.id).category))),
            ]
        }

        then:
        result.expired == [expired.id]
        result.expiredRange == [expired.id]
        result.expiring == [expiringSoon.id, expiringLater.id]
        result.within30 == [expiringSoon.id]
        result.beyondYear == [expiringLater.id]
        result.windowed == [expiringSoon.id]
    }

    void 'the dashboard summaries count the stock of the depot'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    fastMovers : dashboardService.getFastMovers(location, new Date(), 5),
                    value      : dashboardService.getTotalStockValue(location),
                    alerts     : dashboardService.getDashboardAlerts(location),
                    expiration : dashboardService.getExpirationSummary(location),
                    csv        : dashboardService.exportLatestInventoryDate(location),
            ]
        }

        then:
        result.fastMovers.location == depot.id
        result.fastMovers.results == []
        result.value.totalCount == 1
        result.value.totalStockValue == 12
        result.alerts.totalStock == 1
        result.alerts.inStock == 1
        result.expiration.expired == 1
        result.expiration.within30Days == 1
        result.expiration.greaterThan365Days == 1
        result.csv.readLines().size() == 2
        result.csv.contains('Most Recent Stock Count')
        result.csv.contains(product.productCode)
        result.csv.readLines()[1].contains(countedOn.format('dd/MMM/yyyy'))
    }

    private InventoryLevel level(Location location, Product product, InventoryStatus status, Integer min, Integer reorder,
                                 Integer max, String abcClass, Location bin = null) {
        return committed {
            InventoryLevel inventoryLevel = new InventoryLevel(inventory: Location.get(location.id).inventory, product: Product.get(product.id),
                    minQuantity: min, reorderQuantity: reorder, maxQuantity: max, abcClass: abcClass,
                    internalLocation: bin?.id ? Location.get(bin.id) : null)
            inventoryLevel.status = status
            inventoryLevel.save(failOnError: true, flush: true)
        }
    }

    /*
     * Characterization of getDashboardAlerts: one product per edge case of the stock-level rules, each with its own
     * power-of-two unit price so that every cost total shows which products a category counted. The costs sum the
     * unit price of each product, without its quantity.
     */
    void 'the dashboard alerts classify each product of the depot by quantity and its first inventory level'() {
        given:
        Location edges = newDepot('Dashboard alerts depot')
        Location bin = newBin(edges, uniqueName('BIN'))
        Closure<Product> stocked = { int quantity, Number price, Map overrides = [:] ->
            Product p = newProduct([pricePerUnit: price != null ? new BigDecimal(price.toString()) : null] + overrides)
            stock(edges, p, uniqueName('LOT'), new Date() + 200, quantity)
            p
        }
        Product noLevel = stocked(5, 1.5)
        level(edges, stocked(5, 1), InventoryStatus.SUPPORTED, 10, 20, 100, 'A')                    // low, reorder
        level(edges, stocked(50, 2), InventoryStatus.SUPPORTED, 10, 20, 100, 'A')                   // healthy
        level(edges, stocked(150, 4), InventoryStatus.STOCK, 10, 20, 100, 'A')                      // over (a status above SUPPORTED)
        level(edges, stocked(0, 8), InventoryStatus.SUPPORTED, 10, 20, 100, 'A')                    // out, class A, low, reorder
        level(edges, stocked(0, 16), InventoryStatus.NOT_SUPPORTED, 10, 20, 100, 'A')               // zero only
        level(edges, stocked(5, null), InventoryStatus.SUPPORTED, null, null, null, null)           // no limits: in stock only
        level(edges, stocked(-2, 32), InventoryStatus.SUPPORTED, 10, 20, 100, 'B')                  // negative: out, class B, low, reorder
        level(edges, stocked(0, 64), InventoryStatus.SUPPORTED, 0, 0, 0, 'C')                       // zero limits count as none: out, class C
        stocked(3, 2.5, [reconditioned: true])                                                      // reconditioned
        level(edges, stocked(0, 128), null, 10, 20, 100, 'A')                                       // no status: zero only
        level(edges, stocked(0, 256), InventoryStatus.SUPPORTED, null, null, null, null)            // out, no class
        level(edges, stocked(0, 1024), InventoryStatus.SUPPORTED_NON_INVENTORY, 10, 20, 100, 'A')   // a status below SUPPORTED: zero only
        level(edges, newProduct([pricePerUnit: 2048.0]), InventoryStatus.SUPPORTED, 10, 20, 100, 'A') // a level without stock: not counted
        // Stock in two bins counts as one product with the total quantity (5 - 2 = 3): low, reorder
        Product twoBins = newProduct([pricePerUnit: 4096.0])
        String twoBinsLot = uniqueName('LOT')
        stock(edges, twoBins, twoBinsLot, new Date() + 200, 5, bin)
        stock(edges, twoBins, twoBinsLot, new Date() + 200, -2, newBin(edges, uniqueName('BIN')))
        level(edges, twoBins, InventoryStatus.SUPPORTED, 10, 20, 100, 'A')
        // Two levels, a facility-wide SUPPORTED one and a NOT_SUPPORTED one for a bin: the level with the lower id applies.
        // The two products create them in opposite orders, so a rule that always took the facility level, or always
        // the bin level, would get one of them wrong.
        Product facilityLevelFirst = stocked(0, 512)                                                // out, class A, low, reorder
        InventoryLevel facilityLevel = level(edges, facilityLevelFirst, InventoryStatus.SUPPORTED, 10, 20, 100, 'A')
        InventoryLevel binLevel = level(edges, facilityLevelFirst, InventoryStatus.NOT_SUPPORTED, 10, 20, 100, 'A', bin)
        Product binLevelFirst = stocked(0, 8192)                                                    // zero only
        InventoryLevel binLevelOfSecond = level(edges, binLevelFirst, InventoryStatus.NOT_SUPPORTED, 10, 20, 100, 'A', bin)
        InventoryLevel facilityLevelOfSecond = level(edges, binLevelFirst, InventoryStatus.SUPPORTED, 10, 20, 100, 'A')
        // Hibernate's uuid generator puts the creation time and a counter in the id, so ids sort in creation order
        assert facilityLevel.id < binLevel.id && binLevelOfSecond.id < facilityLevelOfSecond.id

        when:
        Map alerts = asAdminAt(edges) { dashboardService.getDashboardAlerts(Location.get(edges.id)) }
        Map summary = asAdminAt(edges) { indicatorDataService.getInventorySummaryData(Location.get(edges.id)) }

        then:
        alerts == [
                lowStock               : 5, lowStockCost: 4649,
                reorderStock           : 5, reorderStockCost: 4649,
                overStock              : 1, overStockCost: 4,
                totalStock             : 16, totalStockCost: 14339,
                reconditionedStock     : 1, reconditionedStockCost: 2.5,
                healthyStock           : 1, healthyStockCost: 2,
                outOfStock             : 5, outOfStockCost: 872,
                outOfStockClassA       : 2, outOfStockCostClassA: 520,
                outOfStockClassB       : 1, outOfStockCostClassB: 32,
                outOfStockClassC       : 1, outOfStockCostClassC: 64,
                outOfStockClassNone    : 1, outOfStockCostClassNone: 256,
                onHandQuantityZero     : 9, onHandQuantityZeroCost: 10232,
                inStock                : 7, inStockCost: 4107,
        ]
        noLevel != null

        and: 'counts are integers and the cost of a category with products is a decimal'
        alerts.findAll { !it.key.contains('Cost') }.values().every { it instanceof Integer }
        alerts.findAll { it.key.contains('Cost') }.values().every { it instanceof BigDecimal }

        and: 'the inventory summary panel shows five of them'
        summary.data.labels == ['In stock', 'Above maximum', 'Below reorder', 'Below minimum', 'No longer in stock']
        summary.data.datasets*.data == [[7, 1, 5, 5, 9]]
        summary.data.datasets*.links*.size() == [5]
    }

    /*
     * Characterization of the expiration summaries: lots with stock in the depot, by days to expiry. Lots without an
     * expiration date, and lots whose quantity on hand (over all bins) is zero or less, do not count.
     */
    void 'the expiration summaries count the lots in stock by days to expiry'() {
        given:
        Location edges = newDepot('Dashboard expiry depot')
        Location binA = newBin(edges, uniqueName('BINA'))
        Location binB = newBin(edges, uniqueName('BINB'))
        Product item = newProduct()
        Date today = new Date()
        stock(edges, item, uniqueName('EXPIRED'), today - 5, 2)
        stock(edges, item, uniqueName('NEVER'), null, 4)
        stock(edges, item, uniqueName('D20'), today + 20, 1)
        stock(edges, item, uniqueName('D45'), today + 45, 1)
        stock(edges, item, uniqueName('D400'), today + 400, 1)
        stock(edges, item, uniqueName('D0'), today, 1)            // expires today: expired
        stock(edges, item, uniqueName('D30'), today + 30, 1)      // within 30 days, the boundary included
        stock(edges, item, uniqueName('D365'), today + 365, 1)    // within 365 days, not more than 365
        stock(edges, item, uniqueName('ZERO'), today + 20, 0)
        stock(edges, item, uniqueName('NEGATIVE'), today + 20, -3)
        String split = uniqueName('SPLIT')
        stock(edges, item, split, today + 100, 5, binA)
        stock(edges, item, split, today + 100, -2, binB)
        String splitNegative = uniqueName('SPLITNEG')
        stock(edges, item, splitNegative, today + 100, 2, binA)
        stock(edges, item, splitNegative, today + 100, -5, binB)

        when:
        Map result = asAdminAt(edges) {
            Location location = Location.get(edges.id)
            [
                    legacy  : dashboardService.getExpirationSummary(location),
                    panel   : indicatorDataService.getExpirationSummaryData(location, [querySize: '12']),
                    default_: indicatorDataService.getExpirationSummaryData(location, [:]),
            ]
        }

        then:
        result.legacy == [expired: 2, within30Days: 2, within60Days: 3, within90Days: 3, within180Days: 4, within365Days: 5,
                          greaterThan365Days: 1, totalExpiring: 20]
        result.panel.data.datasets*.data == [[2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4]]
        result.panel.data.labels*.code[0] == 'react.dashboard.timeline.today.label'
        result.panel.data.labels.size() == 13
        result.panel.data.datasets[0].links.size() == 13
        result.panel.link.endsWith('/inventory/listExpiringStock')
        result.default_.data.datasets*.data == [[2, 2, 3, 3, 4, 4, 4]]
    }

    void 'the summaries of a depot without stock are empty'() {
        given:
        Location empty = newDepot('Dashboard empty depot')

        when:
        Map result = asAdminAt(empty) {
            Location location = Location.get(empty.id)
            [
                    alerts    : dashboardService.getDashboardAlerts(location),
                    expiration: dashboardService.getExpirationSummary(location),
                    panel     : indicatorDataService.getExpirationSummaryData(location, [:]),
            ]
        }

        then:
        result.alerts.size() == 26
        result.alerts.values().every { it == 0 && it instanceof Integer }
        result.expiration == [totalExpiring: null]
        result.panel.data.datasets*.data == [[0, 0, 0, 0, 0, 0, 0]]
    }

    void 'the cost of a category whose unit prices cancel out is a decimal zero'() {
        given:
        Location depot = newDepot('Dashboard prices depot')
        [1.0, -1.0].each { BigDecimal price ->
            stock(depot, newProduct([pricePerUnit: price]), uniqueName('LOT'), new Date() + 200, 1)
        }

        when:
        Map alerts = asAdminAt(depot) { dashboardService.getDashboardAlerts(Location.get(depot.id)) }

        then:
        alerts.totalStock == 2
        alerts.totalStockCost == 0
        alerts.totalStockCost instanceof BigDecimal
        alerts.lowStockCost == 0
        alerts.lowStockCost instanceof Integer
    }

    /*
     * Guards the cost of the summaries: they read plain values, so no product, lot or inventory level is loaded as an
     * entity, whatever the number of products and lots (the original code loaded every one). Counted in the session of
     * the call, so that background jobs of the application cannot affect it.
     */
    void 'the summaries do not load products, lots or inventory levels'() {
        given:
        Location depot = newDepot('Dashboard statements depot')
        10.times { int i ->
            Product product = newProduct()
            stock(depot, product, uniqueName('LOT'), new Date() + 10 * i, i + 1)
            level(depot, product, InventoryStatus.SUPPORTED, 5, 10, 20, 'A')
            level(depot, newProduct(), InventoryStatus.SUPPORTED, 5, 10, 20, 'A')   // a level without stock
        }
        List<String> guarded = ['org.pih.warehouse.product.Product', 'org.pih.warehouse.inventory.InventoryItem',
                                'org.pih.warehouse.inventory.InventoryLevel', 'org.pih.warehouse.product.ProductAvailability']

        when:
        Map<String, List<String>> loaded = ['alerts', 'expiration', 'panel'].collectEntries { String summary ->
            List<String> entities = Location.withNewSession { session ->
                asAdminAt(depot) {
                    Location location = Location.get(depot.id)
                    if (summary == 'alerts') {
                        dashboardService.getDashboardAlerts(location)
                    } else if (summary == 'expiration') {
                        dashboardService.getExpirationSummary(location)
                    } else {
                        indicatorDataService.getExpirationSummaryData(location, [querySize: '12'])
                    }
                }
                return session.statistics.entityKeys*.entityName
            }
            [(summary): entities]
        }

        then: 'the session of each call holds the location it read (the check sees the session), and none of the guarded entities'
        loaded.every { it.value.contains('org.pih.warehouse.core.Location') }
        loaded.collectEntries { [(it.key): it.value.findAll { it in guarded }] } == [alerts: [], expiration: [], panel: []]
    }
}
