package org.pih.warehouse.regression.platform.reporting

import spock.lang.PendingFeature

import org.pih.warehouse.reporting.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.DefaultGrailsTagDateHelper
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.core.UserService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.inventory.BinLocationItem
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.report.ChecklistReportCommand
import org.pih.warehouse.report.MultiLocationInventoryReportCommand
import org.pih.warehouse.report.ReportService
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType

class ReportControllerSpec extends Specification implements ControllerUnitTest<ReportController>, DataTest {

    Closure doWithSpring() {{ ->
        grailsTagDateHelper(DefaultGrailsTagDateHelper)
    }}

    Location warehouse
    Location supplier
    Product aspirin
    Product bandage
    Category medicines

    ReportService reportService = Mock()
    InventoryService inventoryService = Mock()
    ProductAvailabilityService productAvailabilityService = Mock()
    DataService dataService = Mock()
    DocumentService documentService = Mock()
    UserService userService = Mock()

    void setupSpec() {
        mockDomains(Location, Inventory, Product, Category, Tag, InventoryItem, InventoryLevel, Transaction, User,
                Order, OrderItem, Shipment, ShipmentItem, ShipmentType)
    }

    void setup() {
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
            }, LocalizationService)
        }
        warehouse = new Location(name: "Warehouse").save(validate: false)
        warehouse.inventory = new Inventory(warehouse: warehouse).save(validate: false)
        supplier = new Location(name: "Supplier").save(validate: false)
        medicines = new Category(name: "Medicines").save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01", pricePerUnit: 2.0, category: medicines).save(validate: false)
        bandage = new Product(name: "Bandage", productCode: "BA01", pricePerUnit: 0.5, category: medicines).save(validate: false)

        controller.reportService = reportService
        controller.inventoryService = inventoryService
        controller.productAvailabilityService = productAvailabilityService
        controller.dataService = dataService
        controller.documentService = documentService
        controller.userService = userService
        session.warehouse = warehouse
        session.user = new User(username: "manager")
    }

    @Unroll
    void "#action triggers the matching report service rebuild"() {
        when:
        controller."$action"()

        then:
        1 * reportService."$serviceMethod"()
        response.json.success == true

        where:
        action                    | serviceMethod
        "refreshProductDemand"    | "refreshProductDemandData"
        "refreshTransactionFact"  | "buildTransactionFact"
        "refreshConsumptionFact"  | "buildConsumptionFact"
        "refreshStockoutFact"     | "buildStockoutFact"
    }

    void "refreshProductAvailability forces a full refresh"() {
        when:
        controller.refreshProductAvailability()

        then:
        1 * productAvailabilityService.refreshProductAvailability(Boolean.TRUE)
        response.json.success == true
    }

    @Unroll
    void "#action reports its response time"() {
        when:
        controller."$action"()

        then:
        1 * reportService."$serviceMethod"()
        response.json.responseTime.endsWith(" ms")

        where:
        action               | serviceMethod
        "buildFacts"         | "buildFacts"
        "truncateFacts"      | "truncateFacts"
        "buildDimensions"    | "buildDimensions"
        "truncateDimensions" | "truncateDimensions"
    }

    void "buildFacts includes the groovy version"() {
        when:
        controller.buildFacts()

        then:
        response.json.groovyVersion == GroovySystem.version
    }

    private List<BinLocationItem> binLocationItems() {
        Location binA = new Location(name: "A-01").save(validate: false)
        InventoryItem lot1 = new InventoryItem(product: aspirin, lotNumber: "LOT1",
                expirationDate: Date.parse("yyyy-MM-dd", "2030-05-01")).save(validate: false)
        InventoryItem lot2 = new InventoryItem(product: bandage, lotNumber: "LOT2").save(validate: false)
        return [
                new BinLocationItem(product: aspirin, inventoryItem: lot1, binLocation: binA, quantity: 10, status: "inStock"),
                new BinLocationItem(product: bandage, inventoryItem: lot2, binLocation: null, quantity: 4, status: "inStock"),
        ]
    }

    void "exportBinLocation renders bin location quantities with values for finance users"() {
        given:
        userService.hasRoleFinance(_) >> true
        inventoryService.getQuantityByBinLocation(warehouse) >> binLocationItems()

        when:
        controller.exportBinLocation()
        def json = response.json

        then:
        json.binLocationCount == 2
        json.productCount == 2
        json.binLocations[0].productCode == "AS01"
        json.binLocations[0].productName == "Aspirin"
        json.binLocations[0].binLocation == "A-01"
        json.binLocations[0].quantity == "10"
        json.binLocations[0].unitCost == "2"
        json.binLocations[0].totalValue == "20"
        json.binLocations[1].binLocation == "Default Bin"
    }

    void "exportBinLocation downloads CSV without values for other users"() {
        given:
        userService.hasRoleFinance(_) >> false
        inventoryService.getQuantityByBinLocation(warehouse) >> binLocationItems()
        params.downloadFormat = "csv"

        when:
        controller.exportBinLocation()
        String csv = response.text

        then:
        response.contentType.startsWith("text/csv")
        response.getHeader("Content-disposition") == "attachment; filename=\"Bin Locations - Warehouse.csv\""
        csv.contains("productCode")
        csv.contains("AS01")
        csv.contains("A-01")
        csv.readLines()[1].endsWith(",,,")
    }

    void "exportDemandReport renders demand rows as JSON or CSV"() {
        given:
        ForecastingService forecastingService = Mock()
        controller.forecastingService = forecastingService

        when:
        controller.exportDemandReport()

        then:
        1 * forecastingService.getDemandDetails(warehouse, null) >> [[productCode: "AS01", demand: 3]]
        response.json.count == 1
        response.json.data[0].demand == 3

        when:
        response.reset()
        params.downloadFormat = "csv"
        controller.exportDemandReport()

        then:
        1 * forecastingService.getDemandDetails(warehouse, null) >> [[productCode: "AS01", demand: 3]]
        response.getHeader("Content-disposition") == "attachment; filename=\"Product Demand - Warehouse.csv\""
        response.text.contains("productCode,demand,")
        response.text.contains("AS01,3,")
    }

    void "exportInventoryReport writes the rows of the selected statuses"() {
        given:
        params.status = ["IN_STOCK", "STOCK_OUT"]
        reportService.calculateQuantityOnHandByProductGroup(warehouse.id) >> [productGroupDetails: [
                IN_STOCK : [g1: [status: "IN_STOCK", name: "Painkillers", productCodes: ["AS01"], minQuantity: 1,
                                 reorderQuantity: 2, maxQuantity: 3, onHandQuantity: 10, totalValue: 20]],
                STOCK_OUT: [g2: [status: "STOCK_OUT", name: "Dressings", productCodes: ["BA01", "BA02"], minQuantity: 0,
                                 reorderQuantity: 0, maxQuantity: 0, onHandQuantity: 0, totalValue: 0]],
        ]]

        when:
        controller.exportInventoryReport()
        List<String> lines = response.text.readLines()

        then:
        response.getHeader("Content-disposition") == "attachment; filename=\"Stock report - Warehouse.csv\""
        lines[0].endsWith("Status,Product group,Product codes,Min,Reorder,Max,QoH,Value")
        lines[1] == "IN_STOCK,Painkillers,AS01,1,2,3,10,20,"
        lines[2] == "STOCK_OUT,Dressings,\"BA01,BA02\",0,0,0,0,0,"
    }

    void "showInventorySamplingReport writes a CSV of sampled inventory items"() {
        given:
        InventoryItem lot = new InventoryItem(product: aspirin, lotNumber: "LOT\"1",
                expirationDate: Date.parse("yyyy-MM-dd", "2030-01-01")).save(validate: false)
        params.n = "3"

        when:
        controller.showInventorySamplingReport()
        List<String> lines = response.text.readLines()

        then:
        1 * inventoryService.getInventorySampling(warehouse, 3) >> [lot, null]
        response.contentType.startsWith("text/csv")
        lines[0] == "Product Code,Product,Lot number,Expiration date,Bin location,On hand quantity,"
        lines[1].startsWith('"AS01","Aspirin","LOT""1",')
        lines.size() == 2
    }

    void "showInventorySamplingReport writes the error message when sampling fails"() {
        when:
        controller.showInventorySamplingReport()

        then:
        1 * inventoryService.getInventorySampling(warehouse, 10) >> { throw new RuntimeException("not enough items") }
        response.text == "not enough items"
    }

    void "showInventoryReport has no model"() {
        expect:
        controller.showInventoryReport() == null
    }

    @PendingFeature(reason = 'OBAF showConsumptionReport renders a missing view instead of redirecting to consumption/show')
    void "showConsumptionReport sends the user to the consumption report"() {
        when:
        controller.showConsumptionReport()

        then:
        response.redirectedUrl == "/consumption/show"
    }

    void "showTransactionReportDialog renders the data table dialog pointing at the details endpoint"() {
        given:
        views['/report/_dataTableDialog.gsp'] = 'url=${url}'
        params.productCode = "AS01"

        when:
        controller.showTransactionReportDialog()

        then:
        response.text.startsWith("url=/json/getTransactionReportDetails")
        response.text.contains("productCode=AS01")
    }

    void "the packing list and shipping reports generate the report for a valid command"() {
        given:
        ProductService productService = Mock()
        controller.productService = productService
        Category root = new Category(name: "ROOT")
        ChecklistReportCommand command = new ChecklistReportCommand()

        when:
        def model = controller."$action"(command)

        then:
        (1.._) * productService.getRootCategory() >> root
        1 * reportService.generateShippingReport(command)
        model.command.is(command)
        command.rootCategory.is(root)

        where:
        action << ["showPaginatedPackingListReport", "printShippingReport", "printPaginatedPackingListReport"]
    }

    void "printPaginatedPackingListReport survives a report failure"() {
        given:
        controller.productService = Stub(ProductService)
        reportService.generateShippingReport(_) >> { throw new IllegalStateException("boom") }

        when:
        def model = controller.printPaginatedPackingListReport(new ChecklistReportCommand())

        then:
        noExceptionThrown()
        model.command != null
    }

    void "printPickListReport adds the bin locations of the shipment"() {
        given:
        Shipment shipment = new Shipment(name: "S1").save(validate: false)
        ChecklistReportCommand command = new ChecklistReportCommand(shipment: shipment)

        when:
        def model = controller.printPickListReport(command)

        then:
        1 * reportService.generateShippingReport(command)
        1 * inventoryService.getBinLocations(shipment) >> [(aspirin): ["A-01"]]
        model.binLocations[aspirin] == ["A-01"]
    }

    void "downloadShippingReport streams a docx checklist"() {
        given:
        File docx = File.createTempFile("checklist", ".docx")
        docx.bytes = "DOCX".bytes
        params.format = "docx"

        when:
        controller.downloadShippingReport()

        then:
        1 * documentService.generateChecklistAsDocx() >> docx
        response.contentType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        response.contentAsByteArray == "DOCX".bytes

        cleanup:
        docx.delete()
    }

    void "downloadShippingReport rejects unknown formats"() {
        given:
        params.format = "xls"

        when:
        controller.downloadShippingReport()

        then:
        UnsupportedOperationException e = thrown()
        e.message == "Format 'xls' not supported"
    }

    void "clearBinLocationCache redirects to the bin location report"() {
        when:
        controller.clearBinLocationCache()

        then:
        flash.message == "Cache have been flushed"
        response.redirectedUrl == "/report/showBinLocationReport"
    }

    void "showBinLocationReport renders the page model by default"() {
        when:
        def model = controller.showBinLocationReport()

        then:
        model.location == warehouse
        model.statuses == ["inStock", "outOfStock"]
        0 * productAvailabilityService._
    }

    void "showBinLocationReport downloads the stock report filtered by status"() {
        given:
        params.downloadAction = "downloadStockReport"
        params.status = "inStock"
        List items = binLocationItems().collect {
            [status: it.status, product: it.product, inventoryItem: it.inventoryItem, binLocation: it.binLocation,
             quantity: it.quantity, quantityAvailableToPromise: it.quantity - 1, unitCost: 1, totalValue: it.quantity]
        }
        items << [status: "outOfStock", product: aspirin, quantity: 0]

        when:
        controller.showBinLocationReport()
        List<String> lines = response.text.readLines()

        then:
        1 * productAvailabilityService.getQuantityOnHandByBinLocation(warehouse) >> items
        response.getHeader("Content-disposition") == "attachment; filename=\"Bin Location Report - Warehouse - inStock.csv\""
        lines.size() == 3
        lines[0].count(",") == 14
        lines[1].startsWith("inStock,AS01,Aspirin,,Medicines,,,LOT1,01/May/2030,,A-01,10,9,1,10")
        lines[2].contains(",LOT2,,,default.label,4,3,1,4")
    }

    void "showBinLocationReport downloads current stock as stock movement items"() {
        given:
        params.downloadAction = "downloadStockMovement"
        StockMovementService stockMovementService = Mock()
        controller.stockMovementService = stockMovementService
        List entries = [
                [product: aspirin, quantity: 4],
                [product: aspirin, quantity: 6],
                [product: bandage, quantity: 0],
        ]
        List<StockMovement> built = []

        when:
        controller.showBinLocationReport()

        then:
        1 * productAvailabilityService.getQuantityOnHandByBinLocation(warehouse) >> entries
        1 * stockMovementService.buildStockMovementItemList({ StockMovement sm -> built << sm; true }) >> [[code: "AS01"]]
        1 * dataService.generateCsv([[code: "AS01"]]) >> "code\nAS01"
        built[0].lineItems.size() == 1
        built[0].lineItems[0].product == aspirin
        built[0].lineItems[0].quantityRequested == 10
        response.getHeader("Content-disposition") == "attachment; filename=\"StockMovementItems-CurrentStock.csv\""
        response.text == "code\nAS01"
    }

    void "showBinLocationReport reports a download failure in the flash message"() {
        given:
        params.downloadAction = "downloadStockReport"
        productAvailabilityService.getQuantityOnHandByBinLocation(_) >> { throw new IllegalStateException("no data") }

        when:
        def model = controller.showBinLocationReport()

        then:
        flash.message == "no data"
        model.location == warehouse
    }

    void "showOnOrderReport downloads the detailed report of pending order and shipment items"() {
        given:
        controller.orderService = Mock(OrderServiceForReports)
        controller.shipmentService = Mock(ShipmentServiceForReports)
        params.downloadAction = "downloadOnOrderReport"

        Order order = new Order(orderNumber: "PO-1", name: "Purchase", origin: supplier).save(validate: false)
        OrderItem orderItem = Stub(OrderItem) {
            getProduct() >> bandage
            getOrder() >> order
            getQuantityRemaining() >> 3
            getQuantityPerUom() >> 10
            getActualReadyDate() >> Date.parse("yyyy-MM-dd", "2030-02-03")
        }
        Shipment shipment = Stub(Shipment) {
            getIsFromPurchaseOrder() >> false
            getOrigin() >> supplier
            getShipmentNumber() >> "SH-9"
            getExpectedShippingDate() >> Date.parse("yyyy-MM-dd", "2030-01-02")
            getShipmentType() >> new ShipmentType(name: "Air")
        }
        def shipmentItem = [product: aspirin, quantityRemaining: 7, shipment: shipment]

        when:
        controller.showOnOrderReport()
        List<String> lines = response.text.readLines()

        then:
        1 * controller.orderService.getPendingInboundOrderItems(warehouse) >> [orderItem]
        1 * controller.shipmentService.getPendingInboundShipmentItems(warehouse) >> [shipmentItem]
        response.contentType.startsWith("text/csv")
        lines[0].contains("Code")
        lines[0].contains("Shipment Type")
        lines[1].startsWith("\"AS01\",\"Aspirin\"")
        lines[1].contains("\"7\"")
        lines[1].contains("\"SH-9\"")
        lines[1].contains("\"01/02/2030\"")
        lines[1].contains("\"Air\"")
        lines[2].startsWith("\"BA01\",\"Bandage\"")
        lines[2].contains("\"30\"")
        lines[2].contains("\"PO-1\"")
        lines[2].contains("\"02/03/2030\"")
    }

    void "showOnOrderReport downloads the summary report"() {
        given:
        params.downloadAction = "downloadSummaryOnOrderReport"

        when:
        controller.showOnOrderReport()
        List<String> lines = response.text.readLines()

        then:
        1 * reportService.getOnOrderSummary(warehouse) >> [
                [productCode: "BA01", displayNameWithLocaleCode: "Bandage", totalOnOrder: 5, totalOnHand: 1, totalOnHandAndOnOrder: 6],
                [productCode: "AS01", displayNameWithLocaleCode: "Aspirin", totalOnOrder: 2, totalOnHand: 3, totalOnHandAndOnOrder: 5],
        ]
        lines[0].contains("Total On Hand and On Order")
        lines[1].startsWith("\"AS01\",\"Aspirin\"")
        lines[2].startsWith("\"BA01\",\"Bandage\"")
    }

    void "showOnOrderReport without a download action renders nothing special"() {
        when:
        controller.showOnOrderReport()

        then:
        0 * reportService._
        response.text == ""
    }

    void "showInventoryByLocationReport runs the report across the selected locations"() {
        given:
        MultiLocationInventoryReportCommand command = new MultiLocationInventoryReportCommand(actionButton: "run")
        command.locations = [warehouse, supplier]
        command.categories = [medicines]

        when:
        controller.showInventoryByLocationReport(command)

        then:
        1 * inventoryService.getExplodedCategories([medicines]) >> [medicines]
        1 * productAvailabilityService.getQuantityOnHandByProduct([warehouse, supplier], [medicines]) >> [(aspirin): [:]]
        view == "/report/showInventoryByLocationReport"
        model.command.entries.size() == 1
    }

    void "showInventoryByLocationReport downloads quantities per location"() {
        given:
        MultiLocationInventoryReportCommand command = new MultiLocationInventoryReportCommand(actionButton: "download",
                includeSubcategories: false)
        command.locations = [warehouse, supplier]
        command.categories = [medicines]
        productAvailabilityService.getQuantityOnHandByProduct(_, _) >> [
                (aspirin): [(warehouse.id): [quantityOnHand: 10, quantityAvailableToPromise: 8],
                            (supplier.id) : [quantityOnHand: 5, quantityAvailableToPromise: 5]],
                (null)   : [:]
        ]

        when:
        controller.showInventoryByLocationReport(command)
        List<String> lines = response.text.readLines()

        then:
        0 * inventoryService.getExplodedCategories(_)
        response.getHeader("Content-disposition").startsWith("attachment; filename=\"Inventory-by-location-")
        lines[0].endsWith("Code,Product,Product Family,Category,Formularies,Tags,Warehouse,Supplier,QoH Total,Quantity Available Total")
        lines[1] == '"AS01","Aspirin","","Medicines","","","10","5","15","13"'
        lines.size() == 2
    }

    void "showInventoryByLocationReport redisplays an invalid command"() {
        given:
        MultiLocationInventoryReportCommand command = new MultiLocationInventoryReportCommand(actionButton: "explode")

        when:
        controller.showInventoryByLocationReport(command)

        then:
        view == "/report/showInventoryByLocationReport"
        model.command.hasErrors()
        0 * productAvailabilityService._
    }

    void "showRequestDetailReport and expirationHistoryReport render their pages"() {
        when:
        controller.showRequestDetailReport()

        then:
        params.origin == warehouse.id
        view == "/report/showRequestDetailReport"

        when:
        response.reset()
        controller.expirationHistoryReport()

        then:
        view == "/common/react"
    }

    void "showForecastReport renders the page without exporting by default"() {
        when:
        controller.showForecastReport()

        then:
        params.origin == "Warehouse"
        view == "/report/showForecastReport"
        0 * reportService._
    }

    void "showForecastReport exports the forecast as a spreadsheet"() {
        given:
        ForecastingService forecastingService = Mock()
        controller.forecastingService = forecastingService
        params.format = "text/csv"
        params.print = "true"
        params.startDate = "01/01/2024"
        params.endDate = "12/31/2024"
        params.replenishmentPeriodDays = "30"
        params.leadTimeDays = "30"
        List rows = null

        when:
        controller.showForecastReport()

        then:
        1 * reportService.getForecastReport({ it.startDate instanceof Date && it.originId == warehouse.id }) >> [
                [productId: aspirin.id, totalOnOrder: 5, totalOnHand: 10, averageMonthlyDemand: 12.0]
        ]
        1 * forecastingService.getProductExpiry(warehouse, 60, aspirin.id) >> [[quantity_on_hand: 3]]
        1 * documentService.generateExcel(_, _) >> { OutputStream out, List data -> rows = data }
        rows.size() == 1
        rows[0]["Product code"] == "AS01"
        rows[0]["Qty Available"] == 12
        rows[0]["Months of Stock Needed (order period + lead time in months)"] == 2.0
        rows[0]["Qty Needed"] == 24.0
        rows[0]["Qty to Order"] == 12.0
        rows[0]["Estimated Cost"] == 24.0
        response.getHeader("Content-disposition").endsWith(".xls\"")
    }

    void "showForecastReport warns when there is no forecast data"() {
        given:
        controller.forecastingService = Stub(ForecastingService)
        params.format = "text/csv"
        params.print = "true"

        when:
        controller.showForecastReport()

        then:
        1 * reportService.getForecastReport({ it.endDate instanceof Date }) >> []
        0 * documentService.generateExcel(_, _)
        flash.message == "Unable to generate forecast report due to lack of data"
    }

    void "amountOutstandingOnOrdersReport is refused without the finance role"() {
        given:
        userService.hasRoleFinance(_) >> false

        when:
        controller.amountOutstandingOnOrdersReport()

        then:
        flash.message == "You do not have permission to view financial data"
        0 * reportService.getAmountOutstandingOnOrders(_)
    }

    void "amountOutstandingOnOrdersReport exports the rows for finance users"() {
        given:
        userService.hasRoleFinance(_) >> true

        when:
        controller.amountOutstandingOnOrdersReport()

        then:
        1 * reportService.getAmountOutstandingOnOrders(warehouse.id) >> [[order: "PO-1"]]
        1 * documentService.generateExcel(_, [[order: "PO-1"]])
        response.contentType == "application/vnd.ms-excel"
        response.getHeader("Content-disposition").startsWith("attachment; filename=\"AmountOutstandingOnOrders-Warehouse-")
    }

    void "binLocationCsvHeader and binLocationCsvRow are empty without a row"() {
        expect:
        controller.binLocationCsvHeader(null) == ""
        controller.binLocationCsvRow(null) == ""
    }
}

interface OrderServiceForReports {
    def getPendingInboundOrderItems(def location)
}

interface ShipmentServiceForReports {
    def getPendingInboundShipmentItems(def location)
}
