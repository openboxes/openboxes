package org.pih.warehouse.regression.platform.data

import org.pih.warehouse.data.*
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import spock.lang.Specification

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.reporting.DateDimension
import org.pih.warehouse.reporting.LocationDimension
import org.pih.warehouse.reporting.LotDimension
import org.pih.warehouse.reporting.ProductDimension
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

class MigrationControllerActionsSpec extends Specification implements ControllerUnitTest<MigrationController>, DataTest {

    Location depot
    Inventory inventory
    Product product
    TransactionType inventoryType
    TransactionType productInventoryType

    void setupSpec() {
        mockDomains(Location, Inventory, Product, InventoryItem, Transaction, TransactionType, ProductAvailability,
                LocationDimension, ProductDimension, LotDimension, DateDimension, User)
    }

    void setup() {
        depot = new Location(name: 'Main depot').save(validate: false)
        inventory = new Inventory(warehouse: depot).save(validate: false)
        depot.inventory = inventory
        depot.save(validate: false)
        product = new Product(name: 'Paracetamol', productCode: 'PC1').save(validate: false)
        inventoryType = new TransactionType(name: 'Inventory', transactionCode: TransactionCode.INVENTORY)
        inventoryType.id = Constants.INVENTORY_TRANSACTION_TYPE_ID
        inventoryType.save(validate: false)
        productInventoryType = new TransactionType(name: 'Product inventory', transactionCode: TransactionCode.PRODUCT_INVENTORY)
        productInventoryType.id = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
        productInventoryType.save(validate: false)
        session.warehouse = depot
        mockTagLib(ApplicationTagLib)
        mockTagLib(ValidationTagLib)

        controller.migrationService = Mock(MigrationService)
        controller.dataService = Mock(DataService)
        controller.locationService = Mock(LocationService)
        controller.productAvailabilityService = Mock(ProductAvailabilityService)
        controller.transactionSourceMigrationService = Mock(TransactionSourceMigrationService)

        views['/common/_dataTable.gsp'] = '<% data.each { row -> %>${row.values().join("|")};<% } %>'
        views['/migration/_status.gsp'] = '${message ?: ""}${command?.errors?.globalError?.code ?: ""}'
    }

    void cleanup() {
        new AuthService().setCurrentLocation(null)
    }

    void 'dataMigration summarizes the pending migrations'() {
        given:
        new Transaction(transactionType: inventoryType, inventory: inventory, transactionDate: new Date()).save(validate: false)
        new Transaction(transactionType: productInventoryType, inventory: inventory, transactionDate: new Date()).save(validate: false)
        new Transaction(transactionType: productInventoryType, inventory: new Inventory().save(validate: false),
                transactionDate: new Date()).save(validate: false)

        when:
        def model = controller.dataMigration()

        then:
        1 * controller.migrationService.getSuppliersForMigration() >> [1, 2, 3]
        1 * controller.migrationService.getProductsForMigration() >> [1]
        1 * controller.migrationService.getProductsWithTransactions(depot, productInventoryType) >> [product]
        1 * controller.migrationService.getOtherOverlappingTransactions(depot, productInventoryType) >> [PC1: ['t1']]
        1 * controller.transactionSourceMigrationService.getAmountOfMissingInventoryImportTransactionSources() >> 0
        1 * controller.transactionSourceMigrationService.getAmountOfMissingCycleCountTransactionSources() >> 0
        1 * controller.transactionSourceMigrationService.getAmountOfMissingRecordStockTransactionSources() >> 4
        model.organizationCount == 3
        model.productSupplierCount == 1
        model.inventoryTransactionCount == 1
        model.productInventoryTransactionCount == 2
        model.productInventoryTransactionInCurrentLocationCount == 1
        model.productsWithProductInventoryTransactionInCurrentLocation == ['PC1']
        model.overlappingTransactions == [PC1: ['t1']]
        model.amountOfMissingRecordStockTransactionSources == 4
    }

    void 'dataMigration does not count record stock sources until earlier migrations are done'() {
        when:
        def model = controller.dataMigration()

        then:
        1 * controller.migrationService.getSuppliersForMigration() >> []
        1 * controller.migrationService.getProductsForMigration() >> []
        1 * controller.transactionSourceMigrationService.getAmountOfMissingInventoryImportTransactionSources() >> 2
        1 * controller.transactionSourceMigrationService.getAmountOfMissingCycleCountTransactionSources() >> 0
        0 * controller.transactionSourceMigrationService.getAmountOfMissingRecordStockTransactionSources()
        model.amountOfMissingInventoryImportTransactionSources == 2
        model.amountOfMissingRecordStockTransactionSources == null
    }

