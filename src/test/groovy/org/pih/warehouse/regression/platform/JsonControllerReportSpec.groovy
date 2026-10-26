package org.pih.warehouse.regression.platform

import org.pih.warehouse.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.web.DefaultGrailsTagDateHelper
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.pih.warehouse.core.LocalizationService
import org.grails.plugins.web.taglib.ValidationTagLib
import spock.lang.Specification

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PreferenceType
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.core.ValidationCode
import org.pih.warehouse.core.DashboardService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.InventorySnapshotService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductSupplierPreference
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * Covers the JsonController actions that feed report data tables and exports.
 */
class JsonControllerReportSpec extends Specification implements ControllerUnitTest<JsonController>, DataTest {

    Closure doWithSpring() {{ ->
        grailsTagDateHelper(DefaultGrailsTagDateHelper)
    }}

    Location warehouse
    Location supplier
    Product aspirin
    Category medicines

    void setupSpec() {
        mockDomains(Location, Organization, Product, Category, Tag, ProductCatalog, InventoryItem, User, Order, OrderItem,
                Shipment, ShipmentType, ProductSupplier, ProductSupplierPreference, PreferenceType, ProductPackage,
                ProductPrice, UnitOfMeasure)
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        mockTagLib(MessageTagLib)
        defineBeans {
            localizationService(InstanceFactoryBean, Stub(LocalizationService) {
                getCurrentLocale() >> Locale.ENGLISH
            }, LocalizationService)
        }
        warehouse = new Location(name: "Warehouse").save(validate: false)
        supplier = new Location(name: "Supplier", organization: new Organization(name: "Acme", code: "ACME").save(validate: false)).save(validate: false)
        medicines = new Category(name: "Medicines").save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01", pricePerUnit: 2.0, unitOfMeasure: "EA", category: medicines).save(validate: false)
        session.warehouse = warehouse
        session.user = new User(username: "manager")
    }

    void "calculateQuantityOnHandByProduct renders a JSONP payload with stock counts by status"() {
        given:
        Product bandage = new Product(name: "Bandage", productCode: "BA01", pricePerUnit: 0.5).save(validate: false)
        controller.inventoryService = Stub(InventoryService) {
            getQuantityByProductMap(warehouse.id) >> [(aspirin): 10, (bandage): 0]
        }
        controller.dashboardService = Stub(DashboardService) {
            getInventoryStatusAndLevel(warehouse) >> [
                    (aspirin): [inventoryStatus: "IN_STOCK", quantity: 10, inventoryLevel: [minQuantity: 2, maxQuantity: 20, reorderQuantity: 5]],
                    (bandage): [inventoryStatus: "STOCK_OUT", quantity: 0]
            ]
        }
        params.callback = "cb"

        when:
        controller.calculateQuantityOnHandByProduct()
        String body = response.text

        then:
        response.contentType.startsWith("application/javascript")
        body.startsWith("cb(")
        body.contains('"allStockCount":2')
        body.contains('"inStockCount":1')
        body.contains('"outOfStockCount":1')
        body.contains('"totalValue":20')
        body.contains('"reorderQuantity":5')
        body.contains('"genericProduct":"Empty"')
    }

    void "getInventorySnapshotDetails defaults to the most recent snapshot date"() {
        given:
        Date snapshotDate = Date.parse("yyyy-MM-dd", "2024-05-01")
        InventoryItem lot = new InventoryItem(product: aspirin, lotNumber: "L1",
                expirationDate: Date.parse("yyyy-MM-dd", "2030-12-31")).save(validate: false)
        controller.inventorySnapshotService = Mock(InventorySnapshotService)
        InventorySnapshotCommand command = new InventorySnapshotCommand(product: aspirin, location: warehouse)

        when:
        controller.getInventorySnapshotDetails(command)
        def rows = response.json.data

        then:
        1 * controller.inventorySnapshotService.getMostRecentInventorySnapshotDate() >> snapshotDate
        1 * controller.inventorySnapshotService.getInventorySnapshots(aspirin, warehouse, snapshotDate) >> [
                [date: snapshotDate, productCode: "AS01", lotNumber: "L1", inventoryItem: lot, binLocationName: "A-1", quantityOnHand: 7],
                [date: snapshotDate, productCode: "AS01", lotNumber: null, inventoryItem: null, binLocationName: null, quantityOnHand: 3],
        ]
        rows.size() == 2
        rows[0].lotNumber == "L1"
        rows[0].binLocation == "A-1"
        rows[0].quantityOnHand == 7
        rows[1].lotNumber == "default.label"
        rows[1].expirationDate == "default.never.label"
    }

