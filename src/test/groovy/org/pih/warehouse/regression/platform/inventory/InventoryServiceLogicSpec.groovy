package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.springframework.context.ApplicationContext
import org.springframework.validation.MapBindingResult
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.LocalizationUtil
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.MessageService
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.User
import org.pih.warehouse.data.DataService
import org.pih.warehouse.order.OrderService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentService

/**
 * The parts of the inventory service that work on objects in memory: quantity calculation from transaction
 * entries, sorting and grouping, validation of commands and the combination of collaborator results.
 */
@Unroll
class InventoryServiceLogicSpec extends Specification implements ServiceUnitTest<InventoryService>, DataTest {

    void setupSpec() {
        mockDomains(Location, LocationType, LocationGroup, Inventory, InventoryItem, Product, Category, Transaction,
                TransactionEntry, TransactionType, Shipment, ShipmentItem, User, Role, InventoryLevel)
    }

    void setup() {
        GroovyMock(LocalizationUtil, global: true)
        LocalizationUtil.currentLocale >> Locale.ENGLISH
        LocalizationUtil.localizationService >> Stub(LocalizationService) {
            getCurrentLocale() >> Locale.ENGLISH
        }
    }

    private static Product product(String id, String name) {
        Product product = new Product(name: name, productCode: id.toUpperCase(), synonyms: [])
        product.id = id
        return product
    }

