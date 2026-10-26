package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import java.sql.Connection
import java.sql.SQLException
import java.sql.PreparedStatement
import java.text.SimpleDateFormat
import javax.sql.DataSource
import spock.lang.Specification

import grails.persistence.support.PersistenceContextInterceptor
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.Tag
import org.pih.warehouse.jobs.GparsService
import org.pih.warehouse.product.Product

/**
 * Unit tests for the logic around the inventory snapshot table: the SQL each bin location row becomes, which
 * collaborators the populate/delete entry points call and with what, and how query results are shaped.
 * The HQL and SQL themselves are not run here: they need a real database.
 */
class InventorySnapshotServiceSpec extends Specification implements DataTest {

    InventorySnapshotService service
    TimeZone savedTimeZone

    /** Calls captured by the metaClass stubs of executeUpdate / executeQuery. */
    List<Map> updates = []
    List<Map> queries = []

    void setupSpec() {
        mockDomains(Location, Product, InventoryItem, InventorySnapshot, Inventory, InventoryLevel, Tag)
    }

    void setup() {
        savedTimeZone = TimeZone.default
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        service = Spy(InventorySnapshotService)
    }

    void cleanup() {
        TimeZone.setDefault(savedTimeZone)
        GroovySystem.metaClassRegistry.removeMetaClass(InventorySnapshot)
        GroovySystem.metaClassRegistry.removeMetaClass(InventoryLevel)
    }

    private void captureExecuteUpdate(int result = 0) {
        InventorySnapshot.metaClass.static.executeUpdate = { String statement, Map params ->
            updates << [statement: statement, params: params]
            return result
        }
    }

    private void stubExecuteQuery(List rows) {
        InventorySnapshot.metaClass.static.executeQuery = { String statement, Map params ->
            queries << [statement: statement, params: params]
            return rows
        }
        InventorySnapshot.metaClass.static.executeQuery = { String statement ->
            queries << [statement: statement, params: null]
            return rows
        }
    }

    private static Date date(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.parse(value)
    }

    private static Product product(String id, String code) {
        Product product = new Product(productCode: code, name: "Product ${code}")
        product.id = id
        return product
    }

    private static Location location(String id, String name) {
        Location location = new Location(name: name)
        location.id = id
        return location
    }

    private static InventoryItem inventoryItem(String id, String lotNumber, Date expirationDate) {
        InventoryItem inventoryItem = new InventoryItem(lotNumber: lotNumber, expirationDate: expirationDate)
        inventoryItem.id = id
        return inventoryItem
    }

    // generateInsertInventorySnapshotStatement

    void 'generateInsertInventorySnapshotStatement should write every column of a fully populated bin location'() {
        given:
        Location depot = location("loc-1", "Depot")
        Map entry = [
                product      : product("prod-1", "AB12"),
                inventoryItem: inventoryItem("ii-1", "LOT-7", date("2027-05-31 00:00:00")),
                binLocation  : location("bin-1", "Shelf A"),
                quantity     : 42,
        ]

        when:
        String statement = service.generateInsertInventorySnapshotStatement(depot, "2026-03-15 00:00:00",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), entry)