    void 'index pages and table counts'() {
        expect:
        controller.index() == null
        controller.dataQuality() == null

        when:
        new LocationDimension().save(validate: false)
        new ProductDimension().save(validate: false)
        new ProductDimension().save(validate: false)
        def model = controller.dimensionTables()

        then:
        model.locationDimensionCount == 1
        model.productDimensionCount == 2
        model.lotDimensionCount == 0
        model.dateDimensionCount == 0
    }

    void 'materializedViews counts rows through raw queries'() {
        when:
        def model = controller.materializedViews()

        then:
        1 * controller.dataService.executeQuery({ it.contains('product_demand_details') }) >> [[count: 12]]
        1 * controller.dataService.executeQuery({ it.contains('product_availability') }) >> []
        model.productDemandCount == 12
        model.productAvailabilityCount == 0
    }

    void 'stockMovementsWithoutShipmentItems renders a count or a data table'() {
        given:
        List rows = [[id: 'sm1', request_number: 'R-1', status: 'PICKED', date_created: new Date(120, 0, 1),
                      origin: 'Depot', requested: 2, picked: 1, shipped: 0, issued: 0]]

        when:
        params.format = 'count'
        controller.stockMovementsWithoutShipmentItems()

        then:
        1 * controller.migrationService.getStockMovementsWithoutShipmentItems() >> rows
        response.text == '1'

        when:
        response.reset()
        params.remove('format')
        controller.stockMovementsWithoutShipmentItems()

        then:
        1 * controller.migrationService.getStockMovementsWithoutShipmentItems() >> rows
        response.text.contains('/stockMovement/show/sm1')
        response.text.contains('R-1')
        response.text.contains('PICKED')
    }

    void 'receiptsWithoutTransaction renders a count or a data table'() {
        given:
        Shipment shipment = new Shipment(shipmentNumber: 'S-1', name: 'First')
        shipment.id = 'sh1'
        List receipts = [new Receipt(receiptNumber: 'RC-1', receiptStatusCode: ReceiptStatusCode.RECEIVED, shipment: shipment)]

        when:
        params.format = 'count'
        controller.receiptsWithoutTransaction()

        then:
        1 * controller.migrationService.getReceiptsWithoutTransaction() >> receipts
        response.text == '1'

        when:
        response.reset()
        params.remove('format')
        controller.receiptsWithoutTransaction()

        then:
        1 * controller.migrationService.getReceiptsWithoutTransaction() >> receipts
        response.text.contains('/stockMovement/show/sh1')
        response.text.contains('RC-1')
        response.text.contains('RECEIVED')
    }

    void 'shipmentsWithoutTransactions renders a count or a data table'() {
        given:
        Location supplier = new Location(name: 'Supplier').save(validate: false)
        List shipments = [[id: 'sh1', shipmentNumber: 'S-1', currentStatus: ShipmentStatusCode.SHIPPED,
                           origin: supplier, destination: depot]]

        when:
        params.format = 'count'
        controller.shipmentsWithoutTransactions()

        then:
        1 * controller.migrationService.getShipmentsWithoutTransactions() >> shipments
        response.text == '1'

        when:
        response.reset()
        params.remove('format')
        controller.shipmentsWithoutTransactions()

        then:
        1 * controller.migrationService.getShipmentsWithoutTransactions() >> shipments
        response.text.contains('S-1')
        response.text.contains('SHIPPED|Supplier|Main depot')
    }

    void 'calculate and refresh product availability for a location'() {
        when:
        params.id = depot.id
        controller.calculateProductAvailability()

        then:
        1 * controller.productAvailabilityService.calculateBinLocations(depot) >> [[:], [:], [:]]
        response.text == '3'

        when:
        response.reset()
        params['location.id'] = depot.id
        controller.refreshProductAvailability()

        then:
        1 * controller.productAvailabilityService.refreshProductAvailability(depot, true)
        response.text == 'Refreshed product availability for location Main depot'
    }

