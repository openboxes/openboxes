package org.pih.warehouse.regression.platform.core

import spock.lang.PendingFeature

import org.pih.warehouse.core.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryLevelStatus
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.ReorderReportFilterCommand
import org.pih.warehouse.inventory.ReorderReportItemDto
import org.pih.warehouse.inventory.product.availability.InventoryByProduct
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * The stock-level views of the dashboard: products are put into bands by comparing the quantity on hand (from the
 * availability service, mocked here) against the inventory levels of the location.
 */
class DashboardServiceSpec extends Specification implements ServiceUnitTest<DashboardService>, DataTest {

    ProductAvailabilityService productAvailabilityService = Mock()
    ForecastingService forecastingService = Mock()
    UserService userService = Mock()
    MessageLocalizer messageLocalizer = Stub() {
        localize(*_) >> { args -> args.size() > 1 && args[1] ? (args[1] instanceof Object[] ? args[1][0] : args[1]) : args[0] }
    }

    Location depot
    Product outOfStock
    Product low
    Product reorder
    Product healthy
    Product over
    Product unmanaged

    void setupSpec() {
        mockDomains(Location, Inventory, InventoryLevel, Product, Category, Tag, User, Person)
    }

    void setup() {
        service.productAvailabilityService = productAvailabilityService
        service.forecastingService = forecastingService
        service.userService = userService
        service.messageLocalizer = messageLocalizer
        LocalizationService localizationStub = Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        defineBeans {
            localizationService(InstanceFactoryBean, localizationStub, LocalizationService)
        }

        Inventory inventory = new Inventory()
        inventory.save(validate: false, flush: true)
        depot = new Location(name: 'Depot', inventory: inventory)
        depot.save(validate: false, flush: true)

        outOfStock = product('P-OUT', 'Out of stock', 2.0, 'A')
        low = product('P-LOW', 'Low', 1.0, 'B')
        reorder = product('P-REORDER', 'Reorder', null, 'C')
        healthy = product('P-HEALTHY', 'Healthy', 3.0, null)
        over = product('P-OVER', 'Over', 4.0, null)
        unmanaged = product('P-NONE', 'Unmanaged', 5.0, null)
        unmanaged.reconditioned = true
        unmanaged.save(validate: false, flush: true)
    }

    void cleanup() {
        new AuthService().setCurrentLocation(null)
    }

    private Product product(String code, String name, BigDecimal price, String abcClass) {
        Product product = new Product(productCode: code, name: name, pricePerUnit: price, costPerUnit: price)
        product.save(validate: false, flush: true)
        if (code != 'P-NONE') {
            new InventoryLevel(inventory: depot.inventory, product: product, status: InventoryStatus.SUPPORTED, abcClass: abcClass,
                    minQuantity: 5, reorderQuantity: 10, maxQuantity: 20).save(validate: false, flush: true)
        }
        return product
    }

    private Map<Product, Integer> quantities() {
        return [(outOfStock): 0, (low): 3, (reorder): 8, (healthy): 15, (over): 30, (unmanaged): 7]
    }

    private Map inventoryByProduct() {
        return quantities().collectEntries { Product product, Integer quantity ->
            [(product): [quantityOnHand: quantity, quantityAvailableToPromise: quantity - 1]]
        }
    }

    void 'getDashboardAlerts counts the products in each stock band'() {
        when:
        Map alerts = service.getDashboardAlerts(depot)

        then:
        1 * productAvailabilityService.getCurrentInventory(depot) >> quantities()
        alerts.totalStock == 6
        alerts.outOfStock == 1
        alerts.outOfStockClassA == 1
        alerts.outOfStockClassB == 0
        alerts.lowStock == 2
        alerts.reorderStock == 3
        alerts.healthyStock == 1
        alerts.overStock == 1
        alerts.reconditionedStock == 1
        alerts.onHandQuantityZero == 1
        alerts.inStock == 5
    }

    @PendingFeature(reason = 'getTotalCost: price ?: 0 * qty ?: 0 (operator precedence), so band costs ignore the quantity')
    void 'getDashboardAlerts values each stock band at unit price times quantity on hand'() {
        when:
        Map alerts = service.getDashboardAlerts(depot)

        then:
        1 * productAvailabilityService.getCurrentInventory(depot) >> quantities()
        alerts.totalStockCost == 203.0
        alerts.inStockCost == 203.0
        alerts.healthyStockCost == 45.0
        alerts.overStockCost == 120.0
        alerts.reconditionedStockCost == 35.0
        alerts.lowStockCost == 3.0
        alerts.outOfStockCost == 0
        alerts.onHandQuantityZeroCost == 0
    }