        then:
        statement.startsWith("INSERT INTO inventory_snapshot (id, version, date, location_id, product_id, product_code, " +
                "inventory_item_id, lot_number, expiration_date, bin_location_id, bin_location_name, " +
                "quantity_on_hand, date_created, last_updated) values ('")
        statement.contains(", 0, '2026-03-15 00:00:00', 'loc-1', 'prod-1', 'AB12', 'ii-1', 'LOT-7', " +
                "'2027-05-31 00:00:00', 'bin-1', 'Shelf A', 42, now(), now()) ")
        statement.endsWith("ON DUPLICATE KEY UPDATE quantity_on_hand=42, version=version+1, last_updated=now()")
    }

    void 'generateInsertInventorySnapshotStatement should fall back to DEFAULT lot and bin names and NULL ids when the entry has no item or bin'() {
        given:
        Map entry = [product: product("prod-1", "AB12"), quantity: 0]

        when:
        String statement = service.generateInsertInventorySnapshotStatement(location("loc-1", "Depot"),
                "2026-03-15 00:00:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), entry)

        then:
        statement.contains("'prod-1', 'AB12', NULL, 'DEFAULT', NULL, NULL, 'DEFAULT', 0, now(), now())")
    }

    void 'generateInsertInventorySnapshotStatement should treat a blank lot number and bin name as DEFAULT'() {
        given:
        Map entry = [
                product      : product("prod-1", "AB12"),
                inventoryItem: inventoryItem("ii-1", "", null),
                binLocation  : location("bin-1", ""),
                quantity     : 3,
        ]

        when:
        String statement = service.generateInsertInventorySnapshotStatement(location("loc-1", "Depot"),
                "2026-03-15 00:00:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), entry)

        then:
        statement.contains("'ii-1', 'DEFAULT', NULL, 'bin-1', 'DEFAULT', 3,")
    }

    void 'generateInsertInventorySnapshotStatement should escape single quotes in codes, lots and bin names'() {
        given:
        Map entry = [
                product      : product("prod-1", "O'NEIL"),
                inventoryItem: inventoryItem("ii-1", "L'1", null),
                binLocation  : location("bin-1", "Bay 'B'"),
                quantity     : 1,
        ]

        when:
        String statement = service.generateInsertInventorySnapshotStatement(location("loc-1", "Depot"),
                "2026-03-15 00:00:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), entry)

        then:
        statement.contains("'prod-1', 'O''NEIL', 'ii-1', 'L''1', NULL, 'bin-1', 'Bay ''B''', 1,")
    }

    void 'generateInsertInventorySnapshotStatement should give every row a new id'() {
        given:
        Map entry = [product: product("prod-1", "AB12"), quantity: 1]
        def format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        when:
        String first = service.generateInsertInventorySnapshotStatement(location("loc-1", "Depot"), "2026-03-15 00:00:00", format, entry)
        String second = service.generateInsertInventorySnapshotStatement(location("loc-1", "Depot"), "2026-03-15 00:00:00", format, entry)

        then:
        first != second
        first.substring(first.indexOf("values")) =~ /^values \('[0-9a-f-]{36}', 0,/
    }

    // saveInventorySnapshots

    void 'saveInventorySnapshots should add one statement per bin location, at midnight of the given day, and close the connection'() {
        given:
        PreparedStatement statement = Mock(PreparedStatement)
        Connection connection = Mock(Connection)
        DataSource dataSource = Stub(DataSource) { getConnection() >> connection }
        service.dataSource = dataSource
        List<String> batched = []

        and:
        Date snapshotDate = date("2026-03-15 17:45:12")
        Location depot = location("loc-1", "Depot")
        List binLocations = [
                [product: product("prod-1", "AB12"), quantity: 5],
                [product: product("prod-2", "CD34"), quantity: 7],
        ]

        when:
        service.saveInventorySnapshots(snapshotDate, depot, binLocations, false)

        then:
        _ * connection.createStatement(*_) >> statement
        _ * connection.prepareStatement(*_) >> statement
        2 * statement.addBatch(_ as String) >> { String sql -> batched << sql }
        (1.._) * statement.executeBatch() >> ([] as int[])
        1 * connection.close()
        0 * service.deleteInventorySnapshots(*_)

        and:
        batched.size() == 2
        batched.every { it.contains("'2026-03-15 00:00:00', 'loc-1'") }
        batched[0].contains("'prod-1', 'AB12'")
        batched[1].contains("'prod-2', 'CD34'")

        and: 'the caller date is cleared to midnight in place'
        snapshotDate == date("2026-03-15 00:00:00")
    }

    void 'saveInventorySnapshots should first delete the existing snapshots of the location and product when forced to refresh'() {
        given:
        PreparedStatement statement = Stub(PreparedStatement) { executeBatch() >> ([] as int[]) }
        Connection connection = Stub(Connection) {
            createStatement(*_) >> statement
            prepareStatement(*_) >> statement
        }
        service.dataSource = Stub(DataSource) { getConnection() >> connection }
        Location depot = location("loc-1", "Depot")
        Product item = product("prod-1", "AB12")

        when:
        service.saveInventorySnapshots(date("2026-03-15 00:00:00"), depot, item, [], true)

        then:
        1 * service.deleteInventorySnapshots(depot, item) >> null
    }

    void 'saveInventorySnapshots should rethrow a database failure'() {
        given:
        Connection connection = Mock(Connection)
        service.dataSource = Stub(DataSource) { getConnection() >> connection }

        when:
        service.saveInventorySnapshots(date("2026-03-15 00:00:00"), location("loc-1", "Depot"),
                [[product: product("prod-1", "AB12"), quantity: 5]], false)

        then:
        _ * connection.createStatement(*_) >> { throw new SQLException("table is locked") }
        _ * connection.prepareStatement(*_) >> { throw new SQLException("table is locked") }
        SQLException e = thrown()
        e.message == "table is locked"
    }

    // populateInventorySnapshots

    void 'populateInventorySnapshots for a location and date should save the bin locations as of that date'() {
        given:
        service.productAvailabilityService = Mock(ProductAvailabilityService)
        Location depot = location("loc-1", "Depot")
        Date snapshotDate = date("2026-03-15 00:00:00")
        List binLocations = [[product: product("prod-1", "AB12"), quantity: 5]]

        when:
        service.populateInventorySnapshots(snapshotDate, depot, true)

        then:
        1 * service.productAvailabilityService.calculateBinLocationsAsOfDate(depot, snapshotDate) >> binLocations
        1 * service.saveInventorySnapshots(snapshotDate, depot, binLocations, true) >> null
    }

    void 'populateInventorySnapshots for a location should use the most recent snapshot date without forcing a refresh'() {
        given:
        Location depot = location("loc-1", "Depot")
        Date mostRecent = date("2026-03-14 00:00:00")
        service.getMostRecentInventorySnapshotDate() >> mostRecent

        when:
        service.populateInventorySnapshots(depot)

        then:
        1 * service.populateInventorySnapshots(mostRecent, depot, false) >> null
    }

    void 'populateInventorySnapshots for a location should use tomorrow when there are no snapshots yet'() {
        given:
        Location depot = location("loc-1", "Depot")
        service.getMostRecentInventorySnapshotDate() >> null
        Date usedDate = null
        long before = System.currentTimeMillis()

        when:
        service.populateInventorySnapshots(depot)

        then:
        1 * service.populateInventorySnapshots(_ as Date, depot, false) >> { Date d, Location l, Boolean f -> usedDate = d; null }
        usedDate.time >= before + 24 * 60 * 60 * 1000L
        usedDate.time <= System.currentTimeMillis() + 24 * 60 * 60 * 1000L
    }

    void 'populateInventorySnapshots for a location and product should save one snapshot per transaction date'() {
        given:
        service.productAvailabilityService = Mock(ProductAvailabilityService)
        Location depot = location("loc-1", "Depot")
        Product item = product("prod-1", "AB12")
        Date first = date("2026-03-10 00:00:00")
        Date second = date("2026-03-12 00:00:00")
        List binLocations = [[product: item, quantity: 2]]
        service.getTransactionDates(depot, item) >> [second, first]

        when:
        service.populateInventorySnapshots(depot, item)

        then:
        2 * service.productAvailabilityService.calculateBinLocations(depot, item) >> binLocations
        1 * service.saveInventorySnapshots(second, depot, binLocations, false) >> null
        1 * service.saveInventorySnapshots(first, depot, binLocations, false) >> null
    }

    void 'populateInventorySnapshots for a location and product should save nothing when the product has no transactions there'() {
        given:
        service.productAvailabilityService = Mock(ProductAvailabilityService)
        Location depot = location("loc-1", "Depot")
        Product item = product("prod-1", "AB12")
        service.getTransactionDates(depot, item) >> []

        when:
        service.populateInventorySnapshots(depot, item)

        then:
        0 * service.productAvailabilityService.calculateBinLocations(*_)
        0 * service.saveInventorySnapshots(*_)
    }

    void 'populateInventorySnapshots for a date should read every depot in parallel and then save each one'() {
        given:
        Location first = new Location(name: "Depot 1").save(validate: false, flush: true)
        Location second = new Location(name: "Depot 2").save(validate: false, flush: true)
        Date snapshotDate = date("2026-03-15 00:00:00")
        List firstBins = [[product: product("prod-1", "AB12"), quantity: 1]]
        List secondBins = [[product: product("prod-2", "CD34"), quantity: 2]]

        and: 'a plain instance: the depots are read on pool threads, which must not wait on a spy'
        RecordingInventorySnapshotService recording = new RecordingInventorySnapshotService()
        recording.gparsService = new GparsService()
        recording.locationService = Stub(LocationService) { getDepots() >> [first, second] }
        recording.persistenceInterceptor = Mock(PersistenceContextInterceptor)
        Map<String, List> binsByLocationId = [(first.id): firstBins, (second.id): secondBins]
        recording.productAvailabilityService = Stub(ProductAvailabilityService) {
            calculateBinLocationsAsOfDate(_, snapshotDate) >> { Location l, Date d -> binsByLocationId[l?.id] }
        }

        when:
        recording.populateInventorySnapshots(snapshotDate)

        then:
        2 * recording.persistenceInterceptor.init()
        2 * recording.persistenceInterceptor.flush()
        2 * recording.persistenceInterceptor.destroy()

        and:
        recording.saved*.date == [snapshotDate, snapshotDate]
        recording.saved*.forceRefresh == [false, false]
        recording.saved.collectEntries { [(it.location.id): it.binLocations] } == [(first.id): firstBins, (second.id): secondBins]
    }

    // deleteInventorySnapshots

    void 'deleteInventorySnapshots for a date should delete that date across all locations and products'() {
        given:
        captureExecuteUpdate(3)
        Date snapshotDate = date("2026-03-15 00:00:00")

        when:
        service.deleteInventorySnapshots(snapshotDate)

        then:
        updates.size() == 1
        updates[0].statement == "delete from InventorySnapshot snapshot where snapshot.date = :date"
        updates[0].params == [date: snapshotDate]
    }

    void 'deleteInventorySnapshots should narrow the delete by location and product when given'() {
        given:
        captureExecuteUpdate()
        Date snapshotDate = date("2026-03-15 00:00:00")
        Location depot = location("loc-1", "Depot")
        Product item = product("prod-1", "AB12")

        when:
        service.deleteInventorySnapshots(snapshotDate, depot)
        service.deleteInventorySnapshots(snapshotDate, depot, item)

        then:
        updates*.statement == [
                "delete from InventorySnapshot snapshot where snapshot.date = :date and snapshot.location = :location",
                "delete from InventorySnapshot snapshot where snapshot.date = :date and snapshot.location = :location and snapshot.product = :product",
        ]
        updates*.params == [
                [date: snapshotDate, location: depot],
                [date: snapshotDate, location: depot, product: item],
        ]
    }

    void 'deleteInventorySnapshots for a location and product should target the most recent snapshot date'() {
        given:
        captureExecuteUpdate()
        Date mostRecent = date("2026-03-14 00:00:00")
        service.getMostRecentInventorySnapshotDate() >> mostRecent
        Location depot = location("loc-1", "Depot")
        Product item = product("prod-1", "AB12")

        when:
        service.deleteInventorySnapshots(depot)
        service.deleteInventorySnapshots(depot, item)

        then:
        updates*.params == [
                [date: mostRecent, location: depot],
                [date: mostRecent, location: depot, product: item],
        ]
    }

    // updateInventorySnapshots

    void 'updateInventorySnapshots for an inventory item should rewrite the lot number of rows that differ'() {
        given:
        captureExecuteUpdate(2)

        when:
        service.updateInventorySnapshots(inventoryItem("ii-1", lotNumber, null))

        then:
        updates.size() == 1
        updates[0].statement == "update InventorySnapshot a set a.lotNumber=:lotNumber " +
                "where a.inventoryItem.id = :inventoryItemId and a.lotNumber != :lotNumber"
        updates[0].params == [inventoryItemId: "ii-1", lotNumber: expectedLotNumber]

        where:
        lotNumber || expectedLotNumber
        "LOT-9"   || "LOT-9"
        null      || "DEFAULT"
        ""        || "DEFAULT"
    }

    void 'updateInventorySnapshots for a bin location should rewrite the bin location name of rows that differ'() {
        given:
        captureExecuteUpdate()

        when:
        service.updateInventorySnapshots(location("bin-1", name))

        then:
        updates.size() == 1
        updates[0].statement == "update InventorySnapshot a set a.binLocationName = :binLocationName " +
                "where a.binLocation.id = :binLocationId and a.binLocationName != :binLocationName"
        updates[0].params == [binLocationId: "bin-1", binLocationName: expectedName]

        where:
        name      || expectedName
        "Shelf A" || "Shelf A"
        null      || "DEFAULT"
    }

    void 'updateInventorySnapshots for a product should rewrite the product code of rows that differ'() {
        given:
        captureExecuteUpdate()

        when:
        service.updateInventorySnapshots(product("prod-1", "AB12"))

        then:
        updates.size() == 1
        updates[0].statement == "update InventorySnapshot a set a.productCode = :productCode " +
                "where a.product.id = :productId and a.productCode != :productCode"
        updates[0].params == [productId: "prod-1", productCode: "AB12"]
    }

    void 'updateInventorySnapshots for a product should do nothing when the product has no id or no code'() {
        given:
        captureExecuteUpdate()

        when:
        service.updateInventorySnapshots(target as Product)

        then:
        updates.empty

        where:
        target << [product(null, "AB12"), product("prod-1", null), product("prod-1", "")]
    }

    // queries

    void 'findInventorySnapshotByDateAndLocation should return nothing without querying when the date or location is missing'() {
        given:
        stubExecuteQuery([])

        expect:
        service.findInventorySnapshotByDateAndLocation(snapshotDate, depot) == []
        queries.empty

        where:
        snapshotDate                 | depot
        null                         | location("loc-1", "Depot")
        date("2026-03-15 00:00:00")  | null
    }

    void 'findInventorySnapshotByDateAndLocation should combine quantities with the inventory levels of the location'() {
        given:
        Inventory inventory = new Inventory()
        Location depot = location("loc-1", "Depot")
        depot.inventory = inventory
        Date snapshotDate = date("2026-03-15 00:00:00")

        and:
        Product stocked = product("prod-1", "AB12")
        stocked.unitOfMeasure = "BX"
        stocked.tags = [new Tag(tag: "vital"), new Tag(tag: "cold")] as Set
        Product unstocked = product("prod-2", "CD34")
        InventoryLevel level = new InventoryLevel(product: stocked, minQuantity: 5, maxQuantity: 50, reorderQuantity: 10,
                status: InventoryStatus.SUPPORTED)

        and:
        stubExecuteQuery([
                [snapshotDate, "Depot", stocked, "Medicines", 12],
                [snapshotDate, "Depot", unstocked, "Supplies", 0],
        ])
        InventoryLevel.metaClass.static.findAllByInventory = { Inventory i -> i.is(inventory) ? [level] : [] }

        when:
        List data = service.findInventorySnapshotByDateAndLocation(snapshotDate, depot)

        then:
        queries.size() == 1
        queries[0].params == [location: depot, date: snapshotDate]

        and:
        data == [
                [date: snapshotDate, location: "Depot", category: "Medicines", productCode: "AB12", product: "Product AB12",
                 productGroup: null, tags: "cold,vital", status: InventoryStatus.SUPPORTED, quantityOnHand: 12,
                 minQuantity: 5, maxQuantity: 50, reorderQuantity: 10, unitOfMeasure: "BX"],
                [date: snapshotDate, location: "Depot", category: "Supplies", productCode: "CD34", product: "Product CD34",
                 productGroup: null, tags: "", status: null, quantityOnHand: 0,
                 minQuantity: 0, maxQuantity: 0, reorderQuantity: 0, unitOfMeasure: "EA"],
        ]
    }

    void 'getQuantityOnHandByProduct should map each product to its summed quantity'() {
        given:
        Product first = product("prod-1", "AB12")
        Product second = product("prod-2", "CD34")
        stubExecuteQuery([[first, 12], [second, 0]])
        Location depot = location("loc-1", "Depot")
        Date snapshotDate = date("2026-03-15 00:00:00")

        when:
        Map result = service.getQuantityOnHandByProduct(depot, snapshotDate)

        then:
        result == [(first): 12, (second): 0]
        queries[0].params == [location: depot, date: snapshotDate]
    }

    void 'getQuantityOnHandByProduct should return an empty map without querying when the date or location is missing'() {
        given:
        stubExecuteQuery([])

        expect:
        service.getQuantityOnHandByProduct(location("loc-1", "Depot"), null) == [:]
        service.getQuantityOnHandByProduct(null, date("2026-03-15 00:00:00")) == [:]
        queries.empty
    }

    void 'getQuantityOnHandByBinLocation should flag rows with a positive quantity as in stock and the rest as out of stock'() {
        given:
        Product item = product("prod-1", "AB12")
        InventoryItem lot = inventoryItem("ii-1", "LOT-1", null)
        Location bin = location("bin-1", "Shelf A")
        stubExecuteQuery([[item, lot, bin, 4], [item, lot, null, 0], [item, null, bin, -2]])
        Location depot = location("loc-1", "Depot")
        Date snapshotDate = date("2026-03-15 00:00:00")

        when:
        List data = service.getQuantityOnHandByBinLocation(depot, snapshotDate, [item])

        then:
        queries[0].params == [location: depot, products: [item], date: snapshotDate]
        data == [
                [status: "inStock", product: item, inventoryItem: lot, binLocation: bin, quantity: 4],
                [status: "outOfStock", product: item, inventoryItem: lot, binLocation: null, quantity: 0],
                [status: "outOfStock", product: item, inventoryItem: null, binLocation: bin, quantity: -2],
        ]
    }

    void 'getQuantityOnHandByBinLocation should return nothing without querying when the date or location is missing'() {
        given:
        stubExecuteQuery([])

        expect:
        service.getQuantityOnHandByBinLocation(null, date("2026-03-15 00:00:00"), []) == []
        service.getQuantityOnHandByBinLocation(location("loc-1", "Depot"), null, []) == []
        queries.empty
    }

    void 'getMostRecentInventorySnapshotDate and getLastUpdatedInventorySnapshotDate should return the single aggregate value'() {
        given:
        Date latest = date("2026-03-15 00:00:00")
        stubExecuteQuery([latest])

        expect:
        service.getMostRecentInventorySnapshotDate() == latest
        service.getLastUpdatedInventorySnapshotDate() == latest
        queries*.statement == ['select max(date) from InventorySnapshot', 'select max(lastUpdated) from InventorySnapshot']
    }
}

/** Records the snapshots it would save instead of writing them. */
class RecordingInventorySnapshotService extends InventorySnapshotService {

    // The parent's logger is private to it; its closures look the property up on this class
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InventorySnapshotService)

    List<Map> saved = Collections.synchronizedList([])

    @Override
    def saveInventorySnapshots(Date date, Location location, List binLocations, Boolean forceRefresh) {
        saved << [date: date, location: location, binLocations: binLocations, forceRefresh: forceRefresh]
        return null
    }
}