    void 'compareProductAvailability lists differences between stored and calculated availability'() {
        given:
        Location bin = new Location(name: 'A-01').save(validate: false)
        InventoryItem lot = new InventoryItem(product: product, lotNumber: 'L1').save(validate: false)
        InventoryItem otherLot = new InventoryItem(product: product, lotNumber: 'L2').save(validate: false)
        ProductAvailability availability = new ProductAvailability(location: depot, product: product, inventoryItem: lot,
                binLocation: bin, productCode: 'PC1', lotNumber: 'L1', binLocationName: 'A-01', quantityOnHand: 5)
        availability.id = 'pa1'
        availability.save(validate: false)

        when:
        controller.compareProductAvailability()

        then:
        1 * controller.productAvailabilityService.calculateBinLocations(depot) >> [
                [product: product, inventoryItem: lot, binLocation: bin, quantity: 7],
                [product: product, inventoryItem: otherLot, binLocation: bin, quantity: 2]]
        response.text == 'PC1|L1|A-01|5|7|true;PC1|L2|A-01|null|2|false;'
    }

    void 'compareProductAvailability counts the stored availability rows'() {
        given:
        ProductAvailability availability = new ProductAvailability(location: depot, product: product, quantityOnHand: 5)
        availability.id = 'pa1'
        availability.save(validate: false)
        params.format = 'count'

        when:
        controller.compareProductAvailability()

        then:
        0 * controller.productAvailabilityService.calculateBinLocations(_)
        response.text == '1'
    }

    void 'compareProductAvailability hides matching rows'() {
        given:
        InventoryItem lot = new InventoryItem(product: product, lotNumber: 'L1').save(validate: false)
        ProductAvailability availability = new ProductAvailability(location: depot, product: product, inventoryItem: lot,
                productCode: 'PC1', lotNumber: 'L1', quantityOnHand: 5)
        availability.id = 'pa1'
        availability.save(validate: false)

        when:
        controller.compareProductAvailability()

        then:
        1 * controller.productAvailabilityService.calculateBinLocations(depot) >> [[product: product, inventoryItem: lot, quantity: 5]]
        response.text == ''
    }

    void 'compareProductAvailability shows matching rows when all are requested'() {
        given:
        InventoryItem lot = new InventoryItem(product: product, lotNumber: 'L1').save(validate: false)
        ProductAvailability availability = new ProductAvailability(location: depot, product: product, inventoryItem: lot,
                productCode: 'PC1', lotNumber: 'L1', quantityOnHand: 5)
        availability.id = 'pa1'
        availability.save(validate: false)
        params.showAll = 'true'

        when:
        controller.compareProductAvailability()

        then:
        1 * controller.productAvailabilityService.calculateBinLocations(depot) >> [[product: product, inventoryItem: lot, quantity: 5]]
        response.text == 'PC1|L1|null|5|5|true;'
    }

    void 'downloadCurrentInventory answers JSON or CSV'() {
        when:
        controller.downloadCurrentInventory()

        then:
        1 * controller.migrationService.getCurrentInventory([depot]) >> [[code: 'PC1', qty: 3]]
        def json = JSON.parse(response.text)
        json.count == 1
        json.results[0].code == 'PC1'

        when:
        response.reset()
        params.format = 'csv'
        controller.downloadCurrentInventory()

        then:
        1 * controller.migrationService.getCurrentInventory([depot]) >> [[code: 'PC1', qty: 3]]
        1 * controller.dataService.generateCsv(_) >> 'code,qty\nPC1,3'
        response.getHeader('Content-disposition').contains('CurrentInventory_Main depot.csv')
        response.text == 'code,qty\nPC1,3'
    }

    void 'location and product transaction lookups render JSON'() {
        when:
        controller.locationsWithInventoryTransactions()

        then:
        1 * controller.migrationService.getLocationsWithTransactions([TransactionCode.INVENTORY]) >> ['a', 'b']
        JSON.parse(response.text).count == 2

        when:
        response.reset()
        controller.locationsWithProductInventoryTransactions()

        then:
        1 * controller.migrationService.getLocationsWithTransaction(productInventoryType) >> ['a']
        JSON.parse(response.text).locations == ['a']

        when:
        response.reset()
        controller.productsWithInventoryTransactions()

        then:
        1 * controller.migrationService.getProductsWithTransactions(depot, [TransactionCode.INVENTORY]) >> [product]
        JSON.parse(response.text).products*.productCode == ['PC1']
    }