    void 'the stock band lists use the inventory levels of the location'() {
        given:
        productAvailabilityService.getInventoryByProduct(depot, []) >> inventoryByProduct()

        expect:
        service.getTotalStock(depot).size() == 6
        service.getInStock(depot, []).size() == 5
        service.getQuantityOnHandZero(depot, [])*.product == [outOfStock]
        service.getOutOfStock(depot, null, [])*.product == [outOfStock]
        service.getOutOfStock(depot, 'A', [])*.product == [outOfStock]
        service.getOutOfStock(depot, 'B', []).empty
        service.getLowStock(depot, [])*.product as Set == [outOfStock, low] as Set
        service.getReorderStock(depot, [])*.product as Set == [outOfStock, low, reorder] as Set
        service.getHealthyStock(depot, [])*.product == [healthy]
        service.getOverStock(depot, [])*.product == [over]
        service.getReconditionedStock(depot, [])*.product == [unmanaged]
    }

    void 'getInventoryItems reports the level status, or in or out of stock where the product has no level'() {
        given:
        productAvailabilityService.getInventoryByProduct(depot, []) >> inventoryByProduct()

        when:
        List items = service.getInventoryItems(depot)

        then:
        items.find { it.product == low }.status == 'LOW_STOCK'
        items.find { it.product == over }.status == 'OVERSTOCK'
        items.find { it.product == unmanaged }.status == 'IN_STOCK'
        items.find { it.product == unmanaged }.inventoryLevel == null
        items.find { it.product == healthy }.quantityAvailableToPromise == 14
    }

    void 'getInventoryStatus and getInventoryStatusAndLevel label every product'() {
        given:
        productAvailabilityService.getCurrentInventory(depot) >> quantities() + [(new Product(name: 'Gone')): 0]

        when:
        Map statuses = service.getInventoryStatus(depot)
        Map statusAndLevel = service.getInventoryStatusAndLevel(depot)

        then:
        statuses[low] == 'LOW_STOCK'
        statuses[unmanaged] == 'IN_STOCK'
        statuses.find { it.key.name == 'Gone' }.value == 'STOCK_OUT'
        statusAndLevel[over].status == 'OVERSTOCK'
        statusAndLevel[over].onHandQuantity == 30
        statusAndLevel[over].inventoryLevel.maxQuantity == 20
        statusAndLevel[unmanaged].status == 'NONE'
    }

    void 'getTotalStockValue values the stock of priced products only'() {
        when:
        Map value = service.getTotalStockValue(depot)

        then:
        1 * productAvailabilityService.getCurrentInventory(depot) >> quantities()
        value.totalCount == 6
        value.hitCount == 5
        value.missCount == 1
        value.totalStockValue == 1.0 * 3 + 3.0 * 15 + 4.0 * 30 + 5.0 * 7
        value.stockValueByProduct[over] == 120.0
        !value.stockValueByProduct.containsKey(outOfStock)
    }

    void 'getTotalStockValue is empty for a location without inventory'() {
        when:
        Map value = service.getTotalStockValue(new Location(name: 'Clinic'))

        then:
        0 * productAvailabilityService._
        value.totalStockValue == 0.0
        value.totalCount == 0
    }

    void 'getExpirationSummary buckets lots by days to expiry'() {
        given:
        DashboardService spied = Spy(DashboardService)
        spied.getExpirationAlerts(depot) >> [
                [daysToExpiry: -2, inventoryItem: [expires: 'expired']],
                [daysToExpiry: 20, inventoryItem: [expires: 'within30Days']],
                [daysToExpiry: 100, inventoryItem: [expires: 'within180Days']],
                [daysToExpiry: 400, inventoryItem: [expires: 'greaterThan365Days']],
                [daysToExpiry: null, inventoryItem: [expires: 'never']],
        ]

        when:
        Map summary = spied.getExpirationSummary(depot)

        then:
        summary.expired == 1
        summary.within30Days == 1
        summary.within90Days == 1
        summary.within180Days == 2
        summary.within365Days == 2
        summary.greaterThan365Days == 1
        summary.totalExpiring == 9
    }

