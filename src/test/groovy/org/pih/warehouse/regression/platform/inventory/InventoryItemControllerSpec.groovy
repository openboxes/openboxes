package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentItemException
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.product.ProductException
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class InventoryItemControllerSpec extends Specification implements ControllerUnitTest<InventoryItemController>, DataTest {

    Location depot
    Inventory inventory
    Product product
    InventoryItem lot
    User user
    TransactionType inventoryType

    void setupSpec() {
        mockDomains(Location, LocationType, Inventory, Product, Category, InventoryItem, InventoryLevel,
                Transaction, TransactionEntry, TransactionType, User, InventorySnapshot, ProductAssociation,
                Shipment, ShipmentItem, Container, Person)
    }

    void setup() {
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.localizationService >> Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        LocalizationUtil.currentLocale >> Locale.ENGLISH

        mockTagLib(ApplicationTagLib)
        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code + (attrs.args ? attrs.args.toString() : '') }]

        depot = new Location(name: 'Main depot').save(validate: false)
        inventory = new Inventory(warehouse: depot).save(validate: false)
        depot.inventory = inventory
        depot.save(validate: false)
        product = new Product(name: 'Paracetamol', productCode: 'PC1').save(validate: false)
        lot = new InventoryItem(product: product, lotNumber: 'L1', expirationDate: new Date(130, 0, 1)).save(validate: false)
        user = new User(username: 'manager', email: 'manager@example.com').save(validate: false)
        inventoryType = new TransactionType(name: 'Inventory', transactionCode: TransactionCode.INVENTORY)
        inventoryType.id = Constants.INVENTORY_TRANSACTION_TYPE_ID
        inventoryType.save(validate: false)

        session.warehouse = depot
        session.user = user

        controller.inventoryService = Mock(InventoryServiceContract)
        controller.productAvailabilityService = Mock(ProductAvailabilityContract)
        controller.shipmentService = Mock(ShipmentServiceContract)
        controller.orderService = Mock(OrderServiceContract)
        controller.requisitionService = Mock(RequisitionServiceContract)
        controller.forecastingService = Mock(ForecastingServiceContract)
        controller.dataService = Mock(DataServiceContract)
        controller.userService = Mock(UserServiceContract)
        controller.inventoryItemDataService = Mock(InventoryItemDataService)
        controller.stockHistoryAssembler = Mock(StockHistoryAssembler)
    }

    void 'index redirects to the inventory browser'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/inventory/browse'
    }

    void 'getInventoryItems renders the lots of the product as JSON'() {
        given:
        params['product.id'] = product.id

        when:
        controller.getInventoryItems()

        then:
        1 * controller.inventoryService.getInventoryItemsByProduct(product) >> [[lotNumber: 'L1'], [lotNumber: 'L2']]
        JSON.parse(response.text)*.lotNumber == ['L1', 'L2']
    }

    void 'showStockCard populates the command for the current warehouse'() {
        given:
        StockCardCommand command = new StockCardCommand(product: product)

        when:
        def model = controller.showStockCard(command)

        then:
        1 * controller.inventoryService.getStockCardCommand(command, _) >> command
        model.commandInstance.warehouse == depot
    }

    void 'showStockCard redirects to the dashboard when the product cannot be shown'() {
        when:
        controller.showStockCard(new StockCardCommand())

        then:
        1 * controller.inventoryService.getStockCardCommand(_, _) >> { throw new ProductException('Product not found') }
        response.redirectedUrl == '/dashboard/index'
        flash.message == 'Product not found'
    }

    void 'showCurrentStock renders the current stock template for the product'() {
        given:
        views['/inventoryItem/_showCurrentStock.gsp'] = 'stock of ${commandInstance.product.name} at ${commandInstance.warehouse.name}'
        params.id = product.id
        StockCardCommand command = new StockCardCommand()

        when:
        controller.showCurrentStock(command)

        then:
        1 * controller.inventoryService.getStockCardCommand(command, _) >> command
        response.text == 'stock of Paracetamol at Main depot'
    }

    void 'showCurrentStockAllLocations renders the template or downloads the CSV'() {
        given:
        views['/inventoryItem/_showCurrentStockAllLocations.gsp'] = '${targetUri} ${quantityMap.size()}'
        params.id = product.id
        List quantityMap = [['Group A': [locations: [[location: depot, quantity: 5, value: 12.5]]]]]

        when:
        controller.showCurrentStockAllLocations(new StockCardCommand())

        then:
        1 * controller.inventoryService.getCurrentStockAllLocations(product, user) >> quantityMap
        response.text == "/inventoryItem/showStockCard/${product.id} 1"

        when:
        response.reset()
        params.download = 'true'
        controller.showCurrentStockAllLocations(new StockCardCommand())

        then:
        1 * controller.inventoryService.getCurrentStockAllLocations(product, user) >> quantityMap
        response.contentType.startsWith('text/csv')
        response.getHeader('Content-disposition').contains('All-Locations-PC1.csv')
        response.text.contains('Paracetamol')
        response.text.contains('Group A')
        response.text.contains('Main depot')
        response.text.contains('12.5')
    }

    void 'showCurrentStockAllLocations fails for an unknown product'() {
        given:
        params.id = 'missing'

        when:
        controller.showCurrentStockAllLocations(new StockCardCommand())

        then:
        ProductException e = thrown()
        e.message.contains('missing')
    }

    void 'showAssociatedProducts renders an empty association list without asking for quantities'() {
        given:
        views['/inventoryItem/_showProductAssociations.gsp'] = '${product.name} ${totalQuantity}'
        params.id = product.id

        when:
        controller.showAssociatedProducts(new StockCardCommand())

        then:
        0 * controller.productAvailabilityService.getQuantityAvailableToPromiseByProduct(*_)
        response.text == 'Paracetamol 0'
    }

    void 'showStockHistory renders the printable and the regular template'() {
        given:
        views['/inventoryItem/_printStockHistory.gsp'] = 'print ${totalBalance} ${totalCredit} ${totalDebit}'
        views['/inventoryItem/_showStockHistory.gsp'] = 'show ${productId} ${totalCount}'
        StockCardCommand command = new StockCardCommand(product: product)
        StockHistoryPageModel pageModel = new StockHistoryPageModel(commandInstance: command,
                stockHistory: new StockHistoryResult(totalBalance: 7, totalCredit: 10, totalDebit: 3, totalCount: 2),
                displayContext: new StockHistoryDisplayContext())

        when:
        params.print = 'true'
        controller.showStockHistory(command)

        then:
        1 * controller.stockHistoryAssembler.assembleStockHistoryPage(command, _) >> pageModel
        command.warehouse == depot
        response.text == 'print 7 10 3'

        when:
        response.reset()
        params.remove('print')
        controller.showStockHistory(command)

        then:
        1 * controller.stockHistoryAssembler.assembleStockHistoryPage(command, _) >> pageModel
        response.text == "show ${product.id} 2"
    }

    void 'showSuppliers, showDocuments and showInventorySnapshot render their templates'() {
        given:
        views['/inventoryItem/_showSuppliers.gsp'] = 'suppliers ${productInstance.name} ${currentLocation.name}'
        views['/inventoryItem/_showDocuments.gsp'] = 'documents ${productInstance.name}'
        views['/inventoryItem/_showInventorySnapshot.gsp'] = 'snapshots ${inventorySnapshots.size()}'
        new InventorySnapshot(product: product, location: depot, quantityOnHand: 3, date: new Date()).save(validate: false)
        params.id = product.id

        when:
        controller.showSuppliers()

        then:
        response.text == 'suppliers Paracetamol Main depot'

        when:
        response.reset()
        controller.showDocuments()

        then:
        response.text == 'documents Paracetamol'

        when:
        response.reset()
        controller.showInventorySnapshot()

        then:
        response.text == 'snapshots 1'
    }

    void 'showPendingInbound merges shipments and purchase orders by order and ready date'() {
        given:
        views['/inventoryItem/_showPendingInboundStock.gsp'] = '<% itemsMap.each { k, v -> %>${v.type}:${v.quantityRemaining}:${v.quantityPurchased};<% } %>'
        params.id = product.id
        Map shipment = [currentStatus: 'SHIPPED', expectedShippingDate: new Date()]
        Order order = new Order()
        Date readyDate = new Date(126, 0, 1)
        OrderItem first = Stub(OrderItem) {
            getOrderItemStatusCode() >> OrderItemStatusCode.PENDING
            getActualReadyDate() >> readyDate
            getOrder() >> order
            getQuantityRemaining() >> 2
            getQuantityPerUom() >> 10
        }
        OrderItem second = Stub(OrderItem) {
            getOrderItemStatusCode() >> OrderItemStatusCode.PENDING
            getActualReadyDate() >> readyDate
            getOrder() >> order
            getQuantityRemaining() >> 1
            getQuantityPerUom() >> 10
        }
        OrderItem canceled = Stub(OrderItem) {
            getOrderItemStatusCode() >> OrderItemStatusCode.CANCELED
        }

        when:
        controller.showPendingInbound()

        then:
        1 * controller.shipmentService.getPendingInboundShipmentItems(depot, product) >> [
                [shipment: shipment, quantityRemaining: 4], [shipment: shipment, quantityRemaining: 6]]
        1 * controller.orderService.getPendingInboundOrderItems(depot, product) >> [first, second, canceled]
        response.text == 'Stock Movement:10:0;Purchase Order:0:30;'
    }

    void 'showDemand maps the demand details and lists the months of the period'() {
        given:
        views['/inventoryItem/_showDemand.gsp'] = '${numberOfMonths} ${monthKeys.join(",")} <% requisitionItems.each { %>${it.requestNumber}:${it.monthRequested}:${it.monthIssued}:${it.quantityDemand};<% } %>${destinations.size()}'
        params.startDate = '01/15/2025'
        params.endDate = '03/10/2025'
        StockCardCommand command = new StockCardCommand(product: product)

        when:
        controller.showDemand(command)

        then:
        1 * controller.inventoryService.getStockCardCommand(command, _) >> command
        1 * controller.forecastingService.getDemandDetails(depot, null, product, _, _) >> [
                [request_number: 'R1', date_requested: new Date(125, 0, 20), date_issued: new Date(125, 1, 2), quantity_demand: 4],
                [request_number: 'R2', date_requested: new Date(125, 2, 1)],
        ]
        1 * controller.forecastingService.getAvailableDestinationsForDemandDetails(depot, product, _, _) >> [depot]
        response.text == '3 Jan 2025,Feb 2025,Mar 2025 R1:Jan 2025:Feb 2025:4;R2:Mar 2025::0;1'
    }

    void 'showDemand defaults the period when no dates are given'() {
        given:
        views['/inventoryItem/_showDemand.gsp'] = '${numberOfMonths} ${monthKeys.join(",")} <% requisitionItems.each { %>${it.requestNumber}:${it.monthRequested}:${it.monthIssued}:${it.quantityDemand};<% } %>${destinations.size()}'
        StockCardCommand command = new StockCardCommand(product: product)

        when:
        controller.showDemand(command)

        then:
        1 * controller.inventoryService.getStockCardCommand(command, _) >> command
        1 * controller.forecastingService.getDemandDetails(*_) >> []
        1 * controller.forecastingService.getAvailableDestinationsForDemandDetails(*_) >> []
        (response.text.split(' ')[0] as Integer) >= 12
    }

    void 'showProductDemand exports CSV or renders the template'() {
        given:
        views['/inventoryItem/_showProductDemand.gsp'] = 'demand ${product.name}'
        params.id = product.id

        when:
        controller.showProductDemand()

        then:
        response.text == 'demand Paracetamol'

        when:
        response.reset()
        params.format = 'csv'
        controller.showProductDemand()

        then:
        1 * controller.forecastingService.getDemandDetails(depot, product) >> [[a: 1]]
        1 * controller.dataService.generateCsv([[a: 1]]) >> 'a\n1'
        response.getHeader('Content-disposition').contains('Product Demand PC1 Main depot.csv')
        response.text == 'a\n1'
    }

    void '#action populates the stock card command'() {
        given:
        StockCardCommand command = new StockCardCommand(product: product)

        when:
        def model = controller."$action"(command)

        then:
        1 * controller.inventoryService.getStockCardCommand(command, _) >> command
        model.commandInstance.warehouse == depot

        where:
        action << ['showLotNumbers', 'showTransactionLog', 'showGraph']
    }

    void 'showRecordInventory builds the product JSON with its lots'() {
        given:
        params['product.id'] = product.id
        RecordInventoryCommand command = new RecordInventoryCommand(product: product)
        InventoryLevel level = new InventoryLevel(product: product, inventory: inventory).save(validate: false)

        when:
        def model = controller.showRecordInventory(command)

        then:
        1 * controller.inventoryService.populateRecordInventoryCommand(command, _)
        1 * controller.inventoryService.getTransactionEntriesByInventoryAndProduct(inventory, [product]) >> []
        1 * controller.inventoryService.getQuantityByProductMap([]) >> [(product): 9]
        1 * controller.inventoryService.getQuantityAvailableToPromise(product, depot) >> 6
        1 * controller.inventoryService.getInventoryItemsWithQuantity([product], inventory) >> [(product): [lot]]
        model.commandInstance.inventory == inventory
        model.commandInstance.inventoryLevel == level
        model.commandInstance.totalQuantity == 9
        model.commandInstance.totalQuantityAvailableToPromise == 6

        and:
        def json = JSON.parse(model.product)
        json.product.name == 'Paracetamol'
        json.inventoryItems*.lotNumber == ['L1']
    }

    void 'showRecordInventory restores the rows kept from a failed save'() {
        given:
        Location bin = new Location(name: 'A-01').save(validate: false)
        flash.recordInventoryRows = [new RecordInventoryRowCommand(lotNumber: 'L9', binLocation: bin, oldQuantity: 1, newQuantity: 2)]
        RecordInventoryCommand command = new RecordInventoryCommand(product: product, inventory: inventory)

        when:
        def model = controller.showRecordInventory(command)

        then:
        0 * controller.inventoryService.populateRecordInventoryCommand(*_)
        1 * controller.inventoryService.getQuantityByProductMap(_) >> [:]
        1 * controller.inventoryService.getInventoryItemsWithQuantity(*_) >> [:]
        model.commandInstance.recordInventoryRows*.lotNumber == ['L9']
        model.commandInstance.recordInventoryRows[0].binLocation == bin
        model.commandInstance.totalQuantity == 0
    }

    void 'saveRecordInventory refreshes availability and goes to the stock card'() {
        given:
        RecordInventoryCommand command = new RecordInventoryCommand(product: product, inventory: inventory)

        when:
        controller.saveRecordInventory(command)

        then:
        1 * controller.inventoryService.saveRecordInventoryCommand(command, _)
        1 * controller.productAvailabilityService.refreshProductsAvailability(depot.id, [product.id], false)
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    void 'saveRecordInventory keeps the rows in flash when the command has errors'() {
        given:
        RecordInventoryCommand command = new RecordInventoryCommand(product: product, inventory: inventory)

        when:
        controller.saveRecordInventory(command)

        then:
        1 * controller.inventoryService.saveRecordInventoryCommand(command, _) >> { cmd, p -> cmd.errors.reject('invalid') }
        0 * controller.productAvailabilityService.refreshProductsAvailability(*_)
        flash.errors.globalError.code == 'invalid'
        flash.recordInventoryRows != null
        response.redirectedUrl == "/inventoryItem/showRecordInventory?product.id=${product.id}"
    }

    void 'saveInventoryItem rejects a missing quantity and re-renders the form'() {
        given:
        params['product.id'] = product.id
        params['inventory.id'] = inventory.id
        params.lotNumber = 'NEW'

        when:
        controller.saveInventoryItem()

        then:
        1 * controller.inventoryService.getInventoryItemsByProduct(product) >> [lot]
        view == '/inventoryItem/createInventoryItem'
        model.itemInstance.errors.hasFieldErrors('quantity')
        model.inventoryItems == [lot]
        InventoryItem.findByLotNumber('NEW') == null
    }

    void 'edit returns the inventory item'() {
        given:
        params.id = lot.id

        when:
        def model = controller.edit()

        then:
        model.itemInstance == lot
    }

    void 'editInventoryLevel returns the existing level or a new one'() {
        given:
        params['product.id'] = product.id

        when:
        def model = controller.editInventoryLevel()

        then:
        model.inventoryInstance == inventory
        model.inventoryLevelInstance.id == null

        when:
        InventoryLevel level = new InventoryLevel(product: product, inventory: inventory, minQuantity: 3).save(validate: false)
        params['inventory.id'] = inventory.id
        model = controller.editInventoryLevel()

        then:
        model.inventoryLevelInstance == level
        model.productInstance == product
    }

    void 'updateInventoryLevel saves a valid level and re-renders an invalid one'() {
        given:
        params['product.id'] = product.id
        params['inventory.id'] = inventory.id
        params.minQuantity = '2'

        when:
        controller.updateInventoryLevel()

        then:
        InventoryLevel.findByProduct(product)?.minQuantity == 2
        flash.message.startsWith('default.updated.message')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"

        when:
        response.reset()
        params.id = InventoryLevel.findByProduct(product).id
        params.minQuantity = '-1'
        controller.updateInventoryLevel()

        then:
        view == '/inventoryItem/updateInventoryLevel'
        model.inventoryLevelInstance.errors.hasFieldErrors('minQuantity')
    }

    void 'adjustStock adjusts and chains back to the stock card'() {
        given:
        AdjustStockCommand command = new AdjustStockCommand(inventoryItem: lot)

        when:
        controller.adjustStock(command)

        then:
        1 * controller.inventoryService.adjustStock(command)
        flash.message.startsWith('default.updated.message')
        response.redirectedUrl.startsWith("/inventoryItem/showStockCard/${product.id}")
    }

    void 'transferStock redirects to the stock card after a successful transfer'() {
        given:
        TransferStockCommand command = new TransferStockCommand(inventoryItem: lot)

        when:
        controller.transferStock(command)

        then:
        1 * controller.inventoryService.transferStock(command) >> new Transaction()
        flash.message.startsWith('default.updated.message')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}?inventoryItem.id=${lot.id}"
    }

    void 'transferStock chains back with the failed transaction'() {
        given:
        TransferStockCommand command = new TransferStockCommand(inventoryItem: lot)
        Transaction failed = new Transaction()
        failed.errors.reject('transfer.invalid')

        when:
        controller.transferStock(command)

        then:
        1 * controller.inventoryService.transferStock(command) >> failed
        flash.message == null
        flash.chainModel.transaction.is(failed)
        response.redirectedUrl.startsWith("/inventoryItem/showStockCard/${product.id}")

        when:
        response.reset()
        flash.clear()
        controller.transferStock(command)

        then:
        1 * controller.inventoryService.transferStock(command) >> { throw new IllegalStateException('boom') }
        flash.chainModel.itemInstance == lot
    }

    void 'transferStock without an inventory item just goes back'() {
        when:
        controller.transferStock(new TransferStockCommand())

        then:
        0 * controller.inventoryService.transferStock(_)
        response.redirectedUrl.startsWith('/inventoryItem/showStockCard?')
    }

    void 'update validates the lot and saves through the data service'() {
        given:
        params.id = lot.id
        params['product.id'] = product.id
        params.lotNumber = 'L1-renamed'

        when:
        controller.update()

        then:
        1 * controller.inventoryItemDataService.save(lot) >> lot
        lot.lotNumber == 'L1-renamed'
        flash.message.startsWith('default.updated.message')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
    }

    void 'update refuses a blank lot for products without lot control'() {
        given:
        params.id = lot.id
        params['product.id'] = product.id
        params.lotNumber = ''

        when:
        controller.update()

        then:
        0 * controller.inventoryItemDataService.save(_)
        flash.error == 'inventoryItem.blankLot.message'
    }

    void 'update requires lot and expiry for lot controlled products'() {
        given:
        product.lotAndExpiryControl = true
        product.save(validate: false)
        params.id = lot.id
        params['product.id'] = product.id
        params.lotNumber = 'L1'

        when:
        controller.update()

        then:
        0 * controller.inventoryItemDataService.save(_)
        flash.error == 'inventoryItem.lotAndExpiryControl.message'
    }

    void 'update rejects a stale version and reports a missing item'() {
        given:
        lot.version = 3
        lot.save(validate: false)
        params.id = lot.id
        params['product.id'] = product.id
        params.version = '1'

        when:
        controller.update()

        then:
        0 * controller.inventoryItemDataService.save(_)
        lot.errors.hasFieldErrors('version')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"

        when:
        response.reset()
        params.id = 'missing'
        controller.update()

        then:
        flash.message.startsWith('default.not.found.message')
    }

    void 'update reports a failed save'() {
        given:
        params.id = lot.id
        params['product.id'] = product.id
        params.lotNumber = 'L1'
        params.expirationDate = '12/31/2030'

        when:
        controller.update()

        then:
        1 * controller.inventoryItemDataService.save(lot) >> null
        flash.error.startsWith('default.not.updated.message')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
    }

    void 'addToInventory renders a confirmation message'() {
        given:
        params.id = product.id

        when:
        controller.addToInventory()

        then:
        response.text == 'inventoryItem.productAddedToInventory.message[Paracetamol]'
    }

    void 'saveInventoryLevel saves a level'() {
        given:
        params['product.id'] = product.id
        params['inventory.id'] = inventory.id
        params.maxQuantity = '10'

        when:
        controller.saveInventoryLevel()

        then:
        InventoryLevel.findByProduct(product)?.maxQuantity == 10
        flash.message == null
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    void 'create saves a new lot or reports the failure'() {
        given:
        params['product.id'] = product.id
        params.lotNumber = 'L5'

        when:
        controller.create()

        then:
        InventoryItem.findByLotNumber('L5') != null
        flash.message.startsWith('default.created.message')
        response.redirectedUrl == "/inventoryItem/showLotNumbers?product.id=${product.id}"

        when:
        response.reset()
        params.remove('product.id')
        params.remove('product')
        controller.create()

        then:
        flash.message.startsWith('default.not.created.message')
    }

    void 'delete removes the lot through the data service'() {
        given:
        params.id = lot.id

        when:
        controller.delete()

        then:
        1 * controller.inventoryItemDataService.getWithProduct(lot.id) >> lot
        1 * controller.inventoryItemDataService.delete(lot.id)
        flash.message.startsWith('default.deleted.message')
        response.redirectedUrl == "/inventoryItem/showLotNumbers?product.id=${product.id}"

        when:
        response.reset()
        controller.delete()

        then:
        1 * controller.inventoryItemDataService.getWithProduct(lot.id) >> lot
        1 * controller.inventoryItemDataService.delete(lot.id) >> {
            throw new org.springframework.dao.DataIntegrityViolationException('in use')
        }
        flash.message.startsWith('default.not.deleted.message')

        when:
        response.reset()
        controller.delete()

        then:
        1 * controller.inventoryItemDataService.getWithProduct(lot.id) >> null
        flash.message.startsWith('default.not.found.message')
    }

    void 'deleteTransactionEntry deletes the entry and returns to the stock card'() {
        given:
        Transaction transaction = new Transaction(transactionType: inventoryType, inventory: inventory, transactionDate: new Date() - 1)
        TransactionEntry entry = new TransactionEntry(inventoryItem: lot, product: product, quantity: 1)
        transaction.addToTransactionEntries(entry)
        transaction.save(validate: false)
        entry.save(validate: false)
        params.id = entry.id

        when:
        controller.deleteTransactionEntry()

        then:
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    void 'saveTransactionEntry reports a missing lot'() {
        given:
        params['product.id'] = product.id
        params.lotNumber = 'NOPE'

        when:
        controller.saveTransactionEntry()

        then:
        1 * controller.inventoryService.findByProductAndLotNumber(product, 'NOPE') >> null
        flash.message.startsWith('default.notfound.message')
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    void 'saveTransactionEntry negates the quantity of an outgoing entry'() {
        given:
        Location other = new Location(name: 'Other').save(validate: false)
        params['product.id'] = product.id
        params.lotNumber = 'L1'
        params['transactionType.id'] = inventoryType.id
        params['inventory.id'] = inventory.id
        params['destination.id'] = other.id
        params.transactionDate = new Date() - 1
        params.quantity = '4'

        when:
        controller.saveTransactionEntry()

        then:
        1 * controller.inventoryService.findByProductAndLotNumber(product, 'L1') >> lot
        flash.message == 'inventoryItem.savedTransactionEntry.label'
        TransactionEntry.list()*.quantity == [-4]
    }

    void 'recall and revertRecall require an administrator'() {
        given:
        params.id = lot.id

        when:
        controller.recall()

        then:
        1 * controller.userService.isUserAdmin(user) >> false
        0 * controller.inventoryService.recallInventoryItem(_)
        flash.message == 'errors.noPermissions.label'
        response.redirectedUrl == "/inventoryItem/showLotNumbers?product.id=${product.id}"

        when:
        response.reset()
        controller.recall()

        then:
        1 * controller.userService.isUserAdmin(user) >> true
        1 * controller.inventoryService.recallInventoryItem(lot)
        1 * controller.productAvailabilityService.refreshProductsAvailability(null, [product.id], false)
        flash.message == 'inventoryItem.recall.message'

        when:
        response.reset()
        controller.revertRecall()

        then:
        1 * controller.userService.isUserAdmin(user) >> true
        1 * controller.inventoryService.revertRecallInventoryItem(lot)
        1 * controller.productAvailabilityService.refreshProductsAvailability(null, [product.id], false)
        flash.message == 'inventoryItem.revertRecall.message'

        when:
        response.reset()
        controller.revertRecall()

        then:
        1 * controller.userService.isUserAdmin(user) >> false
        0 * controller.inventoryService.revertRecallInventoryItem(_)
        flash.message == 'errors.noPermissions.label'
    }

    void 'recall refuses a lot without a lot number'() {
        given:
        InventoryItem blank = new InventoryItem(product: product).save(validate: false)
        params.id = blank.id

        when:
        controller.recall()

        then:
        1 * controller.userService.isUserAdmin(user) >> true
        0 * controller.inventoryService.recallInventoryItem(_)
        flash.message == 'inventoryItem.recallError.message'
    }

    void 'addToShipment keeps the shipment item errors when validation fails'() {
        given:
        Shipment shipment = new Shipment(name: 'Shipment 1').save(validate: false)
        Container container = new Container(name: 'Box 1', shipment: shipment).save(validate: false)
        params['product.id'] = product.id
        params['inventoryItem.id'] = lot.id
        params.shipmentContainer = "${shipment.id}:${container.id}"
        params.quantity = '3'
        ShipmentItem rejected = null

        when:
        controller.addToShipment()

        then:
        1 * controller.shipmentService.validateShipmentItem(_) >> { ShipmentItem item ->
            rejected = item
            item.errors.reject('shipmentItem.invalid')
            throw new ShipmentItemException(shipmentItem: item)
        }
        rejected.lotNumber == 'L1'
        rejected.shipment == shipment
        rejected.container == container
        rejected.quantity == 3
        flash.errors.globalError.code == 'shipmentItem.invalid'
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    void 'addToShipment reports an item that does not validate'() {
        given:
        Shipment shipment = new Shipment(name: 'Shipment 1').save(validate: false)
        params['product.id'] = product.id
        params['inventoryItem.id'] = lot.id
        params.shipmentContainer = "${shipment.id}:missing"

        when:
        controller.addToShipment()

        then:
        1 * controller.shipmentService.validateShipmentItem(_) >> true
        flash.message.startsWith('inventoryItem.errorValidatingItem.message')
        shipment.shipmentItems == null || shipment.shipmentItems.isEmpty()
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    @PendingFeature(reason = 'OBAF queries TransactionEntry.findAllByProductAndInventory; TransactionEntry has no inventory')
    void 'showTransactions lists the entries of the product in the current inventory'() {
        given:
        Transaction here = new Transaction(inventory: inventory, transactionType: inventoryType, transactionDate: new Date()).save(validate: false)
        TransactionEntry mine = new TransactionEntry(inventoryItem: lot, quantity: 5, transaction: here).save(validate: false)
        params['product.id'] = product.id

        when:
        def model = controller.showTransactions()

        then:
        1 * controller.inventoryService.getInventoryItemsByProductAndInventory(product, inventory) >> [lot]
        1 * controller.inventoryService.getTransactionEntriesByInventoryAndProduct(inventory, [product]) >> [mine]
        model.transactionEntryList == [mine]
        model.transactionEntryMap == [(here): [mine]]
        model.inventoryItemList == [lot]
    }

    @PendingFeature(reason = 'OBAF createInventoryItem puts an undefined totalQuantity into the model')
    void 'createInventoryItem returns the form model'() {
        given:
        params['product.id'] = product.id
        params['inventory.id'] = inventory.id

        when:
        def model = controller.createInventoryItem()

        then:
        1 * controller.inventoryService.getInventoryLevelByProductAndInventory(product, inventory) >> null
        1 * controller.inventoryService.getInventoryItemsByProduct(product) >> [lot]
        model.itemInstance.product == product
        model.inventoryInstance == inventory
        model.inventoryItems == [lot]
    }

    @PendingFeature(reason = 'OBAF InventoryItemController uses Constants without importing it')
    void 'saveInventoryItem saves the lot within a new inventory transaction'() {
        given:
        params['product.id'] = product.id
        params['inventory.id'] = inventory.id
        params.lotNumber = 'NEW'
        params.quantity = '4'

        when:
        controller.saveInventoryItem()

        then:
        1 * controller.inventoryService.getInventoryItemsByProduct(_) >> [lot]
        InventoryItem.findByLotNumber('NEW') != null
        flash.message.startsWith('inventoryItem.savedItemWithinNewTransaction.message')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
    }

    @PendingFeature(reason = 'OBAF edit redirects with the id of the missing item (NPE)')
    void 'edit of an unknown item goes back to the stock card'() {
        given:
        params.id = 'missing'
        params['product.id'] = product.id

        when:
        controller.edit()

        then:
        flash.message.startsWith('default.not.found.message')
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    @PendingFeature(reason = 'OBAF calls getYear() on the configured minimum date String')
    void 'update reports a failed save of an item without an expiration date'() {
        given:
        config.openboxes.expirationDate.minValue = '01/01/2000'
        InventoryItem undated = new InventoryItem(product: product, lotNumber: 'U1').save(validate: false)
        params.id = undated.id
        params['product.id'] = product.id
        params.lotNumber = 'U1'

        when:
        controller.update()

        then:
        1 * controller.inventoryItemDataService.save(undated) >> null
        flash.error.startsWith('default.not.updated.message')
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
    }

    @PendingFeature(reason = 'OBAF compares the expiration date string to a Date')
    void 'update rejects an expiration date before the configured minimum'() {
        given:
        config.openboxes.expirationDate.minValue = '01/01/2000'
        params.id = lot.id
        params['product.id'] = product.id
        params.lotNumber = 'L1'
        params.expirationDate = new Date(90, 0, 1)

        when:
        controller.update()

        then:
        1 * controller.inventoryItemDataService.save(lot) >> null
        flash.error == 'This date is invalid. Please enter a date after 2000.'
        response.redirectedUrl == "/inventoryItem/showLotNumbers/${product.id}"
    }

    @PendingFeature(reason = 'OBAF saveInventoryLevel adds a FieldError to a String')
    void 'saveInventoryLevel lists the validation errors'() {
        given:
        params['product.id'] = product.id
        params.maxQuantity = 'many'

        when:
        controller.saveInventoryLevel()

        then:
        flash.message.startsWith('inventoryItem.errorSavingInventoryLevels.message<br/>')
        flash.message.contains('maxQuantity')
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"
    }

    @PendingFeature(reason = 'OBAF builds the not-found message from the missing product id (NPE)')
    void 'saveTransactionEntry reports a missing product'() {
        given:
        params['product.id'] = 'missing'

        when:
        controller.saveTransactionEntry()

        then:
        flash.message.startsWith('default.notfound.message')
        flash.message.contains('missing')
        response.redirectedUrl.startsWith('/inventoryItem/showStockCard')
    }

    @PendingFeature(reason = 'OBAF operator precedence drops the product name from the message')
    void 'addToShipment names the product and lot in the success message'() {
        given:
        controller.metaClass.format = [product: { Map attrs -> attrs.product.name }]
        Shipment.metaClass.save = { -> delegate }
        Shipment shipment = new Shipment(name: 'Shipment 1').save(validate: false)
        shipment.metaClass.save = { -> delegate }
        params['product.id'] = product.id
        params['inventoryItem.id'] = lot.id
        params.shipmentContainer = "${shipment.id}:missing"
        params.quantity = '3'

        when:
        controller.addToShipment()

        then:
        1 * controller.shipmentService.validateShipmentItem(_) >> { ShipmentItem item ->
            item.metaClass.validate = { -> true }
            true
        }
        flash.message == 'inventoryItem.addedItemToShipment.message[Paracetamol #L1, Shipment 1]'
        response.redirectedUrl == "/inventoryItem/showStockCard?product.id=${product.id}"

        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(Shipment)
    }

    interface InventoryServiceContract {
        List getInventoryItemsByProduct(Product product)
        List getInventoryItemsByProductAndInventory(Product product, Inventory inventory)
        InventoryLevel getInventoryLevelByProductAndInventory(Product product, Inventory inventory)
        Object getStockCardCommand(StockCardCommand command, Map params)
        List getCurrentStockAllLocations(Product product, User user)
        void populateRecordInventoryCommand(RecordInventoryCommand command, Map params)
        List getTransactionEntriesByInventoryAndProduct(Inventory inventory, List products)
        Map getQuantityByProductMap(List transactionEntries)
        Integer getQuantityAvailableToPromise(Product product, Location location)
        Map getInventoryItemsWithQuantity(List products, Inventory inventory)
        void saveRecordInventoryCommand(RecordInventoryCommand command, Map params)
        void adjustStock(AdjustStockCommand command)
        Integer getQuantityFromBinLocation(Location location, Location binLocation, InventoryItem inventoryItem)
        boolean isInventoryItemInOtherLocation(Inventory inventory, InventoryItem inventoryItem)
        Transaction transferStock(TransferStockCommand command)
        InventoryItem findByProductAndLotNumber(Product product, String lotNumber)
        void recallInventoryItem(InventoryItem inventoryItem)
        void revertRecallInventoryItem(InventoryItem inventoryItem)
    }

    interface ProductAvailabilityContract {
        Map getQuantityAvailableToPromiseByProduct(Location location, List products)
        void refreshProductsAvailability(String locationId, List productIds, boolean forceRefresh)
    }

    interface ShipmentServiceContract {
        List getPendingInboundShipmentItems(Location location, Product product)
        boolean validateShipmentItem(ShipmentItem shipmentItem)
    }

    interface OrderServiceContract {
        List getPendingInboundOrderItems(Location location, Product product)
    }

    interface RequisitionServiceContract {
        List getPendingRequisitionItems(Location location, Product product)
    }

    interface ForecastingServiceContract {
        List getDemandDetails(Location origin, Location destination, Product product, Date startDate, Date endDate)
        List getDemandDetails(Location origin, Product product)
        List getAvailableDestinationsForDemandDetails(Location origin, Product product, Date startDate, Date endDate)
    }

    interface DataServiceContract {
        String generateCsv(Object data)
    }

    interface UserServiceContract {
        boolean isUserAdmin(Object user)
    }
}
