package org.pih.warehouse.regression.platform

import spock.lang.PendingFeature

import org.pih.warehouse.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.core.DashboardService
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.InventorySnapshotService
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.jobs.CalculateHistoricalQuantityJob
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.Indicator

/**
 * Covers the JsonController actions that delegate to a service and render its result.
 */
class JsonControllerSpec extends Specification implements ControllerUnitTest<JsonController>, DataTest {

    Location location
    Product aspirin

    void setupSpec() {
        mockDomains(Location, Product, Indicator, User)
    }

    void setup() {
        location = new Location(name: "Main Warehouse").save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01", pricePerUnit: 2.5).save(validate: false)
        session.warehouse = location
    }

    void "evaluateIndicator evaluates the stored expression and renders the result"() {
        given:
        Indicator indicator = new Indicator(name: "count", expression: "1 + 1").save(validate: false)
        controller.consoleService = Mock(ConsoleServiceStub)

        when:
        params.id = indicator.id
        controller.evaluateIndicator()

        then:
        1 * controller.consoleService.eval("1 + 1", true, _) >> [result: "2"]
        response.text == "2"
    }

    void "evaluateIndicator renders error when the indicator does not exist"() {
        when:
        params.id = "missing"
        controller.evaluateIndicator()

        then:
        response.text == "error"
    }

    void "getTranslation renders the translation service result"() {
        given:
        controller.translationService = Mock(TranslationServiceStub)
        params.text = "hello"
        params.src = "en"
        params.dest = "fr"

        when:
        controller.getTranslation()

        then:
        1 * controller.translationService.getTranslation("hello", "en", "fr") >> "bonjour"
        response.json.data == "bonjour"
    }

    void "quantity lookups render the service quantity or zero when there is none"() {
        given:
        InventoryService inventoryService = Mock()
        controller.inventoryService = inventoryService
        params['product.id'] = aspirin.id
        params['location.id'] = location.id

        when:
        controller.getQuantityToReceive()

        then:
        1 * inventoryService.getQuantityToReceive(location, aspirin) >> 12
        response.text == "12"

        when:
        response.reset()
        controller.getQuantityToShip()

        then:
        1 * inventoryService.getQuantityToShip(location, aspirin) >> null
        response.text == "0"

        when:
        response.reset()
        controller.getQuantityOnHand()

        then:
        1 * inventoryService.getQuantityOnHand(location, aspirin) >> 7
        response.text == "7"
    }

    void "flushInventoryBrowserCache and flushQuantityOnHandCache redirect to inventory pages"() {
        when:
        controller.flushInventoryBrowserCache()

        then:
        response.redirectedUrl == "/inventory/browse"

        when:
        response.reset()
        controller.flushQuantityOnHandCache()

        then:
        response.redirectedUrl == "/inventory/analyze"
    }

    void "getGenericProductSummary converts each status list into its size"() {
        given:
        controller.inventoryService = Mock(InventoryService)

        when:
        controller.getGenericProductSummary()

        then:
        1 * controller.inventoryService.getGenericProductSummary(location) >> [IN_STOCK: [1, 2, 3], STOCK_OUT: [], LOW: null]
        response.json.totalCount == 3
        response.json.genericProductByStatusMap.IN_STOCK == 3
        response.json.genericProductByStatusMap.STOCK_OUT == 0
        response.json.genericProductByStatusMap.LOW == 0
    }

    void "dashboard alert actions render the dashboard service results for the current location"() {
        given:
        DashboardService dashboardService = Mock()
        controller.dashboardService = dashboardService

        when:
        controller.getDashboardAlerts()

        then:
        1 * dashboardService.getDashboardAlerts(location) >> [alerts: 4]
        response.json.alerts == 4

        when:
        response.reset()
        controller.getDashboardExpiryAlerts()

        then:
        1 * dashboardService.getExpirationSummary(location) >> [expired: 2]
        response.json.expired == 2
    }