    void 'the simple reorder report lists levels below their minimum or reorder point, with what to order'() {
        given:
        productAvailabilityService.getInventoryByProduct(depot, []) >> inventoryByProduct()

        when:
        List rows = service.getReorderReport(depot)

        then:
        rows*.product as Set == [outOfStock, low, reorder] as Set
        Map reorderRow = rows.find { it.product == reorder }
        reorderRow.quantityToOrder == 20 - 7
        reorderRow.expectedReorderCost == null
        rows.find { it.product == low }.quantityToOrder == 18
        rows.find { it.product == low }.expectedReorderCost == 18.0
    }

    private InventoryByProduct available(Product product, Integer final_, Integer expired = 0) {
        return new InventoryByProduct(product: product, quantityOnHand: final_ + expired, finalQuantityAvailableToPromise: final_,
                expiredQuantityAvailableToPromise: expired)
    }

    void 'the reorder report filters by level status and prices the order for finance users'() {
        given:
        new AuthService().setCurrentLocation(depot)
        List<InventoryByProduct> available = [available(outOfStock, 0), available(low, 3, 2), available(reorder, 8),
                                              available(healthy, 15), available(over, 30)]
        productAvailabilityService.getInventoriesByProduct(*_) >> available
        forecastingService.getDemand(*_) >> [monthlyDemand: 6]
        userService.hasRoleFinance(_) >> true

        when:
        List<ReorderReportItemDto> all = service.getReorderReport(new ReorderReportFilterCommand(inventoryLevelStatus: InventoryLevelStatus.ALL_PRODUCTS))
        List<ReorderReportItemDto> belowMinimum = service.getReorderReport(new ReorderReportFilterCommand(inventoryLevelStatus: InventoryLevelStatus.BELOW_MINIMUM))
        List<ReorderReportItemDto> belowReorder = service.getReorderReport(new ReorderReportFilterCommand(inventoryLevelStatus: InventoryLevelStatus.BELOW_REORDER))
        List<ReorderReportItemDto> belowMaximum = service.getReorderReport(new ReorderReportFilterCommand(inventoryLevelStatus: InventoryLevelStatus.BELOW_MAXIMUM))

        then:
        all.size() == 5
        belowMinimum*.product == [outOfStock, low]
        belowReorder*.product == [outOfStock, low, reorder]
        belowMaximum*.product == [outOfStock, low, reorder, healthy]

        and:
        ReorderReportItemDto lowItem = all.find { it.product == low }
        lowItem.inventoryStatus == 'LOW_STOCK'
        lowItem.quantityToOrder == 17
        lowItem.unitCost == 1.0
        lowItem.expectedReorderCost == 17.0
        lowItem.monthlyDemand == 6
        lowItem.expiredQuantityAvailableToPromise == 2
        all.find { it.product == over }.quantityToOrder == 0
        all.find { it.product == reorder }.expectedReorderCost == null
    }

    void 'the reorder report csv has the localized columns and hides costs from other users'() {
        given:
        new AuthService().setCurrentLocation(depot)
        InventoryLevel level = InventoryLevel.findByProduct(low)
        List<ReorderReportItemDto> report = [
                new ReorderReportItemDto(inventoryStatus: 'LOW_STOCK', product: low, inventoryLevel: level, monthlyDemand: 6,
                        finalQuantityAvailableToPromise: 3, expiredQuantityAvailableToPromise: 2, quantityToOrder: 17, unitCost: 1.0,
                        expectedReorderCost: 17.0),
                new ReorderReportItemDto(inventoryStatus: 'IN_STOCK', product: unmanaged, inventoryLevel: null,
                        finalQuantityAvailableToPromise: 7, expiredQuantityAvailableToPromise: 0),
        ]

        when:
        userService.hasRoleFinance(_) >> financeUser
        String csv = service.getReorderReportCsv(report)
        List<String> lines = csv.readLines()

        then:
        lines[0].contains('Quantity to Order')
        lines[0].contains('Expected Reorder Cost') == financeUser
        lines[1].contains('enum.InventoryLevelStatusCsv.LOW_STOCK')
        lines[1].contains('P-LOW')
        lines[1].contains('"17"')
        lines[2].contains('reorderReport.noMaxQtySet.label')

        where:
        financeUser << [true, false]
    }
}
