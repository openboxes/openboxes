package org.pih.warehouse.inventory.spec

import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

/**
 * Tests ProductAvailabilityService.getQuantityOnHandAtDate, which calculates historical stock in the database and
 * must give the same result as the in-memory replay in getAvailableItemsAtDate (except for the one documented case).
 *
 * Transactions are inserted with SQL instead of GORM so that we control date_created (GORM always sets it to now)
 * and so that saving them doesn't trigger a product availability refresh. Each test runs in its own transaction
 * that is rolled back afterwards, and uses its own facility, so tests can't see each other's data.
 */
@Rollback
class ProductAvailabilityServiceQuantityOnHandAtDateSpec extends IntegrationSpec {

    private static final String CREDIT = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
    private static final String DEBIT = Constants.CONSUMPTION_TRANSACTION_TYPE_ID
    private static final String INVENTORY = Constants.INVENTORY_TRANSACTION_TYPE_ID
    private static final String PRODUCT_INVENTORY = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
    private static final String INVENTORY_BASELINE = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Autowired
    SessionFactory sessionFactory

    Location facility
    Location binA
    Location binB

    Product productA
    InventoryItem itemA1
    InventoryItem itemA2

    Product productB
    InventoryItem itemB1

    private void setupFacilityAndProducts() {
        facility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        binA = new LocationTestBuilder().name("A").asBinLocation(facility).build(true)
        binB = new LocationTestBuilder().name("B").asBinLocation(facility).build(true)

        Category category = new CategoryTestBuilder().build(true)

        productA = new ProductTestBuilder().randomizedName().category(category).build(true)
        itemA1 = new InventoryItem(product: productA, lotNumber: "A1").save(failOnError: true)
        itemA2 = new InventoryItem(product: productA, lotNumber: "A2").save(failOnError: true)

        productB = new ProductTestBuilder().randomizedName().category(category).build(true)
        itemB1 = new InventoryItem(product: productB, lotNumber: "B1").save(failOnError: true)

        sessionFactory.currentSession.flush()
    }

    void 'returns nothing when there is no transaction history'() {
        given:
        setupFacilityAndProducts()

        expect:
        quantityAt(day(10)) == [:]
    }