    void "getTotalStockValue reports when no inventory snapshot exists yet"() {
        given:
        controller.dashboardService = Mock(DashboardService)
        controller.inventorySnapshotService = Stub(InventorySnapshotService) {
            getLastUpdatedInventorySnapshotDate() >> null
        }

        when:
        controller.getTotalStockValue()

        then:
        1 * controller.dashboardService.getTotalStockValue(location) >> [totalStockValue: 1234, hitCount: 3, missCount: 1, totalCount: 4]
        response.json.lastUpdated == "No data available"
        response.json.totalStockValue == 1234
        response.json.hitCount == 3
        response.json.missCount == 1
        response.json.totalCount == 4
        response.json.totalValue.replace(",", "") == "1234"
    }

    void "getStockValueByProduct hides prices from users without the finance role"() {
        given:
        Product bandage = new Product(name: "Bandage", productCode: "BA01", pricePerUnit: 1.0).save(validate: false)
        controller.dashboardService = Stub(DashboardService) {
            getTotalStockValue(location) >> [stockValueByProduct: [(aspirin): 25.0, (bandage): 100.0]]
        }
        controller.userService = Stub(UserServiceStub) {
            hasRoleFinance(_) >> finance
        }

        when:
        controller.getStockValueByProduct()
        def rows = response.json.aaData

        then: "the most valuable product comes first"
        rows*.productCode == ["BA01", "AS01"]
        rows[0].productName == "Bandage"
        rows[0].totalValue == expectedTotal
        (rows[1].unitPrice == null) == !finance

        where:
        finance | expectedTotal
        true    | "100.00"
        false   | null
    }

    void "refreshTotalStockValue acknowledges the request"() {
        when:
        controller.refreshTotalStockValue()

        then:
        response.json.success == true
    }

    void "getInventorySnapshots counts snapshot rows by stock level"() {
        given:
        def supported = InventoryStatus.SUPPORTED
        def rows = [
                [quantityOnHand: 5, minQuantity: 10, reorderQuantity: 20, maxQuantity: 30, status: supported],  // low
                [quantityOnHand: 15, minQuantity: 10, reorderQuantity: 20, maxQuantity: 30, status: supported], // reorder
                [quantityOnHand: 25, minQuantity: 10, reorderQuantity: 20, maxQuantity: 30, status: supported], // over
                [quantityOnHand: 0, minQuantity: 10, reorderQuantity: 20, maxQuantity: 30, status: supported],  // stock out
                [quantityOnHand: 50, minQuantity: 10, reorderQuantity: 20, maxQuantity: 30, status: InventoryStatus.NOT_SUPPORTED],
        ]
        controller.inventorySnapshotService = Mock(InventorySnapshotService)
        params['location.id'] = location.id

        when:
        controller.getInventorySnapshots()
        def summary = response.json.summary

        then:
        1 * controller.inventorySnapshotService.findInventorySnapshotByLocation(location) >> rows
        summary.totalCount == 5
        summary.inStockCount == 3
        summary.lowStockCount == 1
        summary.reoderStockCount == 1
        summary.overStockCount == 1
        summary.stockOutCount == 1
        response.json.details.results.size() == 5
    }

    void "getQuantityOnHandMap renders the quantities with their count"() {
        given:
        controller.inventoryService = Mock(InventoryService)

        when:
        controller.getQuantityOnHandMap()

        then:
        1 * controller.inventoryService.getQuantityByProductMap(location.id) >> [a: 1, b: 2]
        response.json.count == 2
        response.json.results.b == 2
    }

    void "most recent quantity actions render the inventory service results"() {
        given:
        InventoryService inventoryService = Mock()
        controller.inventoryService = inventoryService
        params.id = aspirin.id

        when:
        controller.mostRecentQuantityOnHand()

        then:
        1 * inventoryService.getMostRecentQuantityOnHand(aspirin, location) >> 9
        response.json.mostRecentQuantityOnHand == 9

        when:
        response.reset()
        controller.mostRecentQuantityOnHandByLocation()

        then:
        1 * inventoryService.getMostRecentQuantityOnHand(location) >> [x: 1]
        response.json.results.x == 1

        when:
        response.reset()
        controller.quantityMap()

        then:
        1 * inventoryService.getQuantityMap(location) >> [y: 3]
        response.json.quantityMap.y == 3

        when:
        response.reset()
        controller.distinctProducts()

        then: "the distinct products are looked up but never returned"
        1 * inventoryService.getDistinctProducts(location) >> [aspirin]
        response.json.products == null
    }