    void 'nextInventoryTransaction opens the next product stock card'() {
        when:
        controller.nextInventoryTransaction()

        then:
        1 * controller.migrationService.getProductsWithTransactions(depot, [TransactionCode.INVENTORY]) >> [product]
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"

        when:
        response.reset()
        controller.nextInventoryTransaction()

        then:
        1 * controller.migrationService.getProductsWithTransactions(depot, [TransactionCode.INVENTORY]) >> []
        response.text == 'No inventory transactions'
    }

    void 'migrateProduct migrates one product and reports failures'() {
        given:
        params.id = product.id

        when:
        controller.migrateProduct()

        then:
        1 * controller.migrationService.migrateInventoryTransactions(depot, product, true) >> [:]
        flash.message == 'Migrated product PC1'
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"

        when:
        response.reset()
        controller.migrateProduct()

        then:
        1 * controller.migrationService.migrateInventoryTransactions(depot, product, true) >> { throw new IllegalStateException('negative balance') }
        flash.message == 'Unable to migrate product PC1 due to error: negative balance'
    }

    void 'migrateInventoryTransactions answers JSON or CSV'() {
        when:
        params.performMigration = 'true'
        controller.migrateInventoryTransactions()

        then:
        1 * controller.migrationService.migrateInventoryTransactions(depot, true) >> [a: 1, stockHistory: [2]]
        params.max == 1
        JSON.parse(response.text).count == 2

        when:
        response.reset()
        params.format = 'csv'
        controller.migrateInventoryTransactions()

        then:
        1 * controller.migrationService.migrateInventoryTransactions(depot, true) >> [a: 1, stockHistory: [2]]
        1 * controller.dataService.generateCsv([a: 1]) >> 'a\n1'
        response.text == 'a\n1'
    }

    void 'migrateProductInventoryTransactions previews or migrates'() {
        when:
        controller.migrateProductInventoryTransactions()

        then:
        1 * controller.migrationService.migrateProductInventoryTransactions(depot, false) >> [products: 2]
        def json = JSON.parse(response.text)
        json.results.products == 2
        json.responseTime.startsWith('Generating preview of product inventory transactions at location Main depot')
    }

    void 'createMissing#kind TransactionSources runs for the current location'() {
        given:
        new AuthService().setCurrentLocation(depot)

        when:
        controller."createMissing${kind}TransactionSourcesForCurrentLocation"()

        then:
        1 * controller.transactionSourceMigrationService."createMissing${kind}TransactionSources"(depot) >>
                [inventoryCounts: 3, transactionSourcesCreated: 2]
        def json = JSON.parse(response.text)
        json.inventoryCounts == 3
        json.transactionSourcesCreated == 2

        where:
        kind << ['InventoryImport', 'CycleCount', 'RecordStock']
    }

    void 'migrateProductSuppliers reports the number of migrated products'() {
        when:
        controller.migrateProductSuppliers(new MigrationCommand())

        then:
        1 * controller.migrationService.migrateProductSuppliersInParallel() >> [1, 2]
        response.text.startsWith('Migrated 2 products')
    }

    void 'migrateProductSuppliers reports a failure on the command'() {
        when:
        controller.migrateProductSuppliers(new MigrationCommand())

        then:
        1 * controller.migrationService.migrateProductSuppliersInParallel() >> { throw new IllegalStateException('broken') }
        response.text == 'productSupplier.error.message'
    }

    void 'migrateOrganizations reports the number of migrated organizations'() {
        when:
        controller.migrateOrganizations(new MigrationCommand())

        then:
        1 * controller.migrationService.migrationOrganizationsInParallel() >> [1]
        response.text.startsWith('Migrated 1 organizations')
    }

    void 'migrateOrganizations reports validation errors'() {
        given:
        MigrationCommand command = new MigrationCommand()
        ValidationErrors errors = new ValidationErrors(command)
        errors.reject('organization.invalid')

        when:
        controller.migrateOrganizations(command)

        then:
        1 * controller.migrationService.migrationOrganizationsInParallel() >> { throw new ValidationException('invalid', errors) }
        response.text == 'organization.invalid'
    }

    void 'deleteOrganizations reports the number of deleted organizations'() {
        when:
        controller.deleteOrganizations()

        then:
        1 * controller.migrationService.deleteOrganizations() >> 4
        response.text.startsWith('Deleted 4 organizations')
    }

    void 'deleteProductSuppliers reports the number of deleted suppliers'() {
        when:
        controller.deleteProductSuppliers()

        then:
        1 * controller.migrationService.deleteProductSuppliers() >> 6
        response.text.startsWith('Deleted 6 product suppliers')
    }
}