    void "getQuantityOnHandByMonth turns snapshot rows into a time series"() {
        given:
        Date first = Date.parse("yyyy-MM-dd", "2024-01-01")
        controller.inventorySnapshotService = Mock(InventorySnapshotService)
        params['location.id'] = warehouse.id
        params['product.id'] = aspirin.id
        params.numMonths = "3"

        when:
        controller.getQuantityOnHandByMonth()

        then:
        1 * controller.inventorySnapshotService.getMostRecentInventorySnapshotDate() >> null
        1 * controller.inventorySnapshotService.getQuantityOnHandBetweenDates(aspirin, warehouse,
                { Date start -> start < new Date() - 80 && start > new Date() - 100 }, _ as Date) >> [[first, "x", "y", 12]]
        response.json.data == [[first.time, 12]]
    }

    void "getBinLocationSummary summarizes the bin locations of the requested location"() {
        given:
        controller.productAvailabilityService = Stub(ProductAvailabilityServiceForBins) {
            getQuantityOnHandByBinLocation(warehouse) >> [[status: "inStock"]]
        }
        controller.inventoryService = Mock(InventoryService)
        params['location.id'] = warehouse.id

        when:
        controller.getBinLocationSummary()

        then:
        1 * controller.inventoryService.getBinLocationSummary([[status: "inStock"]]) >> [[status: "inStock", count: 1]]
        response.json[0].count == 1
    }

    void "getBinLocationReport flattens bin rows and shows values only to finance users"() {
        given:
        InventoryItem lot = new InventoryItem(product: aspirin, lotNumber: "L1",
                expirationDate: Date.parse("yyyy-MM-dd", "2030-12-31")).save(validate: false)
        Location bin = new Location(name: "A-01").save(validate: false)
        controller.productAvailabilityService = Stub(ProductAvailabilityServiceForBins) {
            getQuantityOnHandByBinLocation(warehouse) >> [
                    [status: "inStock", product: aspirin, inventoryItem: lot, binLocation: bin, quantity: 4, quantityAvailableToPromise: 3],
                    [status: "outOfStock", product: aspirin, quantity: 0],
            ]
        }
        controller.userService = Stub(JsonUserServiceStub) { hasRoleFinance(_) >> finance }
        params.status = "inStock"

        when:
        controller.getBinLocationReport()
        def rows = response.json.aaData

        then:
        rows.size() == 1
        rows[0].productCode == "AS01"
        rows[0].lotNumber == "L1"
        rows[0].binLocation == "A-01"
        rows[0].expirationDate == "31/Dec/2030"
        rows[0].quantity == 4
        rows[0].quantityAvailableToPromise == 3
        rows[0].unitCost == unitCost
        rows[0].totalValue == totalValue

        where:
        finance | unitCost | totalValue
        true    | 2.0      | "8"
        false   | null     | null
    }

    void "getDetailedOrderReport combines pending order items and shipment items"() {
        given:
        Order order = new Order(orderNumber: "PO-1", name: "Purchase", origin: supplier).save(validate: false)
        OrderItem orderItem = Stub(OrderItem) {
            getProduct() >> aspirin
            getOrder() >> order
            getQuantityRemaining() >> 2
            getQuantityPerUom() >> 5
            getActualReadyDate() >> Date.parse("yyyy-MM-dd", "2030-03-04")
        }
        Shipment shipment = Stub(Shipment) {
            getIsFromPurchaseOrder() >> true
            getOrigin() >> supplier
            getShipmentNumber() >> "SH-1"
            getShipmentType() >> new ShipmentType(name: "Sea")
        }
        def shipmentItem = [product: aspirin, quantityRemaining: 6, shipment: shipment, orderNumber: "PO-2", orderName: "Second"]
        controller.orderService = Stub(JsonOrderServiceStub) { getPendingInboundOrderItems(warehouse) >> [orderItem] }
        controller.shipmentService = Stub(JsonShipmentServiceStub) { getPendingInboundShipmentItems(warehouse) >> [shipmentItem] }

        when:
        controller.getDetailedOrderReport()
        def rows = response.json.aaData

        then:
        rows.size() == 2
        rows[0].qtyOrderedNotShipped == 10
        rows[0].orderNumber == "PO-1"
        rows[0].supplierOrganization == "Acme"
        rows[0].estimatedGoodsReadyDate == "03/04/2030"
        rows[1].qtyShippedNotReceived == 6
        rows[1].orderNumber == "PO-2"
        rows[1].orderDescription == "Second"
        rows[1].shipmentNumber == "SH-1"
        rows[1].shipmentType == "Sea"
    }

