package org.pih.warehouse.regression.platform.data

import org.pih.warehouse.data.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.validation.BeanPropertyBindingResult
import spock.lang.Specification

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.ConsumptionFact
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.LocationDimension
import org.pih.warehouse.reporting.LotDimension
import org.pih.warehouse.reporting.ProductDimension
import org.pih.warehouse.reporting.TransactionFact
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class MigrationControllerSpec extends Specification implements ControllerUnitTest<MigrationController>, DataTest {

    Location depot
    Product aspirin
    MigrationService migrationService = Mock()
    TransactionSourceMigrationService transactionSourceMigrationService = Mock()
    DataService dataService = Mock()

    void setupSpec() {
        mockDomains(Location, Inventory, Product, Transaction, TransactionType, LocationDimension, ProductDimension,
                LotDimension, DateDimension, TransactionFact, ConsumptionFact, Shipment)
    }

    void setup() {
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)
        depot = new Location(name: "Depot").save(validate: false)
        depot.inventory = new Inventory(warehouse: depot).save(validate: false)
        aspirin = new Product(name: "Aspirin", productCode: "AS01").save(validate: false)
        session.warehouse = depot
        controller.migrationService = migrationService
        controller.transactionSourceMigrationService = transactionSourceMigrationService
        controller.dataService = dataService
        views['/migration/_status.gsp'] = 'status: ${message ?: command?.errors?.allErrors?.size()}'
        views['/common/_dataTable.gsp'] = 'rows: ${data.size()}'
    }

    void "index and dataQuality have no model"() {
        expect:
        controller.index() == null
        controller.dataQuality() == null
    }

    void "dataMigration summarizes what is left to migrate"() {
        given:
        TransactionType productInventory = new TransactionType(name: "Product inventory")
        productInventory.id = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
        productInventory.save(validate: false)
        new Transaction(transactionType: productInventory, inventory: depot.inventory).save(validate: false)

        when:
        def model = controller.dataMigration()

        then:
        1 * migrationService.getSuppliersForMigration() >> [1, 2]
        1 * migrationService.getProductsForMigration() >> [1, 2, 3]
        1 * migrationService.getProductsWithTransactions(depot, _ as TransactionType) >> [aspirin]
        1 * migrationService.getOtherOverlappingTransactions(depot, _) >> [AS01: ["t1"]]
        1 * transactionSourceMigrationService.getAmountOfMissingInventoryImportTransactionSources() >> 0
        1 * transactionSourceMigrationService.getAmountOfMissingCycleCountTransactionSources() >> 0
        1 * transactionSourceMigrationService.getAmountOfMissingRecordStockTransactionSources() >> 4
        model.organizationCount == 2
        model.productSupplierCount == 3
        model.productInventoryTransactionCount == 1
        model.productInventoryTransactionInCurrentLocationCount == 1
        model.productsWithProductInventoryTransactionInCurrentLocation == ["AS01"]
        model.overlappingTransactions == [AS01: ["t1"]]
        model.amountOfMissingRecordStockTransactionSources == 4
    }

    void "dataMigration only counts record stock sources once the earlier migrations are done"() {
        when:
        def model = controller.dataMigration()

        then:
        1 * transactionSourceMigrationService.getAmountOfMissingInventoryImportTransactionSources() >> 2
        1 * transactionSourceMigrationService.getAmountOfMissingCycleCountTransactionSources() >> 0
        0 * transactionSourceMigrationService.getAmountOfMissingRecordStockTransactionSources()
        migrationService.getSuppliersForMigration() >> []
        migrationService.getProductsForMigration() >> []
        model.amountOfMissingRecordStockTransactionSources == null
    }

    void "the table counts come from the reporting tables"() {
        given:
        new LocationDimension(locationName: "Depot").save(validate: false)

        when:
        def dimensions = controller.dimensionTables()
        def facts = controller.factTables()
        def views = controller.materializedViews()

        then:
        1 * dataService.executeQuery("select count(*) as count from stockout_fact") >> [[count: 7]]
        1 * dataService.executeQuery("select count(*) as count from product_demand_details") >> [[count: 3]]
        1 * dataService.executeQuery("select count(*) as count from product_availability") >> []
        dimensions.locationDimensionCount == 1
        dimensions.productDimensionCount == 0
        facts.stockoutFactCount == 7
        facts.transactionFactCount == 0
        views.productDemandCount == 3
        views.productAvailabilityCount == 0
    }

    void "the data checks render a count or a table"() {
        given:
        migrationService.getStockMovementsWithoutShipmentItems() >> [
                [id: "r1", request_number: "R1", status: "PENDING", date_created: new Date()],
                [id: "r2", request_number: "R2", status: "PENDING", date_created: new Date()],
        ]

        when:
        params.format = "count"
        controller.stockMovementsWithoutShipmentItems()

        then:
        response.text == "2"

        when:
        response.reset()
        params.remove("format")
        controller.stockMovementsWithoutShipmentItems()

        then:
        response.text == "rows: 2"
    }

    void "receipts and shipments without transactions are counted"() {
        when:
        params.format = "count"
        controller."$action"()

        then:
        1 * migrationService."$serviceMethod"() >> [1, 2, 3]
        response.text == "3"

        where:
        action                          | serviceMethod
        "receiptsWithoutTransaction"    | "getReceiptsWithoutTransaction"
        "shipmentsWithoutTransactions"  | "getShipmentsWithoutTransactions"
    }

    void "shipments without transactions are listed with links"() {
        given:
        Location ward = new Location(name: "Ward").save(validate: false)
        Shipment shipment = Stub(Shipment) {
            getId() >> "s1"
            getShipmentNumber() >> "SH-1"
            getCurrentStatus() >> ShipmentStatusCode.SHIPPED
            getOrigin() >> depot
            getDestination() >> ward
        }

        when:
        controller.shipmentsWithoutTransactions()

        then:
        1 * migrationService.getShipmentsWithoutTransactions() >> [shipment]
        response.text == "rows: 1"
    }

    void "product availability can be calculated and refreshed for a location"() {
        given:
        ProductAvailabilityService productAvailabilityService = Mock()
        controller.productAvailabilityService = productAvailabilityService

        when:
        params.id = depot.id
        controller.calculateProductAvailability()

        then:
        1 * productAvailabilityService.calculateBinLocations(depot) >> [1, 2]
        response.text == "2"

        when:
        response.reset()
        params['location.id'] = depot.id
        params.location = [id: depot.id]
        controller.refreshProductAvailability()

        then:
        1 * productAvailabilityService.refreshProductAvailability(depot, true)
        response.text == "Refreshed product availability for location Depot"
    }

    void "downloadCurrentInventory renders the current inventory as JSON or CSV"() {
        when:
        controller.downloadCurrentInventory()

        then:
        1 * migrationService.getCurrentInventory([depot]) >> [[productCode: "AS01", quantity: 4]]
        response.json.count == 1
        response.json.results[0].quantity == 4

        when:
        response.reset()
        params.format = "csv"
        controller.downloadCurrentInventory()

        then:
        1 * migrationService.getCurrentInventory([depot]) >> [[productCode: "AS01"]]
        1 * dataService.generateCsv([[productCode: "AS01"]]) >> "productCode\nAS01"
        response.getHeader("Content-disposition") == "attachment; filename='CurrentInventory_Depot.csv'"
        response.text == "productCode\nAS01"
    }

    void "locations and products with inventory transactions are listed"() {
        when:
        controller.locationsWithInventoryTransactions()

        then:
        1 * migrationService.getLocationsWithTransactions([TransactionCode.INVENTORY]) >> [[locationId: "l1"]]
        response.json.count == 1

        when:
        response.reset()
        controller.locationsWithProductInventoryTransactions()

        then:
        1 * migrationService.getLocationsWithTransaction(_) >> [[locationId: "l1"], [locationId: "l2"]]
        response.json.count == 2

        when:
        response.reset()
        controller.productsWithInventoryTransactions()

        then:
        1 * migrationService.getProductsWithTransactions(depot, [TransactionCode.INVENTORY]) >> [aspirin]
        response.json.products[0].productCode == "AS01"
    }

    void "nextInventoryTransaction opens the stock card of the next product to migrate"() {
        when:
        controller.nextInventoryTransaction()

        then:
        1 * migrationService.getProductsWithTransactions(depot, [TransactionCode.INVENTORY]) >> [aspirin]
        response.redirectedUrl == "/inventoryItem/showStockCard/${aspirin.id}"

        when:
        response.reset()
        controller.nextInventoryTransaction()

        then:
        1 * migrationService.getProductsWithTransactions(depot, [TransactionCode.INVENTORY]) >> []
        response.text == "No inventory transactions"
    }

    void "migrateProduct reports success or failure on the stock card"() {
        when:
        params.id = aspirin.id
        controller.migrateProduct()

        then:
        1 * migrationService.migrateInventoryTransactions(depot, aspirin, true) >> [:]
        flash.message == "Migrated product AS01"
        response.redirectedUrl == "/inventoryItem/showStockCard/${aspirin.id}"

        when:
        response.reset()
        controller.migrateProduct()

        then:
        1 * migrationService.migrateInventoryTransactions(depot, aspirin, true) >> { throw new IllegalStateException("bad entry") }
        flash.message == "Unable to migrate product AS01 due to error: bad entry"
    }

    void "migrateInventoryTransactions previews the migration as JSON or CSV"() {
        when:
        params.performMigration = "true"
        controller.migrateInventoryTransactions()

        then:
        1 * migrationService.migrateInventoryTransactions(depot, true) >> [a: 1, b: 2]
        response.json.count == 2

        when:
        response.reset()
        params.format = "csv"
        controller.migrateInventoryTransactions()

        then:
        1 * migrationService.migrateInventoryTransactions(depot, true) >> [stockHistory: [], a: 1]
        1 * dataService.generateCsv([a: 1]) >> "a\n1"
        response.text == "a\n1"
    }

    void "migrateProductInventoryTransactions reports what it did"() {
        when:
        controller.migrateProductInventoryTransactions()

        then:
        1 * migrationService.migrateProductInventoryTransactions(depot, false) >> [migrated: 0]
        response.json.results.migrated == 0
        response.json.responseTime.startsWith("Generating preview of product inventory transactions at location Depot took")
    }

    void "missing transaction sources are created for the current location"() {
        given:
        AuthService authService = new AuthService()
        authService.setCurrentLocation(depot)

        when:
        controller."$action"()

        then:
        1 * transactionSourceMigrationService."$serviceMethod"(depot) >> [inventoryCounts: 3, transactionSourcesCreated: 2]
        response.json.inventoryCounts == 3
        response.json.transactionSourcesCreated == 2

        cleanup:
        authService.setCurrentLocation(null)

        where:
        action                                                              | serviceMethod
        "createMissingInventoryImportTransactionSourcesForCurrentLocation" | "createMissingInventoryImportTransactionSources"
        "createMissingCycleCountTransactionSourcesForCurrentLocation"      | "createMissingCycleCountTransactionSources"
        "createMissingRecordStockTransactionSourcesForCurrentLocation"     | "createMissingRecordStockTransactionSources"
    }

    void "organization and product supplier migrations report their status"() {
        when:
        controller.migrateProductSuppliers(new MigrationCommand())

        then:
        1 * migrationService.migrateProductSuppliersInParallel() >> [1, 2]
        response.text.startsWith("status: Migrated 2 products in ")

        when:
        response.reset()
        controller.migrateOrganizations(new MigrationCommand())

        then:
        1 * migrationService.migrationOrganizationsInParallel() >> [1]
        response.text.startsWith("status: Migrated 1 organizations in ")

        when:
        response.reset()
        controller.deleteOrganizations()

        then:
        1 * migrationService.deleteOrganizations() >> 5
        response.text.startsWith("status: Deleted 5 organizations in ")

        when:
        response.reset()
        controller.deleteProductSuppliers()

        then:
        1 * migrationService.deleteProductSuppliers() >> 6
        response.text.startsWith("status: Deleted 6 product suppliers in ")
    }

    void "migration failures are reported on the command"() {
        given:
        MigrationCommand command = new MigrationCommand()

        when:
        controller.migrateProductSuppliers(command)

        then:
        1 * migrationService.migrateProductSuppliersInParallel() >> { throw new IllegalStateException("duplicate") }
        command.errors.globalError.code == "productSupplier.error.message"
        response.text == "status: 1"

        when:
        response.reset()
        controller.migrateOrganizations(command)

        then:
        1 * migrationService.migrationOrganizationsInParallel() >> {
            def errors = new BeanPropertyBindingResult(new Object(), "organization")
            errors.reject("invalid")
            errors.reject("invalid.again")
            throw new ValidationException("invalid", errors)
        }
        response.text == "status: 2"
    }
}