    void "historical quantity job switches report their state"() {
        when:
        controller.disableCalculateHistoricalQuantityJob()

        then:
        !CalculateHistoricalQuantityJob.enabled
        response.json.message == "CalculateHistoricalQuantityJob has been disabled"

        when:
        response.reset()
        controller.statusCalculateHistoricalQuantityJob()

        then:
        response.text == "disabled"

        when:
        response.reset()
        controller.enableCalculateHistoricalQuantityJob()

        then:
        CalculateHistoricalQuantityJob.enabled
        response.json.message == "CalculateHistoricalQuantityJob has been enabled"

        when:
        response.reset()
        controller.statusCalculateHistoricalQuantityJob()

        then:
        response.text == "enabled"
    }

    void "shipment status maintenance actions render counts"() {
        given:
        controller.shipmentService = Mock(ShipmentServiceStub)

        when:
        controller.getShipmentsWithInvalidStatus()

        then:
        1 * controller.shipmentService.getShipmentsWithInvalidStatus() >> ["s1", "s2"]
        response.json.count == 2

        when:
        response.reset()
        controller.fixShipmentsWithInvalidStatus()

        then:
        1 * controller.shipmentService.fixShipmentsWithInvalidStatus() >> 5
        response.json.count == 5
    }

    void "demand actions render the forecasting service results"() {
        given:
        ForecastingService forecastingService = Mock()
        controller.forecastingService = forecastingService
        params.id = aspirin.id

        when:
        controller.getProductDemandDetails()

        then:
        1 * forecastingService.getDemandDetails(location, aspirin) >> [[month: 1]]
        response.json.aaData[0].month == 1

        when:
        response.reset()
        controller.getProductDemandSummary()

        then:
        1 * forecastingService.getDemandSummary(location, aspirin) >> [[total: 4]]
        response.json.aaData[0].total == 4

        when:
        response.reset()
        params['product.id'] = aspirin.id
        params['location.id'] = location.id
        controller.getForecastingData()

        then:
        1 * forecastingService.getDemand(location, null, aspirin) >> [demand: 8]
        response.json.demand == 8
    }

    void "getStockoutData defaults to 30 days and renders the first row"() {
        given:
        controller.reportService = Mock(ReportServiceStub)
        params['product.id'] = aspirin.id
        params['location.id'] = location.id

        when:
        controller.getStockoutData()

        then:
        1 * controller.reportService.getStockoutData(location, aspirin, 30) >> [[stockouts: 2], [stockouts: 9]]
        response.json.stockouts == 2
    }

    void "getOrdersDerivedStatus passes the order ids through"() {
        given:
        controller.orderService = Mock(OrderServiceStub)
        params['order.id'] = ["o1", "o2"]

        when:
        controller.getOrdersDerivedStatus()

        then:
        1 * controller.orderService.getOrdersDerivedStatus(["o1", "o2"]) >> [o1: "PENDING"]
        response.json.o1 == "PENDING"
    }

    void "getSummaryByProductGroup and getSummaryOrderReport render report data"() {
        given:
        controller.reportService = Mock(ReportServiceStub)
        params['location.id'] = location.id

        when:
        controller.getSummaryByProductGroup()

        then:
        1 * controller.reportService.calculateQuantityOnHandByProductGroup(location.id) >> [productGroupSummary: [IN_STOCK: 3]]
        response.json.IN_STOCK == 3

        when:
        response.reset()
        controller.getSummaryOrderReport()

        then:
        1 * controller.reportService.getOnOrderSummary(location) >> [[productCode: "AS01"]]
        response.json.aaData[0].productCode == "AS01"
    }

