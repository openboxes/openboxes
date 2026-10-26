package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class InventoryControllerSpec extends Specification implements ControllerUnitTest<InventoryController>, DataTest {

    Location depot
    Inventory inventory
    Category category
    Product product
    InventoryItem lot
    User user
    TransactionType adjustmentType

    void setupSpec() {
        mockDomains(Location, LocationType, Inventory, Product, Category, InventoryItem, InventoryLevel,
                Transaction, TransactionEntry, TransactionType, User)
    }

    void setup() {
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.localizationService >> Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
        LocalizationUtil.currentLocale >> Locale.ENGLISH

        controller.metaClass.warehouse = [message: { Map attrs -> attrs.code + (attrs.args ? attrs.args.toString() : '') }]
        controller.metaClass.formatDate = { Map attrs -> attrs.date ? attrs.date.format(attrs.format) : '' }

        LocationType depotType = new LocationType(name: 'Depot',
                supportedActivities: [ActivityCode.MANAGE_INVENTORY.id, ActivityCode.RECEIVE_STOCK.id]).save(validate: false)
        depot = new Location(name: 'Main depot', locationType: depotType).save(validate: false)
        inventory = new Inventory(warehouse: depot).save(validate: false)
        depot.inventory = inventory
        depot.save(validate: false)

        category = new Category(name: 'Medicines').save(validate: false)
        product = new Product(name: 'Paracetamol', productCode: 'PC1', category: category, pricePerUnit: 2.5,
                unitOfMeasure: 'EA').save(validate: false)
        lot = new InventoryItem(product: product, lotNumber: 'L1', expirationDate: new Date(130, 0, 1)).save(validate: false)
        user = new User(username: 'manager', firstName: 'Mia', lastName: 'Manager').save(validate: false)
        adjustmentType = new TransactionType(name: 'Adjustment', transactionCode: TransactionCode.CREDIT)
        adjustmentType.id = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
        adjustmentType.save(validate: false)

        session.warehouse = depot
        session.user = user

        controller.productService = Stub(Object)
        controller.dashboardService = Mock(DashboardServiceContract)
        controller.inventoryService = Mock(InventoryServiceContract)
        controller.inventorySnapshotService = Mock(InventorySnapshotServiceContract)
        controller.productAvailabilityService = Mock(ProductAvailabilityServiceContract)
        controller.userService = Stub(UserServiceContract) {
            hasRoleFinance(_) >> true
        }
        controller.documentService = Mock(DocumentServiceContract)
        controller.transactionIdentifierService = Stub(TransactionIdentifierService) {
            generate(_) >> 'TX-1'
        }
        controller.adjustInventoryService = Stub(AdjustInventoryService) {
            createAdjustInventoryTransactionSource(_) >> null
        }
    }

    void 'index redirects to the inventory browser and simple pages render their views'() {
        when:
        controller.index()

        then:
        response.redirectedUrl == '/inventory/browse'

        when:
        response.reset()
        controller.cycleCount()

        then:
        view == '/common/react'

        when:
        response.reset()
        controller.reorderReport()

        then:
        view == '/common/react'

        when:
        def model = controller.manage(new ManageInventoryCommand(productCodes: 'PC1'))

        then:
        model.command.productCodes == 'PC1'
    }

    void 'binLocations renders the bin location quantities as a datatables payload'() {
        given:
        Location bin = new Location(name: 'A-01', parentLocation: depot).save(validate: false)

        when:
        controller.binLocations()

        then:
        1 * controller.productAvailabilityService.getQuantityOnHandByBinLocation(depot) >> [
                [inventoryItem: lot, binLocation: bin, quantity: 7],
                [inventoryItem: new InventoryItem(product: product), binLocation: null, quantity: 3],
        ]

        and:
        def json = JSON.parse(response.text)
        json.aaData.size() == 2
        json.aaData[0][0] == 'PC1'
        json.aaData[0][1] == 'Paracetamol'
        json.aaData[0][2] == 'A-01'
        json.aaData[0][3] == 'L1'
        json.aaData[0][4] != null
        json.aaData[0][5] == 7
        json.aaData[0][7] == 'None'
        json.aaData[1][0] == 'PC1'
        json.aaData[1][5] == 3
    }

    void 'editBinLocation looks up the bin, the lot and its quantity'() {
        given:
        Location bin = new Location(name: 'A-01', parentLocation: depot).save(validate: false)
        params.productCode = 'PC1'
        params.binLocation = 'A-01'
        params.lotNumber = 'L1'

        when:
        def model = controller.editBinLocation()

        then:
        1 * controller.inventoryService.findInventoryItemByProductAndLotNumber(product, 'L1') >> lot
        1 * controller.inventoryService.getQuantityFromBinLocation(depot, bin, lot) >> 12
        model.location == depot
        model.binLocation == bin
        model.inventoryItem == lot
        model.quantity == 12
    }

    void 'browse fills in the default location, paging and search results'() {
        given:
        InventoryCommand command = new InventoryCommand()
        controller.productService = Stub(ProductServiceContract) {
            getRootCategory() >> category
        }

        when:
        def model = controller.browse(command)

        then:
        1 * controller.productAvailabilityService.searchProducts(command) >> ['result']
        model.commandInstance.location == depot
        model.commandInstance.category == category
        model.commandInstance.maxResults == 10
        model.commandInstance.offset == 0
        model.commandInstance.catalogs == null
        model.commandInstance.tags == null
        model.commandInstance.searchResults == ['result']
    }

    void 'show returns the current location and falls back to its inventory'() {
        when:
        def model = controller.show()

        then:
        model.location == depot
        model.quantityMap == [:]
        model.elapsedTime >= 0
    }

    void 'show redirects to list when neither the inventory nor the location inventory exist'() {
        given:
        Location empty = new Location(name: 'No inventory').save(validate: false)
        session.warehouse = empty

        when:
        controller.show()

        then:
        response.redirectedUrl == '/inventory/list'
        flash.message.startsWith('default.not.found.message')
    }

    void 'save redirects to list when the warehouse cannot be found'() {
        given:
        params['warehouse.id'] = 'missing'

        when:
        controller.save()

        then:
        response.redirectedUrl == '/inventory/list'
        flash.message.startsWith('default.not.found.message')
    }

    void 'save attaches a new inventory to the warehouse'() {
        given:
        Location store = new Location(name: 'Store').save(validate: false)
        params['warehouse.id'] = store.id

        when:
        controller.save()

        then:
        store.inventory != null
    }

    void 'search computes quantities per date and renders the show view'() {
        given:
        request.method = 'POST'
        Date start = new Date(120, 0, 1)
        Date end = new Date(120, 0, 3)
        params['locations[0].id'] = depot.id
        params.startDate = start
        params.endDate = end
        params.frequency = 'Daily'
        Product other = new Product(name: 'Ibuprofen', productCode: 'IB1').save(validate: false)

        when:
        controller.search()

        then:
        3 * controller.inventorySnapshotService.getQuantityOnHandByProduct(depot, _) >>> [
                [(product): 5], [(product): 6, (other): 1], [(product): 7]]
        view == '/inventory/show'
        model.quantityMapByDate.size() == 3
        model.quantityMapByDate[start] == [(product): 5]
        model.command.products as List == [other, product].sort()
    }

    void 'search with frequency #frequency steps the dates by the right interval'() {
        given:
        request.method = 'POST'
        params['locations[0].id'] = depot.id
        params.startDate = new Date(120, 0, 1)
        params.endDate = new Date(121, 0, 1)
        params.frequency = frequency

        when:
        controller.search()

        then:
        expected * controller.inventorySnapshotService.getQuantityOnHandByProduct(depot, _) >> [(product): 1]
        model.quantityMapByDate.size() == expected

        where:
        frequency   | expected
        'Weekly'    | 53
        'Monthly'   | 13
        'Quarterly' | 5
        'Annually'  | 2
    }

    void 'search merges the quantities of several locations on the same date'() {
        given:
        request.method = 'POST'
        Location store = new Location(name: 'Store').save(validate: false)
        Date start = new Date(120, 0, 1)
        params['locations[0].id'] = depot.id
        params['locations[1].id'] = store.id
        params.startDate = start
        params.endDate = new Date(120, 0, 2)
        params.frequency = 'Daily'

        when:
        controller.search()

        then:
        2 * controller.inventorySnapshotService.getQuantityOnHandByProduct(depot, _) >> { [(product): 5] }
        2 * controller.inventorySnapshotService.getQuantityOnHandByProduct(store, _) >> { [(product): 3] }
        model.quantityMapByDate[start] == [(product): 8]
    }

    void 'search download exports a baseline CSV'() {
        given:
        request.method = 'POST'
        params['locations[0].id'] = depot.id
        params.startDate = new Date(120, 0, 1)
        params.endDate = new Date(120, 0, 2)
        params.frequency = 'Daily'
        params.button = 'download'

        when:
        controller.search()

        then:
        2 * controller.inventorySnapshotService.getQuantityOnHandByProduct(depot, _) >> [(product): 5]
        1 * controller.inventoryService.exportBaselineQoH(_, _) >> 'code,qty\nPC1,5'
        response.contentType.startsWith('text/csv')
        response.getHeader('Content-disposition').contains('Baseline-QoH-')
        response.text == 'code,qty\nPC1,5'
    }

    void 'search download without products answers 404'() {
        given:
        request.method = 'POST'
        params.button = 'download'

        when:
        controller.search()

        then:
        response.status == 404
        response.text == 'No products found'
    }

    void 'addToInventory redirects with a message when the product is missing'() {
        given:
        params['product.id'] = 'missing'

        when:
        controller.addToInventory()

        then:
        response.redirectedUrl == '/inventory/browse'
        flash.message.startsWith('default.not.found.message')
    }

    void 'addToInventory creates an inventory item for the product'() {
        given:
        params.id = inventory.id
        params['product.id'] = product.id

        when:
        controller.addToInventory()

        then:
        InventoryItem.countByProduct(product) == 2
        response.redirectedUrl == "/inventory/browse/${inventory.id}"
    }

    void 'edit, update and delete an inventory'() {
        when:
        params.id = 'missing'
        controller.edit()

        then:
        response.redirectedUrl == '/inventory/list'

        when:
        response.reset()
        params.id = inventory.id
        def model = controller.edit()

        then:
        model.inventoryInstance == inventory
        model.productInstanceMap.values().flatten().contains(product)

        when:
        response.reset()
        controller.update()

        then:
        response.redirectedUrl == "/inventory/browse/${inventory.id}"
        flash.message.startsWith('default.updated.message')

        when:
        response.reset()
        params.id = 'missing'
        controller.update()

        then:
        response.redirectedUrl == '/inventory/list'

        when:
        response.reset()
        params.id = 'missing'
        controller.delete()

        then:
        response.redirectedUrl == '/inventory/list'
        flash.message.startsWith('default.not.found.message')

        when:
        response.reset()
        Inventory spare = new Inventory(warehouse: new Location(name: 'Spare').save(validate: false)).save(validate: false)
        params.id = spare.id
        controller.delete()

        then:
        response.redirectedUrl == '/inventory/list'
        flash.message.startsWith('default.deleted.message')
    }

    void 'update rejects a stale version'() {
        given:
        inventory.version = 5
        inventory.save(validate: false)
        params.id = inventory.id
        params.version = '1'

        when:
        controller.update()

        then:
        view == '/inventory/edit'
        model.inventoryInstance.errors.getFieldError('version') != null
    }

    void 'addItem reports an existing lot and otherwise creates it'() {
        given:
        params['inventory.id'] = inventory.id
        params['product.id'] = product.id
        params.lotNumber = 'L1'

        when:
        controller.addItem()

        then:
        1 * controller.inventoryService.findByProductAndLotNumber(product, 'L1') >> lot
        response.redirectedUrl == "/inventory/show/${inventory.id}"
        flash.message.startsWith('default.alreadyExists.message')

        when:
        response.reset()
        params.lotNumber = 'L2'
        controller.addItem()

        then:
        1 * controller.inventoryService.findByProductAndLotNumber(product, 'L2') >> null
        InventoryItem.findByLotNumber('L2') != null
        view == '/inventory/show'
        model.itemInstance.lotNumber == 'L2'
    }

    void 'deleteItem deletes an existing item or reports it missing'() {
        given:
        InventoryItem spare = new InventoryItem(product: product, lotNumber: 'SPARE').save(validate: false)
        params['inventory.id'] = inventory.id

        when:
        params.id = spare.id
        controller.deleteItem()

        then:
        response.redirectedUrl == "/inventory/show/${inventory.id}"
        flash.message.startsWith('default.deleted.message')

        when:
        response.reset()
        params.id = 'missing'
        controller.deleteItem()

        then:
        flash.message.startsWith('default.not.found.message')
    }

    void 'listDailyTransactions groups transactions by day'() {
        given:
        Date today = new Date().clearTime()
        createTransaction(today)
        createTransaction(today - 1)
        params.date = today.format('dd/MM/yyyy')

        when:
        def model = controller.listDailyTransactions()

        then:
        model.dateSelected == today
        model.transactions.size() == 1
        model.transactionsByDate*.key == [today, today - 1]
    }

    void 'stock list #action renders the list view or downloads a CSV'() {
        given:
        Map item = [product: product, quantity: 4, status: 'IN_STOCK', quantityAvailableToPromise: 3,
                    inventoryLevel: new InventoryLevel(minQuantity: 1, reorderQuantity: 2, maxQuantity: 9, forecastQuantity: 3, abcClass: 'B')]

        when:
        controller."$action"()

        then:
        1 * controller.inventoryService.getExplodedCategories([]) >> []
        1 * controller.dashboardService."$serviceMethod"(depot, []) >> [item]
        view == '/inventory/list'
        model.availableItems == [item]

        when:
        response.reset()
        params.button = 'download'
        controller."$action"()

        then:
        1 * controller.inventoryService.getExplodedCategories([]) >> []
        1 * controller.dashboardService."$serviceMethod"(depot, []) >> [item]
        response.getHeader('Content-disposition').contains(prefix + 'Main depot.csv')
        response.text.contains('enum.InventoryLevelStatusCsv.IN_STOCK')
        response.text.contains('"B"')
        response.text.contains('Medicines')
        response.text.contains('10.0')

        where:
        action                    | serviceMethod           | prefix
        'list'                    | 'getInventoryItems'     | ''
        'listReconditionedStock'  | 'getReconditionedStock' | 'Reconditioned stock - '
        'listTotalStock'          | 'getTotalStock'         | 'Total stock - '
        'listInStock'             | 'getInStock'            | 'In stock - '
        'listLowStock'            | 'getLowStock'           | 'Low stock - '
        'listReorderStock'        | 'getReorderStock'       | 'Reorder stock - '
        'listQuantityOnHandZero'  | 'getQuantityOnHandZero' | 'Out of stock  - all - '
        'listHealthyStock'        | 'getHealthyStock'       | 'Overstock - '
        'listOverStock'           | 'getOverStock'          | 'Overstock - '
    }

    void 'stock lists explode the selected categories unless subcategories are excluded'() {
        given:
        Category child = new Category(name: 'Analgesics', parentCategory: category).save(validate: false)
        params.categories = [category.id]

        when:
        controller.list()

        then:
        1 * controller.inventoryService.getExplodedCategories([category]) >> [category, child]
        1 * controller.dashboardService.getInventoryItems(depot, [category, child]) >> []

        when:
        response.reset()
        params._includeSubcategories = ''
        params.includeSubcategories = false
        controller.list()

        then:
        0 * controller.inventoryService.getExplodedCategories(_)
        1 * controller.dashboardService.getInventoryItems(depot, [category]) >> []
    }

    void 'listOutOfStock renders the list and the CSV download'() {
        given:
        params.abcClass = 'A'
        Map item = [product: product, quantity: 0, status: 'STOCK_OUT']

        when:
        controller.listOutOfStock()

        then:
        1 * controller.inventoryService.getExplodedCategories([]) >> []
        1 * controller.dashboardService.getOutOfStock(depot, 'A', []) >> [item]
        model.availableItems == [item]

        when:
        response.reset()
        params.button = 'download'
        controller.listOutOfStock()

        then:
        1 * controller.inventoryService.getExplodedCategories([]) >> []
        1 * controller.dashboardService.getOutOfStock(depot, 'A', []) >> [item]
        response.getHeader('Content-disposition').contains('Out of stock - supported - Main depot.csv')
        response.text.contains('enum.InventoryLevelStatusCsv.STOCK_OUT')
    }

    void '#action lists items by inventory item and exports them as CSV'() {
        given:
        new InventoryLevel(product: product, inventory: inventory, minQuantity: 2, reorderQuantity: 5, maxQuantity: 20)
                .save(validate: false)

        when:
        def result = controller."$action"()

        then:
        1 * controller.dashboardService."$serviceMethod"(_) >> [lot]
        1 * controller.productAvailabilityService.getQuantityOnHandByInventoryItem(depot, [lot]) >> [(lot): 3]
        result.data == [[inventoryItem: lot, quantity: 3]]
        result.categories == [category]
        result.command.location == depot

        when:
        response.reset()
        params.format = 'csv'
        controller."$action"()

        then:
        1 * controller.dashboardService."$serviceMethod"(_) >> [lot]
        1 * controller.productAvailabilityService.getQuantityOnHandByInventoryItem(depot, [lot]) >> [(lot): 3]
        response.getHeader('Content-disposition').contains(filePrefix + ' | Main depot.csv')
        response.text.contains('"PC1"')
        response.text.contains('L1')
        response.text.contains('01/01/2030')
        response.text.contains('7.5')

        where:
        action              | serviceMethod      | filePrefix
        'listExpiredStock'  | 'getExpiredStock'  | 'Expired stock'
        'listExpiringStock' | 'getExpiringStock' | 'Expiring stock'
    }

    void '#action with bin locations uses the bin location quantities'() {
        given:
        params.withBinLocation = 'true'
        params.format = 'csv'

        when:
        controller."$action"()

        then:
        1 * controller.dashboardService."$serviceMethod"(_) >> [lot]
        1 * controller.productAvailabilityService.getAvailableQuantityOnHandByBinLocation(depot, [lot]) >>
                [[inventoryItem: lot, binLocation: 'A-01', quantity: 0]]
        response.text.contains('inventoryLevel.binLocation.label')
        response.text.contains('"A-01"')
        response.text.contains('STOCK_OUT')

        where:
        action              | serviceMethod
        'listExpiredStock'  | 'getExpiredStock'
        'listExpiringStock' | 'getExpiringStock'
    }

    void 'exportLatestInventoryDate streams the CSV for the current location'() {
        when:
        controller.exportLatestInventoryDate()

        then:
        1 * controller.dashboardService.exportLatestInventoryDate(depot) >> 'code,date'
        response.contentType.startsWith('text/csv')
        response.getHeader('Content-disposition').contains('MostRecentStockCount-')
        response.text == 'code,date'
    }

    void 'createDefaultInventoryItems creates a blank lot per product and showProducts lists them'() {
        given:
        Product other = new Product(name: 'Ibuprofen', productCode: 'IB1').save(validate: false)

        when:
        controller.createDefaultInventoryItems()

        then:
        1 * controller.inventoryService.findProductsWithoutEmptyLotNumber() >> [other]
        InventoryItem.findAllByProduct(other).size() == 1
        response.redirectedUrl == '/inventory/showProducts'

        when:
        def model = controller.showProducts()

        then:
        1 * controller.inventoryService.findProductsWithoutEmptyLotNumber() >> [product]
        model.products == [product]
    }

    void 'listTransactions filters the current inventory transactions'() {
        given:
        createTransaction(new Date(120, 0, 10), 'TX-100')
        createTransaction(new Date(120, 0, 20), 'TX-200')
        params.transactionNumber = '100'
        params['transactionType.id'] = adjustmentType.id
        params.transactionDateFrom = '01/01/2020'
        params.transactionDateTo = '01/31/2020'

        when:
        controller.listTransactions()

        then:
        view == '/inventory/listTransactions'
        model.transactionInstanceList*.transactionNumber == ['TX-100']
        model.transactionCount == 1
        model.transactionTypeSelected == adjustmentType
    }

    void 'transaction list shortcuts'() {
        given:
        Transaction confirmed = createTransaction(new Date() - 1, 'TX-C')
        confirmed.confirmed = true
        confirmed.save(validate: false)
        createTransaction(new Date() - 1, 'TX-P')

        when:
        controller.listAllTransactions()

        then:
        response.redirectedUrl == '/inventory/listTransactions'

        when:
        response.reset()
        controller.listPendingTransactions()

        then:
        model.transactionInstanceList*.transactionNumber == ['TX-P']

        when:
        response.reset()
        controller.listConfirmedTransactions()

        then:
        model.transactionInstanceList*.transactionNumber == ['TX-C']
    }

    void 'deleteTransaction delegates to the service or reports a missing transaction'() {
        given:
        Transaction transaction = createTransaction(new Date() - 1)

        when:
        params.id = transaction.id
        controller.deleteTransaction()

        then:
        1 * controller.inventoryService.deleteTransaction(transaction)
        response.redirectedUrl == '/inventory/listTransactions'
        flash.message.startsWith('default.deleted.message')

        when:
        response.reset()
        controller.deleteTransaction()

        then:
        1 * controller.inventoryService.deleteTransaction(transaction) >> {
            throw new org.springframework.dao.DataIntegrityViolationException('in use')
        }
        response.redirectedUrl == "/inventory/editTransaction/${transaction.id}"
        flash.message.startsWith('default.not.deleted.message')

        when:
        response.reset()
        params.id = 'missing'
        controller.deleteTransaction()

        then:
        response.redirectedUrl == '/inventory/listTransactions'
        flash.message.startsWith('default.not.found.message')
    }

    void 'saveTransaction saves a valid transaction and reports an invalid one'() {
        given:
        Transaction transaction = createTransaction(new Date() - 1)

        when:
        params.id = transaction.id
        params.comment = 'checked'
        controller.saveTransaction()

        then:
        Transaction.get(transaction.id).comment == 'checked'
        flash.message == 'inventory.transactionSaved.message'
        response.redirectedUrl == "/inventory/editTransaction/${transaction.id}"

        when:
        response.reset()
        params.clear()
        controller.saveTransaction()

        then:
        flash.message == 'inventory.unableToSaveTransaction.message'
        flash.errors.hasFieldErrors('transactionType')
    }

    void 'showTransaction and showTransactionDialog render a transaction or a blank one'() {
        given:
        Transaction transaction = createTransaction(new Date() - 1)

        when:
        params.id = transaction.id
        controller.showTransaction()

        then:
        view == '/inventory/showTransaction'
        model.transactionInstance == transaction

        when:
        response.reset()
        params.id = 'missing'
        controller.showTransaction()

        then:
        flash.message.startsWith('inventory.noTransactionWithId.message')
        model.transactionInstance.id == null

        when:
        response.reset()
        params.id = transaction.id
        controller.showTransactionDialog()

        then:
        view == '/inventory/showTransactionDialog'
        model.transactionInstance == transaction
        model.transactionTypeList.contains(adjustmentType)
        model.warehouseInstance == depot
        model.productInstanceMap[category] == [product]

        when:
        response.reset()
        params.id = 'missing'
        controller.showTransactionDialog()

        then:
        flash.message.startsWith('inventory.noTransactionWithId.message')
    }

    void 'confirmTransaction toggles the confirmation'() {
        given:
        Transaction transaction = createTransaction(new Date() - 1)
        params.id = transaction.id

        when:
        controller.confirmTransaction()

        then:
        transaction.confirmed
        transaction.confirmedBy == user
        transaction.dateConfirmed != null
        flash.message == 'inventory.transactionHasBeenConfirmed.message'
        response.redirectedUrl == '/inventory/listTransactions'

        when:
        response.reset()
        controller.confirmTransaction()

        then:
        !transaction.confirmed
        transaction.confirmedBy == null
        flash.message == 'inventory.transactionHasBeenUnconfirmed.message'
    }

    void 'create transfer shortcuts forward to createTransaction with their transaction type'() {
        when:
        controller.createInboundTransfer()

        then:
        params.transactionType == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
        response.forwardedUrl?.contains('createTransaction')

        when:
        response.reset()
        controller.createExpired()

        then:
        params.transactionType == Constants.EXPIRATION_TRANSACTION_TYPE_ID

        when:
        response.reset()
        controller.createDamaged()

        then:
        params.transactionType == Constants.DAMAGE_TRANSACTION_TYPE_ID
    }

    void '#action is refused at a location that does not support the activity'() {
        when:
        controller."$action"()

        then:
        UnsupportedOperationException e = thrown()
        e.message.contains('Main depot')

        where:
        action << ['createOutboundTransfer', 'createAdjustment', 'createConsumed']
    }

    void 'create shortcuts forward when the location supports the activity'() {
        given:
        depot.supportedActivities = [ActivityCode.SEND_STOCK.id, ActivityCode.ADJUST_INVENTORY.id, ActivityCode.CONSUME_STOCK.id] as Set
        depot.save(validate: false)

        when:
        controller.createOutboundTransfer()

        then:
        params.transactionType == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID

        when:
        response.reset()
        controller.createAdjustment()

        then:
        params.transactionType == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID

        when:
        response.reset()
        controller.createConsumed()

        then:
        params.transactionType == Constants.CONSUMPTION_TRANSACTION_TYPE_ID
    }

    void 'createTransaction builds the command from products or inventory items'() {
        when:
        params['product.id'] = product.id
        def model = controller.createTransaction()

        then:
        1 * controller.inventoryService.getInventoryItemsByProducts(depot, [product.id]) >> [(product): [lot]]
        1 * controller.inventoryService.getProductQuantityByBinLocation(depot, [product]) >> ['bins']
        1 * controller.inventoryService.getQuantityForInventory(inventory, [product]) >> [(lot): 4]
        model.command.productInventoryItems == [(product): [lot]]
        model.command.binLocations == ['bins']
        model.command.quantityMap == [(lot): 4]
        model.command.warehouseInstance == depot

        when:
        params.remove('product.id')
        params.remove('product')
        params['inventoryItem.id'] = lot.id
        model = controller.createTransaction()

        then:
        1 * controller.inventoryService.getBinLocationsByInventoryItems(depot, [lot]) >> ['lot bins']
        1 * controller.inventoryService.getQuantityForInventory(inventory, [product]) >> [:]
        model.command.binLocations == ['lot bins']
    }

    void 'createTransaction requires a product or an inventory item'() {
        when:
        controller.createTransaction()

        then:
        RuntimeException e = thrown()
        e.message.contains('at least one product')
    }

    void 'saveAdjustmentTransaction rejects negative quantities and re-renders the form'() {
        given:
        TransactionCommand command = new TransactionCommand(transactionInstance: new Transaction(transactionType: adjustmentType,
                transactionDate: new Date() - 1, inventory: inventory))
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, quantity: -2)]
        params['product.id'] = product.id

        when:
        controller.saveAdjustmentTransaction(command)

        then:
        1 * controller.inventoryService.getInventoryItemsByProducts(depot, [product.id]) >> [(product): [lot]]
        view == '/inventory/createTransaction'
        model.command.transactionInstance.errors.hasFieldErrors('transactionEntries')
        model.command.warehouseInstance == depot
    }

    void 'saveAdjustmentTransaction saves the adjustment and redirects to the stock card'() {
        given:
        TransactionCommand command = new TransactionCommand(transactionInstance: new Transaction(transactionType: adjustmentType,
                transactionDate: new Date() - 1, inventory: inventory))
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, quantity: 5, comment: 'count'),
                                      new TransactionEntryCommand(inventoryItem: lot, quantity: 0)]

        when:
        controller.saveAdjustmentTransaction(command)

        then:
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
        flash.message == 'Successfully saved transaction'
        Transaction.count() == 1
        Transaction.list().first().transactionEntries*.quantity == [5]
    }

    void 'saveDebitTransaction rejects quantities above the quantity on hand'() {
        given:
        TransactionCommand command = new TransactionCommand()
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, product: product, quantity: 10)]
        params['product.id'] = product.id
        params.transactionInstance = [transactionDate: new Date() - 1, 'transactionType.id': adjustmentType.id]

        when:
        controller.saveDebitTransaction(command)

        then:
        1 * controller.inventoryService.getQuantityForInventory(inventory, [product]) >> [(lot): 4]
        1 * controller.inventoryService.getInventoryItemsByProducts(depot, [product.id]) >> [(product): [lot]]
        view == '/inventory/createTransaction'
        model.command.transactionInstance.transactionNumber == 'TX-1'
        model.command.transactionInstance.errors.hasFieldErrors('transactionEntries')
        model.command.quantityMap == [(lot): 4]
    }

    void 'saveDebitTransaction saves a valid debit and redirects to the stock card'() {
        given:
        TransactionCommand command = new TransactionCommand()
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, product: product, quantity: 3)]
        params['product.id'] = product.id
        params.transactionInstance = [transactionDate: new Date() - 1, transactionType: adjustmentType,
                                      inventory: inventory]

        when:
        controller.saveDebitTransaction(command)

        then:
        1 * controller.inventoryService.getQuantityForInventory(inventory, [product]) >> [(lot): 4]
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
        Transaction.findByTransactionNumber('TX-1').transactionEntries*.quantity == [3]
    }

    void 'saveCreditTransaction creates the missing lot and saves the credit'() {
        given:
        TransactionCommand command = new TransactionCommand(transactionInstance: new Transaction(transactionType: adjustmentType,
                transactionDate: new Date() - 1, inventory: inventory))
        command.transactionEntries = [new TransactionEntryCommand(product: product, lotNumber: 'NEW', expirationDate: new Date(131, 0, 1), quantity: 6)]

        when:
        controller.saveCreditTransaction(command)

        then:
        1 * controller.inventoryService.findInventoryItemByProductAndLotNumber(product, 'NEW') >> null
        InventoryItem.findByLotNumber('NEW')?.expirationDate == new Date(131, 0, 1)
        response.redirectedUrl == "/inventoryItem/showStockCard/${product.id}"
        flash.message == 'Successfully saved transaction'
    }

    void 'saveCreditTransaction rejects negative quantities'() {
        given:
        TransactionCommand command = new TransactionCommand(transactionInstance: new Transaction(transactionType: adjustmentType,
                transactionDate: new Date() - 1, inventory: inventory))
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, quantity: -1)]
        params['inventoryItem.id'] = lot.id

        when:
        controller.saveCreditTransaction(command)

        then:
        view == '/inventory/createTransaction'
        model.command.productInventoryItems == [(product): [lot]]
        model.command.warehouseInstance == depot
    }

    void 'editTransaction renders the transaction or redirects when it is missing'() {
        given:
        Transaction transaction = createTransaction(new Date() - 1)
        transaction.addToTransactionEntries(new TransactionEntry(inventoryItem: lot, product: product, quantity: 1))
        transaction.save(validate: false)

        when:
        params.id = transaction.id
        controller.editTransaction()

        then:
        view == '/inventory/editTransaction'
        model.inventoryItemsMap[product.id] == [lot]
        model.warehouseInstance == depot

        when:
        response.reset()
        params.id = 'missing'
        controller.editTransaction()

        then:
        response.redirectedUrl == '/inventory/listTransactions'
    }

    void 'upload on GET returns an empty inventory list'() {
        when:
        def model = controller.upload()

        then:
        model.inventoryList == [:]
    }

    void 'downloadTemplate writes the rows with quantity, or one blank row'() {
        given:
        Location bin = new Location(name: 'A-01').save(validate: false)
        List rows = null

        when:
        controller.downloadTemplate()

        then:
        1 * controller.productAvailabilityService.getQuantityOnHandByBinLocation(_) >> [
                [product: product, inventoryItem: lot, binLocation: bin, quantity: 4],
                [product: product, inventoryItem: lot, binLocation: null, quantity: 0]]
        1 * controller.documentService.generateInventoryTemplate(_, _) >> { out, r -> rows = r }
        response.contentType == 'application/vnd.ms-excel'
        response.getHeader('Content-disposition').contains('inventory.xls')
        rows.size() == 1
        rows[0]['Product code'] == 'PC1'
        rows[0]['Bin location'] == 'A-01'
        rows[0]['OB QOH'] == 4
        rows[0]['Expiration date'] == '01/01/2030'

        when:
        response.reset()
        controller.downloadTemplate()

        then:
        1 * controller.productAvailabilityService.getQuantityOnHandByBinLocation(_) >> []
        1 * controller.documentService.generateInventoryTemplate(_, _) >> { out, r -> rows = r }
        rows.size() == 1
        rows[0]['Product code'] == ''
    }

    void 'saveInventoryChanges requires entries with quantities'() {
        given:
        ManageInventoryCommand command = new ManageInventoryCommand()
        command.entries = [new ManageInventoryEntryCommand(inventoryItem: lot, quantity: 0)]

        when:
        controller.saveInventoryChanges(command)

        then:
        Transaction.count() == 0
        response.redirectedUrl?.contains('/inventory/manage')
    }

    void 'saveInventoryChanges saves a transaction with the entered quantities'() {
        given:
        ManageInventoryCommand command = new ManageInventoryCommand()
        command.entries = [new ManageInventoryEntryCommand(inventoryItem: lot, quantity: 6),
                           new ManageInventoryEntryCommand(inventoryItem: lot, quantity: 0)]
        params['transactionType.id'] = adjustmentType.id
        params.transactionDate = new Date() - 1
        params.tags = 'tag1'

        when:
        controller.saveInventoryChanges(command)

        then:
        Transaction saved = Transaction.list().first()
        saved.transactionEntries*.quantity == [6]
        saved.inventory == inventory
        flash.message == "Transaction ${saved.id} saved"
        response.redirectedUrl == '/inventory/manage?tags=tag1'
    }

    void 'saveAdjustmentTransaction re-renders the lots that were selected'() {
        given:
        TransactionCommand command = new TransactionCommand(transactionInstance: new Transaction(transactionType: adjustmentType,
                transactionDate: new Date() - 1, inventory: inventory))
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, quantity: -1)]
        params['inventoryItem.id'] = lot.id

        when:
        controller.saveAdjustmentTransaction(command)

        then:
        view == '/inventory/createTransaction'
        model.command.productInventoryItems == [(product): [lot]]
    }

    void 'saveDebitTransaction re-renders the lots that were selected'() {
        given:
        TransactionCommand command = new TransactionCommand()
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, product: product, quantity: 10)]
        params['inventoryItem.id'] = lot.id
        params.transactionInstance = [transactionDate: new Date() - 1, transactionType: adjustmentType, inventory: inventory]

        when:
        controller.saveDebitTransaction(command)

        then:
        1 * controller.inventoryService.getQuantityForInventory(inventory, []) >> [(lot): 4]
        view == '/inventory/createTransaction'
        model.command.productInventoryItems == [(product): [lot]]
    }

    void 'saveCreditTransaction reloads the products after a rejected quantity'() {
        given:
        TransactionCommand command = new TransactionCommand(transactionInstance: new Transaction(transactionType: adjustmentType,
                transactionDate: new Date() - 1, inventory: inventory))
        command.transactionEntries = [new TransactionEntryCommand(inventoryItem: lot, quantity: -1)]
        params['product.id'] = product.id

        when:
        controller.saveCreditTransaction(command)

        then:
        1 * controller.inventoryService.getInventoryItemsByProducts(depot, [product.id]) >> [(product): [lot]]
        view == '/inventory/createTransaction'
        model.command.productInventoryItems == [(product): [lot]]
    }

    private Transaction createTransaction(Date date, String number = null) {
        Transaction transaction = new Transaction(transactionDate: date, transactionType: adjustmentType,
                inventory: inventory, transactionNumber: number)
        transaction.save(validate: false)
        return transaction
    }

    // Narrow typed views of the collaborators so Spock mocks can verify calls with arguments
    @PendingFeature(reason = 'OBAF download reads command.location, which QuantityOnHandReportCommand does not have')
    void 'download exports the stock of the selected location as CSV'() {
        given:
        params['locations[0].id'] = depot.id
        params.startDate = new Date(120, 0, 1)

        when:
        controller.download()

        then:
        1 * controller.inventoryService.getQuantityOnHandAsOfDate(depot, _, _) >> [(product): 4]
        1 * controller.dashboardService.getInventoryStatus(depot) >> [(product): 'IN_STOCK']
        1 * controller.inventoryService.getQuantityAvailableToPromise(product, depot) >> 3
        response.getHeader('Content-disposition').contains('All Products - Main depot - 2020Jan01.csv')
        response.text.contains('Paracetamol')
    }

    @PendingFeature(reason = 'OBAF download reads command.location, which QuantityOnHandReportCommand does not have')
    void 'download falls back to the current location and reports an empty result'() {
        when:
        controller.download()

        then:
        1 * controller.inventoryService.getQuantityOnHandAsOfDate(depot, _, _) >> [:]
        flash.message == 'There are no search results available to download - please try again.'
        response.redirectedUrl == '/inventory/show'
    }

    @PendingFeature(reason = 'OBAF filter actions redirect(action: browse) with browse unquoted')
    void '#action changes the browse filters and returns to the browser'() {
        given:
        session.inventoryCategoryFilters = []
        session.inventorySearchTerms = ['para']
        params.categoryId = category.id
        params.searchTerm = 'para'

        when:
        controller."$action"()

        then:
        response.redirectedUrl == '/inventory/browse'

        where:
        action << ['removeCategoryFilter', 'clearAllFilters', 'addCategoryFilter', 'narrowCategoryFilter', 'removeSearchTerm']
    }

    interface DashboardServiceContract {
        Map getInventoryStatus(Location location)
        List getInventoryItems(Location location, List categories)
        List getReconditionedStock(Location location, List categories)
        List getTotalStock(Location location, List categories)
        List getInStock(Location location, List categories)
        List getLowStock(Location location, List categories)
        List getReorderStock(Location location, List categories)
        List getQuantityOnHandZero(Location location, List categories)
        List getHealthyStock(Location location, List categories)
        List getOverStock(Location location, List categories)
        List getOutOfStock(Location location, String abcClass, List categories)
        List getExpiredStock(Object command)
        List getExpiringStock(Object command)
        String exportLatestInventoryDate(Location location)
    }

    interface InventoryServiceContract {
        InventoryItem findInventoryItemByProductAndLotNumber(Product product, String lotNumber)
        Integer getQuantityFromBinLocation(Location location, Location binLocation, InventoryItem inventoryItem)
        String exportBaselineQoH(Object products, Object quantityMapByDate)
        Map getQuantityOnHandAsOfDate(Location location, Date date, Object tags)
        Integer getQuantityAvailableToPromise(Product product, Location location)
        InventoryItem findByProductAndLotNumber(Product product, String lotNumber)
        List getExplodedCategories(List categories)
        List findProductsWithoutEmptyLotNumber()
        void deleteTransaction(Transaction transaction)
        Map getInventoryItemsByProducts(Location location, List productIds)
        List getProductQuantityByBinLocation(Location location, List products)
        List getBinLocationsByInventoryItems(Location location, List inventoryItems)
        Map getQuantityForInventory(Inventory inventory, List products)
    }

    interface InventorySnapshotServiceContract {
        Map getQuantityOnHandByProduct(Location location, Date date)
    }

    interface ProductAvailabilityServiceContract {
        List getQuantityOnHandByBinLocation(Location location)
        Object searchProducts(InventoryCommand command)
        List getAvailableQuantityOnHandByBinLocation(Location location, List inventoryItems)
        Map getQuantityOnHandByInventoryItem(Location location, List inventoryItems)
    }

    interface ProductServiceContract {
        Category getRootCategory()
    }

    interface UserServiceContract {
        boolean hasRoleFinance(Object user)
    }

    interface DocumentServiceContract {
        void generateInventoryTemplate(OutputStream out, List rows)
    }
}