    void 'sums credits and subtracts debits per inventory item and bin'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA1, null, 5], [itemA2, binA, 7]])
        insertTransaction(DEBIT, day(2), [[itemA1, binA, 3], [itemA2, binA, 7]])
        insertTransaction(DEBIT, day(3), [[itemA1, null, 8]])

        expect: 'zero quantities are left out, negative ones are kept'
        quantityAt(day(10)) == ["A1|A": 7, "A1|-": -3]
    }

    void 'only counts transactions before the given date'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10]])
        insertTransaction(CREDIT, day(2), [[itemA1, binA, 5]])
        insertTransaction(DEBIT, day(3), [[itemA1, binA, 1]])

        expect:
        quantityAt(day(1)) == [:]
        quantityAt(day(2)) == ["A1|A": 10]
        quantityAt(day(2, 1)) == ["A1|A": 15]
        quantityAt(day(3)) == ["A1|A": 15]
        quantityAt(day(4)) == ["A1|A": 14]
    }

    void 'a product inventory replaces the stock of every inventory item of the product'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA2, binB, 20], [itemB1, binA, 30]])
        insertTransaction(PRODUCT_INVENTORY, day(2), [[itemA1, binA, 4], [itemA1, binB, 1]])
        insertTransaction(DEBIT, day(3), [[itemA1, binA, 1]])

        expect: 'before the count, the full history counts'
        quantityAt(day(2)) == ["A1|A": 10, "A2|B": 20, "B1|A": 30]

        and: 'after the count, items that were not counted are gone, and other products are not affected'
        quantityAt(day(3)) == ["A1|A": 4, "A1|B": 1, "B1|A": 30]
        quantityAt(day(4)) == ["A1|A": 3, "A1|B": 1, "B1|A": 30]
    }

    void 'an inventory baseline transaction behaves like a product inventory'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA2, binA, 20]])
        insertTransaction(INVENTORY_BASELINE, day(2), [[itemA1, binA, 4]])

        expect:
        quantityAt(day(3)) == ["A1|A": 4]
    }

    void 'an inventory transaction replaces the stock of only that inventory item'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA1, binB, 3], [itemA2, binA, 20]])
        insertTransaction(INVENTORY, day(2), [[itemA1, binA, 6]])
        insertTransaction(CREDIT, day(3), [[itemA1, binB, 2]])

        expect: 'item A1 restarts from the count, item A2 keeps its full history'
        quantityAt(day(4)) == ["A1|A": 6, "A1|B": 2, "A2|A": 20]
    }

    void 'a product inventory resets every inventory item, even when its counted items are counted again later'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA2, binA, 20]])
        insertTransaction(PRODUCT_INVENTORY, day(2), [[itemA2, binA, 5]])
        insertTransaction(INVENTORY, day(3), [[itemA2, binB, 7]])

        expect: 'item A1 was reset to zero by the product inventory, and item A2 restarts from its own count'
        quantityAt(day(4)) == ["A2|B": 7]

        and: 'note that the in-memory replay disagrees: it skips the product inventory because its only entry is ' +
                'older than the count of A2, so it never resets A1'
        replayQuantityAt(day(4)) == ["A1|A": 10, "A2|B": 7]
    }

    void 'only the most recent count is used'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(PRODUCT_INVENTORY, day(1), [[itemA1, binA, 100], [itemA2, binA, 50]])
        insertTransaction(INVENTORY, day(2), [[itemA1, binA, 80]])
        insertTransaction(INVENTORY, day(3), [[itemA1, binA, 70]])
        insertTransaction(PRODUCT_INVENTORY, day(4), [[itemA2, binB, 5]])
        insertTransaction(INVENTORY, day(5), [[itemA2, binB, 6]])

        expect:
        quantityAt(day(2)) == ["A1|A": 100, "A2|A": 50]
        quantityAt(day(4)) == ["A1|A": 70, "A2|A": 50]
        quantityAt(day(5)) == ["A2|B": 5]
        quantityAt(day(6)) == ["A2|B": 6]
    }

    void 'transactions are ordered by transaction date, then by date created'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10]], day(1))
        insertTransaction(PRODUCT_INVENTORY, day(3), [[itemA1, binA, 50]], day(3))

        and: 'a credit back-dated to before the count, but entered after it, is overwritten by the count'
        insertTransaction(CREDIT, day(2), [[itemA1, binA, 1000]], day(4))

        and: 'a credit with the same transaction date as the count is only overwritten if it was entered before it'
        insertTransaction(CREDIT, day(3), [[itemA1, binA, 100]], day(2))
        insertTransaction(CREDIT, day(3), [[itemA1, binA, 7]], day(5))

        expect:
        quantityAt(day(10)) == ["A1|A": 57]
    }

    void 'credits and debits that tie exactly with a count are applied after it'() {
        given: 'a baseline and its adjustments created together, with the same transaction date and date created'
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA2, binA, 3]])
        insertTransaction(INVENTORY_BASELINE, day(2), [[itemA1, binA, 12]], day(2))
        insertTransaction(CREDIT, day(2), [[itemA1, binA, 5]], day(2))
        insertTransaction(DEBIT, day(2), [[itemA1, binA, 2]], day(2))

        and: 'the same for an inventory transaction on a single item'
        insertTransaction(INVENTORY, day(3), [[itemB1, binB, 4]], day(3))
        insertTransaction(CREDIT, day(3), [[itemB1, binB, 1]], day(3))

        expect:
        quantityAt(day(4)) == ["A1|A": 15, "B1|B": 5]
    }

    void 'when two counts tie exactly, only one of them is used'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(PRODUCT_INVENTORY, day(1), [[itemA1, binA, 10]], day(1))
        insertTransaction(PRODUCT_INVENTORY, day(1), [[itemA1, binA, 20]], day(1))
        insertTransaction(INVENTORY, day(2), [[itemB1, binA, 30]], day(2))
        insertTransaction(INVENTORY, day(2), [[itemB1, binA, 40]], day(2))

        when:
        Map<String, Integer> quantity = quantityAt(day(3))

        then: 'which one is arbitrary, but they must not be added together'
        quantity["A1|A"] in [10, 20]
        quantity["B1|A"] in [30, 40]
    }

    void 'fetches every product when no products are given, but only at the given facility'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemB1, null, 3]])

        and: 'stock of the same item at another facility'
        Location otherFacility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        sessionFactory.currentSession.flush()
        insertTransaction(CREDIT, day(1), [[itemA1, null, 99]], day(1), otherFacility)

        expect:
        quantityAt(day(2), null) == ["A1|A": 10, "B1|-": 3]
        quantityAt(day(2), []) == ["A1|A": 10, "B1|-": 3]
        quantityAt(day(2), [productB]) == ["B1|-": 3]
    }

    void 'getQuantityOnHandAtDateAsMap uses the transaction history for past dates'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10]])
        insertTransaction(DEBIT, day(2), [[itemA1, binA, 4]])

        when:
        List<AvailableItem> availableItems = productAvailabilityService.getQuantityOnHandAtDateAsMap(
                facility, [productA], day(2)).values().toList()

        then:
        availableItems.size() == 1
        availableItems[0].inventoryItem.id == itemA1.id
        availableItems[0].binLocation.id == binA.id
        availableItems[0].quantityOnHand == 10
    }

    void 'getAvailableItemsAtDate still uses the in-memory replay'() {
        given: 'a history where the replay and the database calculation disagree'
        setupFacilityAndProducts()
        insertTransaction(CREDIT, day(1), [[itemA1, binA, 10], [itemA2, binA, 20]])
        insertTransaction(PRODUCT_INVENTORY, day(2), [[itemA2, binA, 5]])
        insertTransaction(INVENTORY, day(3), [[itemA2, binB, 7]])

        expect:
        toMap(productAvailabilityService.getAvailableItemsAtDate(facility, [productA], day(4))) ==
                ["A1|A": 10, "A2|B": 7]
        toMap(productAvailabilityService.getQuantityOnHandAtDateAsMap(facility, [productA], day(4)).values().toList()) ==
                ["A2|B": 7]
    }

    void 'matches the in-memory replay for a random transaction history'() {
        given:
        setupFacilityAndProducts()
        Random random = new Random(6201)
        List<InventoryItem> items = [itemA1, itemA2, itemB1]
        List<Location> bins = [binA, binB, null]
        // No INVENTORY transactions: when they come after a product inventory the replay can disagree with us
        // (see the test above). They're covered by the other tests instead.
        List<String> types = [CREDIT, CREDIT, CREDIT, DEBIT, DEBIT, PRODUCT_INVENTORY, INVENTORY_BASELINE]

        and: 'transactions with a few distinct transaction dates (so that they tie), entered in a random order'
        200.times { int i ->
            List<List> entries = (1..(1 + random.nextInt(3))).collect {
                [items[random.nextInt(items.size())], bins[random.nextInt(bins.size())], random.nextInt(20)]
            }
            insertTransaction(types[random.nextInt(types.size())], day(1 + random.nextInt(20)), entries,
                    day(30, i))
        }

        expect:
        (1..22).each { int d ->
            Date at = day(d)
            assert quantityAt(at) == replayQuantityAt(at), "Mismatch at day ${d}"
        }
    }

    /**
     * @return the stock at the facility, keyed on "<lot number>|<bin location name>" ("-" for the default bin)
     */
    private Map<String, Integer> quantityAt(Date at, Collection<Product> products = [productA, productB]) {
        return toMap(productAvailabilityService.getQuantityOnHandAtDate(facility, products, at))
    }

    /**
     * @return the stock at the facility as calculated by the in-memory replay in getAvailableItemsAtDate
     */
    private Map<String, Integer> replayQuantityAt(Date at, Collection<Product> products = [productA, productB]) {
        return toMap(productAvailabilityService.getAvailableItemsAtDate(facility, products, at))
    }

    private static Map<String, Integer> toMap(List<AvailableItem> availableItems) {
        return availableItems.collectEntries {
            ["${it.inventoryItem.lotNumber}|${it.binLocation?.name ?: '-'}".toString(), it.quantityOnHand as Integer]
        }
    }

    /**
     * @return midnight on the given day of January 2025, plus the given number of seconds. MySQL rounds fractional
     *         seconds when storing a datetime, so we stay at whole seconds.
     */
    private static Date day(int day, int seconds = 0) {
        Calendar calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(2025, Calendar.JANUARY, day, 0, 0, seconds)
        return calendar.time
    }

    /**
     * Inserts a transaction with the given entries, each given as [inventory item, bin location, quantity].
     */
    private void insertTransaction(String transactionTypeId, Date transactionDate, List<List> entries,
                                   Date dateCreated = transactionDate, Location location = facility) {
        String transactionId = UUID.randomUUID().toString()
        sessionFactory.currentSession.createSQLQuery("""
                INSERT INTO transaction (id, version, date_created, last_updated, inventory_id, transaction_date,
                                         transaction_type_id)
                VALUES (:id, 0, :dateCreated, :dateCreated, :inventoryId, :transactionDate, :transactionTypeId)
            """)
                .setString("id", transactionId)
                .setTimestamp("dateCreated", dateCreated)
                .setString("inventoryId", location.inventory.id)
                .setTimestamp("transactionDate", transactionDate)
                .setString("transactionTypeId", transactionTypeId)
                .executeUpdate()

        entries.each { entry ->
            InventoryItem inventoryItem = entry[0] as InventoryItem
            Location binLocation = entry[1] as Location
            sessionFactory.currentSession.createSQLQuery("""
                    INSERT INTO transaction_entry (id, version, transaction_id, inventory_item_id, bin_location_id,
                                                   quantity)
                    VALUES (:id, 0, :transactionId, :inventoryItemId, :binLocationId, :quantity)
                """)
                    .setString("id", UUID.randomUUID().toString())
                    .setString("transactionId", transactionId)
                    .setString("inventoryItemId", inventoryItem.id)
                    .setString("binLocationId", binLocation?.id)
                    .setInteger("quantity", entry[2] as Integer)
                    .executeUpdate()
        }
    }
}