    void "getQuantityOnHandByProductGroup merges the requested statuses and totals their value"() {
        given:
        controller.reportService = Stub(ReportServiceStub) {
            calculateQuantityOnHandByProductGroup(location.id) >> [productGroupDetails: [
                    IN_STOCK : [a: [name: "a", totalValue: 10.5], b: [name: "b", totalValue: 4.5]],
                    STOCK_OUT: [c: [name: "c", totalValue: 0]],
                    OVERSTOCK: [d: [name: "d", totalValue: 100]]
            ]]
        }
        params['location.id'] = location.id
        params['status[]'] = "IN_STOCK,STOCK_OUT,UNKNOWN"

        when:
        controller.getQuantityOnHandByProductGroup()

        then:
        response.json.aaData*.name.sort() == ["a", "b", "c"]
        response.json.totalValue == 15
        response.json.totalValueFormatted == "15.00"
    }

    void "getQuantityOnHandByProductGroup without statuses renders an empty table"() {
        given:
        controller.reportService = Mock(ReportServiceStub)

        when:
        controller.getQuantityOnHandByProductGroup()

        then:
        0 * controller.reportService._
        response.json.aaData.size() == 0
        response.json.totalValueFormatted == "0.00"
    }

    void "getFastMovers parses the date parameter and falls back to the session location"() {
        given:
        controller.dashboardService = Mock(DashboardService)
        params.date = "03/15/2024"

        when:
        controller.getFastMovers()

        then:
        1 * controller.dashboardService.getFastMovers(location, { Date d -> d.format("yyyy-MM-dd") == "2024-03-15" }, null) >> [results: [[name: "fast"]]]
        response.json.aaData[0].name == "fast"
    }

    void "getFastMovers renders an empty list when there are no results"() {
        given:
        controller.dashboardService = Stub(DashboardService) {
            getFastMovers(_, _, _) >> null
        }

        when:
        controller.getFastMovers()

        then:
        response.json.aaData.size() == 0
    }

    @PendingFeature(reason = 'getFastMovers passes the max string to a method taking an Integer: MissingMethodException')
    void "getFastMovers passes the max parameter to the service as a number"() {
        given:
        controller.dashboardService = Mock(DashboardService)
        params.max = "5"

        when:
        controller.getFastMovers()

        then:
        1 * controller.dashboardService.getFastMovers(location, _ as Date, 5) >> [results: [[name: "fast"]]]
        response.json.aaData[0].name == "fast"
    }

    @PendingFeature(reason = 'calls a two-argument inventoryService.calculateQuantityOnHand that does not exist')
    void "calculateQuantityOnHand asks the inventory service for the quantity of each product at the location"() {
        given:
        InventoryService inventoryService = Mock()
        controller.inventoryService = inventoryService
        params.locationId = location.id

        when:
        controller.calculateQuantityOnHand()

        then:
        1 * inventoryService.getQuantityOnHand({ it.id == location.id }, { it.id == aspirin.id }) >> 4
    }
}

/** Minimal collaborator shapes for services whose real classes pull in heavy dependencies. */
interface ConsoleServiceStub {
    def eval(String expression, boolean returnResult, def request)
}

interface TranslationServiceStub {
    def getTranslation(String text, String src, String dest)
}

interface UserServiceStub {
    boolean hasRoleFinance(def user)
}

interface ShipmentServiceStub {
    def getShipmentsWithInvalidStatus()
    def fixShipmentsWithInvalidStatus()
    def getPendingInboundShipmentItems(def location)
}

interface ReportServiceStub {
    def getStockoutData(def location, def product, int days)
    def calculateQuantityOnHandByProductGroup(def locationId)
    def getOnOrderSummary(def location)
    def getTransactionReport(def location, def categories, def tags, def catalogs, def products, def startDate, def endDate, def csv)
    def getTransactionReportModalData(def location, def product, def startDate, def endDate)
}

interface OrderServiceStub {
    def getOrdersDerivedStatus(def orderIds)
    def getPendingInboundOrderItems(def location)
}