    private TransactionReportCommand transactionCommand(String start, String end) {
        return new TransactionReportCommand(startDate: Date.parse("yyyy-MM-dd", start),
                endDate: Date.parse("yyyy-MM-dd", end), location: warehouse, category: medicines)
    }

    void "getTransactionReport renders report rows for the selected filters"() {
        given:
        Tag tag = new Tag(tag: "cold").save(validate: false)
        ProductCatalog catalog = new ProductCatalog(name: "Essential").save(validate: false)
        controller.reportService = Mock(ReportServiceStub)
        controller.productService = Stub(ProductService)
        params.tags = tag.id
        params.catalogs = catalog.id
        TransactionReportCommand command = transactionCommand("2024-01-01", "2024-01-31")

        when:
        controller.getTransactionReport(command)

        then:
        1 * controller.reportService.getTransactionReport(warehouse, [medicines], [tag], [catalog], null,
                command.startDate, { Date end -> end.format("yyyy-MM-dd") == "2024-02-01" }, false) >> [[productCode: "AS01"]]
        response.json.aaData[0].productCode == "AS01"
    }

    void "getTransactionReport downloads CSV including child categories"() {
        given:
        Category child = new Category(name: "Tablets", parentCategory: medicines).save(validate: false)
        medicines.addToCategories(child)
        controller.reportService = Mock(ReportServiceStub)
        controller.dataService = Mock(DataService)
        params.format = "text/csv"
        params.includeCategoryChildren = "true"

        when:
        controller.getTransactionReport(transactionCommand("2024-01-01", "2024-01-31"))

        then:
        1 * controller.reportService.getTransactionReport(warehouse, { it as Set == [medicines, child] as Set }, [], [], null, _, _, true) >> [[a: 1]]
        1 * controller.dataService.generateCsv([[a: 1]]) >> "a\n1"
        response.getHeader("Content-disposition") == "attachment; filename=\"Transaction-Report-20240101-20240131.csv\""
        response.text == "a\n1"
    }

    void "getTransactionReport validates the date range"() {
        given:
        controller.reportService = Mock(ReportServiceStub)

        when:
        controller.getTransactionReport(transactionCommand(start, end))

        then:
        IllegalArgumentException e = thrown()
        e.message == message
        0 * controller.reportService._

        where:
        start        | end          | message
        "2024-02-10" | "2024-02-01" | "Start date must occur before end date"
        "2024-01-01" | "2099-01-01" | "End date must occur on or before today"
    }

    void "getTransactionReport falls back to the current location and the root category"() {
        given:
        Category root = new Category(name: "ROOT").save(validate: false)
        controller.productService = Stub(ProductService) { getRootCategory() >> root }
        controller.reportService = Mock(ReportServiceStub)
        TransactionReportCommand command = new TransactionReportCommand(startDate: Date.parse("yyyy-MM-dd", "2024-01-01"),
                endDate: Date.parse("yyyy-MM-dd", "2024-01-02"))
        AuthService authService = new AuthService()
        authService.setCurrentLocation(supplier)

        when:
        controller.getTransactionReport(command)

        then:
        1 * controller.reportService.getTransactionReport(supplier, [root], [], [], null, _, _, false) >> []
        response.json.aaData.size() == 0

        cleanup:
        authService.setCurrentLocation(null)
    }

    void "getTransactionReportDetails renders the modal rows for a product code"() {
        given:
        controller.reportService = Mock(ReportServiceStub)
        params.productCode = "AS01"
        TransactionReportCommand command = transactionCommand("2024-01-01", "2024-01-31")

        when:
        controller.getTransactionReportDetails(command)

        then:
        1 * controller.reportService.getTransactionReportModalData(warehouse, aspirin, command.startDate, _) >> [[qty: 1]]
        response.json.aaData[0].qty == 1
    }

    void "productChanged lists the product suppliers that are not hidden for the destination"() {
        given:
        Organization vendor = new Organization(name: "Vendor", code: "V").save(validate: false)
        Organization buyer = new Organization(name: "Buyer", code: "B").save(validate: false)
        controller.dataService = Mock(DataService)
        params.productId = aspirin.id
        params.supplierId = vendor.id
        params.destinationPartyId = buyer.id

        when:
        controller.productChanged()

        then:
        1 * controller.dataService.executeQuery(_ as String, [supplierId: vendor.id, productId: aspirin.id,
                destinationPartyId: buyer.id, validationCode: "HIDE"]) >> [
                [id: "ps1", code: "PS-1", supplier_code: "SUP-1", name: "Aspirin 100", manufacturer_code: "M-1", manufacturer_id: "m1"]
        ]
        response.json.productSupplierOptions[0].text == "PS-1 Aspirin 100"
        response.json.productSupplierOptions[0].supplierCode == "SUP-1"
        response.json.productSupplierOptions[0].manufacturerCode == "M-1"
    }