    private static InventoryItem item(String id, Product product, String lotNumber, Date expirationDate = null) {
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: lotNumber, expirationDate: expirationDate)
        inventoryItem.id = id
        return inventoryItem
    }

    private static Location bin(String name) {
        Location location = new Location(name: name, locationType: new LocationType(supportedActivities: [] as Set))
        location.id = name
        return location
    }

    private static TransactionEntry entry(String id, Date date, TransactionCode code, InventoryItem inventoryItem,
                                          Location binLocation, int quantity, Transaction transaction = null) {
        Transaction parent = transaction ?: new Transaction(transactionDate: date, dateCreated: date,
                transactionType: new TransactionType(transactionCode: code))
        parent.id = parent.id ?: "t-${id}"
        TransactionEntry transactionEntry = new TransactionEntry(transaction: parent, inventoryItem: inventoryItem,
                binLocation: binLocation, quantity: quantity)
        transactionEntry.id = id
        return transactionEntry
    }

    void 'getStatusMessage should be #expected for status #status with levels #min/#reorder/#max and quantity #quantity'() {
        expect:
        service.getStatusMessage(status, min, reorder, max, quantity) == expected

        where:
        status                                  | min  | reorder | max  | quantity || expected
        null                                    | 10   | 20      | 50   | 0        || 'STOCK_OUT'
        InventoryStatus.SUPPORTED               | 10   | 20      | 50   | -2       || 'STOCK_OUT'
        null                                    | 10   | 20      | 50   | 10       || 'LOW_STOCK'
        null                                    | 10   | 20      | 50   | 15       || 'REORDER'
        null                                    | null | 20      | 50   | 20       || 'REORDER'
        null                                    | 10   | 20      | 50   | 51       || 'OVERSTOCK'
        null                                    | 10   | 20      | 50   | 30       || 'IN_STOCK'
        null                                    | 0    | 0       | 0    | 1        || 'IN_STOCK'
        InventoryStatus.NOT_SUPPORTED           | 10   | 20      | 50   | 30       || 'NOT_SUPPORTED'
        InventoryStatus.SUPPORTED_NON_INVENTORY | 10   | 20      | 50   | 30       || 'SUPPORTED_NON_INVENTORY'
        InventoryStatus.FORMULARY               | 10   | 20      | 50   | 30       || 'UNAVAILABLE'
    }

    void 'getProductMap groups items by category, puts the uncategorized ones under Unclassified and sorts each group'() {
        given:
        Category drugs = new Category(name: 'Drugs', sortOrder: 1)
        drugs.id = 'drugs'
        Category supplies = new Category(name: 'Supplies', sortOrder: 2)
        supplies.id = 'supplies'
        List items = [
                [category: supplies, description: 'gauze'],
                [category: drugs, description: 'paracetamol'],
                [category: null, description: 'misc b'],
                [category: drugs, description: 'aspirin'],
                [category: null, description: 'misc a'],
        ]

        when:
        Map map = service.getProductMap(items)

        then:
        map.keySet()*.name == ['Unclassified', 'Drugs', 'Supplies']
        map.values().collect { it*.description } == [['misc a', 'misc b'], ['aspirin', 'paracetamol'], ['gauze']]
        service.getProductMap(null).isEmpty()
    }

    void 'sortAvailableItems drops empty items and puts items without an expiration date last'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        AvailableItem noExpiry = new AvailableItem(inventoryItem: item('i1', aspirin, 'NE'), quantityAvailable: 3)
        AvailableItem late = new AvailableItem(inventoryItem: item('i2', aspirin, 'LATE', new Date() + 200), quantityAvailable: 1)
        AvailableItem early = new AvailableItem(inventoryItem: item('i3', aspirin, 'EARLY', new Date() + 10), quantityAvailable: 9)
        AvailableItem empty = new AvailableItem(inventoryItem: item('i4', aspirin, 'EMPTY', new Date() + 5), quantityAvailable: 0)

        expect:
        service.sortAvailableItems([noExpiry, late, empty, early])*.inventoryItem*.lotNumber == ['EARLY', 'LATE', 'NE']
        service.sortAvailableItems([noExpiry, late, empty, early], false)*.inventoryItem*.lotNumber ==
                ['EMPTY', 'EARLY', 'LATE', 'NE']
    }

    void 'groupDuplicatedRecordInventoryRows merges rows of the same lot and bin and keeps the first known old quantity'() {
        given:
        Location binA = bin('A')
        RecordInventoryCommand command = new RecordInventoryCommand(product: product('p1', 'Aspirin'))
        command.recordInventoryRows.addAll([
                new RecordInventoryRowCommand(lotNumber: 'L1', binLocation: binA, oldQuantity: 0, newQuantity: 2),
                new RecordInventoryRowCommand(lotNumber: 'L1', binLocation: binA, oldQuantity: 5, newQuantity: 3),
                new RecordInventoryRowCommand(lotNumber: 'L2', binLocation: binA, oldQuantity: 4, newQuantity: null),
        ])

        when:
        List<RecordInventoryRowCommand> rows = service.groupDuplicatedRecordInventoryRows(command)

        then:
        rows.collect { [it.lotNumber, it.oldQuantity, it.newQuantity] } == [['L1', 5, 5], ['L2', 4, 0]]
    }

    void 'saveRecordInventoryCommand rejects a row with an expiration date but no lot number without saving anything'() {
        given:
        RecordInventoryCommand command = new RecordInventoryCommand(product: product('p1', 'Aspirin'))
        RecordInventoryRowCommand row = new RecordInventoryRowCommand(lotNumber: null, expirationDate: new Date() + 30,
                newQuantity: 4)
        command.recordInventoryRows.add(row)

        when:
        RecordInventoryCommand result = service.saveRecordInventoryCommand(command, [:])

        then:
        result.is(command)
        command.errors.globalErrors*.code == ['inventoryItem.invalid']
        row.error
    }

    void 'saveRecordInventoryCommand requires both lot and expiration date for a lot and expiry controlled product'() {
        given:
        Product controlled = product('p1', 'Vaccine')
        controlled.lotAndExpiryControl = true
        RecordInventoryCommand command = new RecordInventoryCommand(product: controlled)
        RecordInventoryRowCommand row = new RecordInventoryRowCommand(lotNumber: 'L1', expirationDate: null, newQuantity: 4)
        command.recordInventoryRows.add(row)

        when:
        service.saveRecordInventoryCommand(command, [:])

        then:
        command.errors.globalErrors*.defaultMessage == ['Both lot number and expiry date are required for this product.']
        row.error
    }

    void 'validateInventoryData rejects numbers where text is expected and text where a number is expected'() {
        given:
        MapBindingResult errors = new MapBindingResult([:], 'row')

        when:
        service.validateInventoryData([lotNumber: 12.0d, quantity: '5', manufacturerCode: 3.0d, upc: 4.0d, ndc: 5.0d], errors)

        then:
        errors.globalErrors*.code == [
                "Property 'Serial Number / Lot Number' with value '12.0' should be not formatted as a Double value",
                "Property [quantity] with value '5 for '12.0' should not be formatted as a Text value",
                "Property 'Manufacturer Code' with value '3.0' should not be formatted as a Double value",
                "Property 'UPC' with value '4.0' should be not formatted as a Double value",
                "Property 'GTIN' with value '5.0' should be not formatted as a Double value",
        ]
    }

    void 'validateInventoryData accepts text identifiers and numeric quantities'() {
        given:
        MapBindingResult errors = new MapBindingResult([:], 'row')

        when:
        service.validateInventoryData([lotNumber: 'L1', quantity: 5.0d, manufacturerCode: 'M', upc: 'U', ndc: 'N'], errors)

        then:
        !errors.hasErrors()
    }

    void 'getBinLocationSummary counts the bins in and out of stock with their labels'() {
        given:
        service.messageService = Stub(MessageService) {
            getMessage(_ as String) >> { String code -> "label of ${code}".toString() }
        }

        expect:
        service.getBinLocationSummary([[quantity: 5], [quantity: 0], [quantity: -1], [quantity: 3]]) == [
                [status: 'inStock', label: 'label of binLocationSummary.inStock.label', count: 2],
                [status: 'outOfStock', label: 'label of binLocationSummary.outOfStock.label', count: 2],
        ]
        service.getBinLocationSummary([])*.count == [0, 0]
    }

    void 'getOutgoingQuantityByProduct and getIncomingQuantityByProduct add up shipments and orders'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        Product gauze = product('p2', 'Gauze')
        Location depot = bin('Depot')
        ShipmentService shipmentService = Mock(ShipmentService)
        OrderService orderService = Mock(OrderService)
        service.applicationContext = Stub(ApplicationContext) {
            getBean('shipmentService') >> shipmentService
            getBean('orderService') >> orderService
        }

        when:
        Map outgoing = service.getOutgoingQuantityByProduct(depot, [aspirin, gauze])
        Map incoming = service.getIncomingQuantityByProduct(depot, [aspirin, gauze])

        then:
        1 * shipmentService.getOutgoingQuantityByProduct(depot, [aspirin, gauze]) >> [(aspirin): 4, (gauze): null]
        1 * orderService.getOutgoingQuantityByProduct(depot, [aspirin, gauze]) >> [(aspirin): 6]
        1 * shipmentService.getIncomingQuantityByProduct(depot, [aspirin, gauze]) >> [(gauze): 2]
        1 * orderService.getIncomingQuantityByProduct(depot, [aspirin, gauze]) >> [(gauze): 3, (aspirin): 1]
        outgoing == [(aspirin): 10, (gauze): 0]
        incoming == [(gauze): 5, (aspirin): 1]
    }

    void 'getQuantityToShip and getQuantityToReceive read the combined maps and default to zero'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        Location depot = bin('Depot')
        service.applicationContext = Stub(ApplicationContext) {
            getBean('shipmentService') >> Stub(ShipmentService) {
                getOutgoingQuantityByProduct(_, _) >> [(aspirin): 7]
                getIncomingQuantityByProduct(_, _) >> [:]
            }
            getBean('orderService') >> Stub(OrderService) {
                getOutgoingQuantityByProduct(_, _) >> [:]
                getIncomingQuantityByProduct(_, _) >> [:]
            }
        }

        expect:
        service.getQuantityToShip(depot, aspirin) == 7
        service.getQuantityToReceive(depot, aspirin) == 0
    }

    void 'validateForLocalTransfer rejects other transaction types and locations that do not manage inventory'() {
        given:
        Location unmanaged = bin('Supplier')
        TransactionType consumption = new TransactionType(transactionCode: TransactionCode.DEBIT)
        consumption.id = Constants.CONSUMPTION_TRANSACTION_TYPE_ID
        Transaction transaction = new Transaction(transactionType: consumption, source: unmanaged, destination: unmanaged)

        when:
        service.validateForLocalTransfer(transaction)

        then:
        transaction.errors.getFieldError('transactionType').code == 'transaction.localTransfer.invalidType'
        transaction.errors.getFieldError('source').code == 'transaction.localTransfer.invalidSource'
        transaction.errors.getFieldError('destination').code == 'transaction.localTransfer.invalidDestination'
    }

    void 'validateForLocalTransfer accepts a transfer out to a location that manages inventory'() {
        given:
        Location managed = new Location(name: 'Depot', locationType: new LocationType(
                supportedActivities: [ActivityCode.MANAGE_INVENTORY.id] as Set))
        TransactionType transferOut = new TransactionType(transactionCode: TransactionCode.DEBIT)
        transferOut.id = Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID
        Transaction transaction = new Transaction(transactionType: transferOut, destination: managed)

        when:
        service.validateForLocalTransfer(transaction)

        then:
        !transaction.hasErrors()
    }

    void 'getProductsByShipment lists the product of every shipment item'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        Product gauze = product('p2', 'Gauze')
        Shipment shipment = new Shipment()
        shipment.addToShipmentItems(new ShipmentItem(inventoryItem: item('i1', aspirin, 'A')))
        shipment.addToShipmentItems(new ShipmentItem(inventoryItem: item('i2', gauze, 'B')))

        expect:
        service.getProductsByShipment(shipment, 10, 0)*.name.sort() == ['Aspirin', 'Gauze']
        service.countProductsByShipment(shipment) == 2
        service.getProductsByShipment(null, 10, 0) == []
        service.countProductsByShipment(null) == 0
    }

    void 'getCurrentLocation fails without a current location that has an inventory'() {
        given:
        Location location = new Location(name: 'No inventory').save(validate: false)
        AuthService authService = new AuthService()
        authService.currentLocation = current ? location : null
        service.authService = authService

        when:
        service.getCurrentLocation()

        then:
        Exception exception = thrown()
        exception.message == 'Inventory not found'

        cleanup:
        authService.currentLocation = null

        where:
        current << [false, true]
    }

    void 'getCurrentLocation returns the current location when it has an inventory'() {
        given:
        Location depot = new Location(name: 'Depot')
        depot.inventory = new Inventory(warehouse: depot)
        depot.save(validate: false)
        AuthService authService = new AuthService()
        authService.currentLocation = depot
        service.authService = authService

        expect:
        service.getCurrentLocation().name == 'Depot'

        cleanup:
        authService.currentLocation = null
    }

    void 'exportBaselineQoH writes one row per product with a column per date'() {
        given:
        DataService dataService = Mock(DataService)
        service.dataService = dataService
        Category drugs = new Category(name: 'Drugs')
        Product aspirin = product('p1', 'Aspirin')
        aspirin.category = drugs
        aspirin.unitOfMeasure = 'EA'
        aspirin.manufacturer = 'Acme'
        Product gauze = product('p2', 'Gauze')
        Date firstOfMay = Date.parse('yyyy-MM-dd', '2026-05-01')
        List rows

        when:
        String csv = service.exportBaselineQoH([aspirin, gauze], [(firstOfMay): [(aspirin): 12]])

        then:
        1 * dataService.generateCsv(_) >> { arguments -> rows = arguments[0]; 'csv' }
        csv == 'csv'
        rows[0] == ['Product code': 'P1', Product: 'Aspirin', UOM: 'EA', 'Generic product': '', Category: 'Drugs',
                    Manufacturer: 'Acme', 'Manufacturer code': '', Vendor: '', 'Vendor code': '', '01-May-2026': '12']
        rows[1]['01-May-2026'] == ''
        rows[1].Category == null
    }

    void 'getExplodedCategories walks down the category tree and skips missing categories'() {
        given:
        Category root = new Category(name: 'Root')
        Category child = new Category(name: 'Child')
        Category grandchild = new Category(name: 'Grandchild')
        child.categories = [grandchild] as Set
        root.categories = [child] as Set

        expect:
        service.getExplodedCategories([root, null])*.name as Set == ['Root', 'Child', 'Grandchild'] as Set
        service.getExplodedCategories([root, null])[0].name == 'Root'
        service.getExplodedCategories(null) == []
    }

    void 'addInventory needs a location without an inventory'() {
        when:
        service.addInventory(location)

        then:
        RuntimeException exception = thrown()
        exception.message == message

        where:
        location                                             || message
        null                                                 || 'No warehouse specified.'
        new Location(name: 'Depot', inventory: new Inventory()) || 'An inventory is already associated with this warehouse.'
    }

    void 'getAvailableBinLocations merges the availability of the same lot and bin'() {
        given:
        Product aspirin = product('p1', 'Aspirin')
        InventoryItem lotA = item('i1', aspirin, 'A', new Date() + 30)
        InventoryItem lotB = item('i2', aspirin, 'B', null)
        Location binOne = bin('One')
        Location depot = bin('Depot')
        ProductAvailabilityService productAvailabilityService = Mock(ProductAvailabilityService)
        service.productAvailabilityService = productAvailabilityService

        when:
        List<AvailableItem> items = service.getAvailableBinLocations(depot, aspirin)

        then:
        1 * productAvailabilityService.getAvailableItems(depot, ['p1'], true, true) >> [
                new AvailableItem(inventoryItem: lotB, binLocation: binOne, quantityAvailable: 1, quantityOnHand: 1),
                new AvailableItem(inventoryItem: lotA, binLocation: binOne, quantityAvailable: 2, quantityOnHand: 3),
                new AvailableItem(inventoryItem: lotA, binLocation: binOne, quantityAvailable: 4, quantityOnHand: 4),
        ]
        items.collect { [it.inventoryItem.lotNumber, it.quantityAvailable, it.quantityOnHand] } == [['A', 6, 7], ['B', 1, 1]]
    }

    void 'getAvailableBinLocations returns nothing for no products without asking for availability'() {
        given:
        ProductAvailabilityService productAvailabilityService = Mock(ProductAvailabilityService)
        service.productAvailabilityService = productAvailabilityService

        when:
        List result = service.getAvailableBinLocations(bin('Depot'), [])

        then:
        result == []
        0 * productAvailabilityService._
    }

    void 'getCurrentStockAllLocations groups the warehouses with stock that the user can access by location group'() {
        given:
        LocationType depotType = new LocationType(locationTypeCode: LocationTypeCode.DEPOT, supportedActivities: [] as Set)
        LocationGroup north = new LocationGroup(name: 'North')
        north.id = 'north'
        LocationGroup south = new LocationGroup(name: 'South')
        south.id = 'south'
        Location depotA = new Location(name: 'A', locationType: depotType, locationGroup: south)
        Location depotB = new Location(name: 'B', locationType: depotType, locationGroup: south)
        Location depotC = new Location(name: 'C', locationType: depotType, locationGroup: north)
        Location noAccess = new Location(name: 'D', locationType: depotType, locationGroup: north)
        Location ward = new Location(name: 'Ward', locationType: new LocationType(locationTypeCode: LocationTypeCode.WARD,
                supportedActivities: [] as Set))
        Product aspirin = product('p1', 'Aspirin')
        aspirin.pricePerUnit = 2
        User user = Stub(User) {
            getEffectiveRoles(noAccess) >> []
            getEffectiveRoles(_) >> [new Role()]
        }
        service.locationService = Stub(LocationService) {
            getLoginLocationsWithEagerJoins() >> [depotA, depotB, depotC, noAccess, ward]
        }
        ProductAvailabilityService productAvailabilityService = Mock(ProductAvailabilityService)
        service.productAvailabilityService = productAvailabilityService

        when:
        List<Map> result = service.getCurrentStockAllLocations(aspirin, user)

        then:
        1 * productAvailabilityService.getQuantityOnHandByLocation(aspirin, [depotA, depotB, depotC]) >>
                [(depotA): 5, (depotB): 0, (depotC): 2]
        result.size() == 2
        result[0][north].totalQuantity == 2
        result[0][north].totalValue == 4
        result[0][north].locations*.location*.name == ['C']
        result[1][south].totalQuantity == 5
        result[1][south].totalValue == 10
        result[1][south].locations*.location*.name == ['A']
    }

    /**
     * Entries of one product, most recent last: a credit before the stock count (ignored), a product inventory
     * (stock count) of both lots, a debit of lot 1, an inventory (count) of lot 2 and a credit of lot 2.
     */
    private Map entries() {
        Product aspirin = product('p1', 'Aspirin')
        InventoryItem lot1 = item('i1', aspirin, 'L1', new Date() + 60)
        InventoryItem lot2 = item('i2', aspirin, 'L2', new Date() + 30)
        Location binA = bin('A')
        Location binB = bin('B')
        Date start = new Date() - 10
        Transaction count = new Transaction(transactionDate: start + 2, dateCreated: start + 2,
                transactionType: new TransactionType(transactionCode: TransactionCode.PRODUCT_INVENTORY))
        count.id = 't-count'
        List<TransactionEntry> list = [
                entry('e5', start + 5, TransactionCode.CREDIT, lot2, binB, 2),
                entry('e1', start + 1, TransactionCode.CREDIT, lot1, binA, 100),
                entry('e2a', start + 2, TransactionCode.PRODUCT_INVENTORY, lot1, binA, 10, count),
                entry('e2b', start + 2, TransactionCode.PRODUCT_INVENTORY, lot2, binB, 4, count),
                entry('e3', start + 3, TransactionCode.DEBIT, lot1, binA, 3),
                entry('e4', start + 4, TransactionCode.INVENTORY, lot2, binB, 6),
        ]
        return [entries: list, aspirin: aspirin, lot1: lot1, lot2: lot2, binA: binA, binB: binB]
    }

    void 'the quantity maps count back from the most recent entries to the last stock count'() {
        given:
        Map data = entries()

        expect:
        service.getQuantityByProductAndInventoryItemMap(data.entries) == [(data.aspirin): [(data.lot1): 7, (data.lot2): 8]]
        service.getQuantityByProductAndInventoryItemMap(data.entries, true) ==
                [(data.aspirin): [(data.lot1): [(data.binA): 7], (data.lot2): [(data.binB): 8]]]
        service.getQuantityByProductMap(data.entries) == [(data.aspirin): 15]
        service.getQuantityByInventoryItemMap(data.entries) == [(data.lot1): 7, (data.lot2): 8]
        service.getQuantityByProductAndInventoryItemMap([]) == [:]
    }

    void 'the bin views of the entries list each lot and bin, sorted by expiration date'() {
        given:
        Map data = entries()
        List<TransactionEntry> withEmptyBin = data.entries + [entry('e6', new Date() - 1, TransactionCode.DEBIT,
                data.lot1, data.binA, 7)]

        when:
        List<BinLocationItem> bins = service.getQuantityByBinLocation(data.entries)
        List<AvailableItem> availableItems = service.getQuantityAsAvailableItems(data.entries)

        then:
        bins.collect { [it.inventoryItem.lotNumber, it.binLocation.name, it.quantity, it.status] } ==
                [['L2', 'B', 8, 'inStock'], ['L1', 'A', 7, 'inStock']]
        bins[0].value == 'Bin: B, Lot: L2, Qty: 8'
        bins[0].product == data.aspirin
        !bins[0].isOnHold
        availableItems.collect { [it.inventoryItem.lotNumber, it.binLocation.name, it.quantityOnHand] } as Set ==
                [['L1', 'A', 7], ['L2', 'B', 8]] as Set

        and: 'a bin that was emptied only shows when out of stock bins are asked for'
        service.getQuantityByBinLocation(withEmptyBin)*.inventoryItem*.lotNumber == ['L2']
        service.getQuantityByBinLocation(withEmptyBin, true).collect { [it.inventoryItem.lotNumber, it.status] } ==
                [['L2', 'inStock'], ['L1', 'outOfStock']]
    }

    void 'getAvailableItems joins the quantity on hand of the entries with the quantity available per bin'() {
        given:
        Map data = entries()
        Map quantityAvailable = [(data.aspirin): [(data.lot1): [(data.binA.id): 5], (data.lot2): [(data.binB.id): -2]]]

        when:
        List<AvailableItem> items = service.getAvailableItems(data.entries, quantityAvailable)

        then:
        items.collect { [it.inventoryItem.lotNumber, it.quantityOnHand, it.quantityAvailable] } == [['L2', 8, 0], ['L1', 7, 5]]
    }

    @PendingFeature(reason = 'OBAF calls getQuantityOnHandByInventoryItem/ByProduct, which are not on InventoryService')
    void 'getInventorySampling and getGenericProductSummary read the quantities on hand of the location'() {
        given:
        Location depot = new Location(name: 'Depot')
        Product aspirin = product('p1', 'Aspirin')
        Product gauze = product('p2', 'Gauze')
        InventoryItem lot = item('i1', aspirin, 'L1')
        service.productAvailabilityService = Stub(ProductAvailabilityService) {
            getQuantityOnHandByInventoryItem(depot) >> [(lot): 5]
            getQuantityOnHandByProduct(depot) >> [(aspirin): 5, (gauze): -2]
        }

        when:
        List sample = service.getInventorySampling(depot, 3)
        Map summary = service.getGenericProductSummary(depot)

        then:
        sample == [lot]
        summary.values().flatten().collect { [it.name, it.currentQuantity] } as Set == [['Aspirin', 5], ['Gauze', 0]] as Set
    }
}