    void "productChanged without a supplier renders no options"() {
        given:
        controller.dataService = Mock(DataService)

        when:
        params.productId = aspirin.id
        controller.productChanged()

        then:
        0 * controller.dataService._
        response.json.productSupplierOptions.size() == 0
    }

    void "productSupplierChanged renders the default package price and the preference for the destination"() {
        given:
        Organization buyer = new Organization(name: "Buyer", code: "B").save(validate: false)
        PreferenceType hidden = new PreferenceType(name: "Do not use", validationCode: ValidationCode.HIDE).save(validate: false)
        PreferenceType preferred = new PreferenceType(name: "Preferred", validationCode: ValidationCode.DEFAULT).save(validate: false)
        UnitOfMeasure box = new UnitOfMeasure(name: "Box", code: "BX").save(validate: false)
        ProductSupplier productSupplier = new ProductSupplier(code: "PS-1", supplierCode: "SUP-1", manufacturerCode: "M-1",
                minOrderQuantity: 10, product: aspirin).save(validate: false)
        ProductPackage productPackage = new ProductPackage(name: "Box of 10", quantity: 10, uom: box,
                productPrice: new ProductPrice(price: 12).save(validate: false), productSupplier: productSupplier).save(validate: false)
        productSupplier.addToProductPackages(productPackage)
        productSupplier.addToProductSupplierPreferences(new ProductSupplierPreference(destinationParty: buyer, preferenceType: hidden))
        productSupplier.addToProductSupplierPreferences(new ProductSupplierPreference(destinationParty: null, preferenceType: preferred))
        productSupplier.save(validate: false)
        params.productSupplierId = productSupplier.id

        when:
        params.destinationPartyId = buyer.id
        controller.productSupplierChanged()

        then:
        response.json.unitPrice == "12"
        response.json.supplierCode == "SUP-1"
        response.json.quantityPerUom == 10
        response.json.minOrderQuantity == 10
        response.json.validationCode.toString().contains("HIDE")

        when: "a destination without its own preference uses the global one"
        response.reset()
        params.destinationPartyId = "other"
        controller.productSupplierChanged()

        then:
        response.json.validationCode.toString().contains("DEFAULT")
    }

    void "getRequestDetailReport requires a date range"() {
        when:
        controller.getRequestDetailReport()

        then:
        IllegalArgumentException e = thrown()
        e.message == "Start and end date are required"
    }

    void "getRequestDetailReport renders rows as JSON or as a CSV download"() {
        given:
        ForecastingService forecastingService = Mock()
        controller.forecastingService = forecastingService
        Map row = [requestNumber: "R-1", dateRequested: "2024-01-02", origin: "Warehouse", destination: "Ward",
                   productCode: "AS01", productName: "Aspirin", quantityRequested: 5, quantityIssued: 4, quantityDemand: 5,
                   reasonCode: "STOCKOUT"]
        params.startDate = "01/01/2024"
        params.endDate = "01/31/2024"

        when:
        controller.getRequestDetailReport()

        then:
        1 * forecastingService.getRequestDetailReport({ it.startDate instanceof Date && it.endDate instanceof Date }) >> [row]
        response.json.aaData[0].requestNumber == "R-1"

        when:
        response.reset()
        params.startDate = "01/01/2024"
        params.endDate = "01/31/2024"
        params.format = "text/csv"
        controller.getRequestDetailReport()
        List<String> lines = response.text.readLines()

        then:
        1 * forecastingService.getRequestDetailReport(_) >> [row]
        response.getHeader("Content-disposition") == "attachment; filename=\"Request-Detail-Report.csv\""
        lines[0].contains("\"Request Number\"")
        lines[0].contains("\"Reason Code Classification\"")
        lines[1].startsWith("\"R-1\",\"2024-01-02\"")
        lines[1].contains("\"STOCKOUT\"")
    }
}

interface ProductAvailabilityServiceForBins {
    def getQuantityOnHandByBinLocation(def location)
}

interface JsonUserServiceStub {
    boolean hasRoleFinance(def user)
}

interface JsonOrderServiceStub {
    def getPendingInboundOrderItems(def location)
}

interface JsonShipmentServiceStub {
    def getPendingInboundShipmentItems(def location)
}
