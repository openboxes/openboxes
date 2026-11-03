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
 *
 * Product inventory and inventory baseline transactions both have the PRODUCT_INVENTORY transaction code, which is
 * all the query looks at, so the tests of counts run with each of them.
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

    void setup() {
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
        expect:
        assert quantityAt(day(10)) == [:]
    }

    void 'sums credits and subtracts debits per inventory item and bin'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA1, quantity: 5),
                transactionEntry(item: itemA2, bin: binA, quantity: 7),
        ])
        insertTransaction(DEBIT, day(2), [
                transactionEntry(item: itemA1, bin: binA, quantity: 3),
                transactionEntry(item: itemA2, bin: binA, quantity: 7),
        ])
        insertTransaction(DEBIT, day(3), [transactionEntry(item: itemA1, quantity: 8)])

        expect: 'zero quantities are left out, negative ones are kept'
        assert quantityAt(day(10)) == ["A1|A": 7, "A1|-": -3]
    }

    void 'only counts transactions before the given date'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(CREDIT, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 5)])
        insertTransaction(DEBIT, day(3), [transactionEntry(item: itemA1, bin: binA, quantity: 1)])

        expect:
        assert quantityAt(day(1)) == [:]
        assert quantityAt(day(2)) == ["A1|A": 10]
        assert quantityAt(day(2, 1)) == ["A1|A": 15]
        assert quantityAt(day(3)) == ["A1|A": 15]
        assert quantityAt(day(4)) == ["A1|A": 14]
    }

    void 'a #countType replaces the stock of every inventory item of the product'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binB, quantity: 20),
                transactionEntry(item: itemB1, bin: binA, quantity: 30),
        ])
        insertTransaction(countTypeId, day(2), [
                transactionEntry(item: itemA1, bin: binA, quantity: 4),
                transactionEntry(item: itemA1, bin: binB, quantity: 1),
        ])
        insertTransaction(DEBIT, day(3), [transactionEntry(item: itemA1, bin: binA, quantity: 1)])

        expect: 'before the count, the full history counts'
        assert quantityAt(day(2)) == ["A1|A": 10, "A2|B": 20, "B1|A": 30]

        and: 'after the count, items that were not counted are gone, and other products are not affected'
        assert quantityAt(day(3)) == ["A1|A": 4, "A1|B": 1, "B1|A": 30]
        assert quantityAt(day(4)) == ["A1|A": 3, "A1|B": 1, "B1|A": 30]

        where:
        countType            | countTypeId
        "product inventory"  | PRODUCT_INVENTORY
        "inventory baseline" | INVENTORY_BASELINE
    }

    void 'an inventory transaction replaces the stock of only that inventory item'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA1, bin: binB, quantity: 3),
                transactionEntry(item: itemA2, bin: binA, quantity: 20),
        ])
        insertTransaction(INVENTORY, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 6)])
        insertTransaction(CREDIT, day(3), [transactionEntry(item: itemA1, bin: binB, quantity: 2)])

        expect: 'item A1 restarts from the count, item A2 keeps its full history'
        assert quantityAt(day(4)) == ["A1|A": 6, "A1|B": 2, "A2|A": 20]
    }

    void 'a #countType resets every inventory item, even when its counted items are counted again later'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binA, quantity: 20),
        ])
        insertTransaction(countTypeId, day(2), [transactionEntry(item: itemA2, bin: binA, quantity: 5)])
        insertTransaction(INVENTORY, day(3), [transactionEntry(item: itemA2, bin: binB, quantity: 7)])

        expect: 'item A1 was reset to zero by the count, and item A2 restarts from its own count'
        assert quantityAt(day(4)) == ["A2|B": 7]

        and: 'note that the in-memory replay disagrees: it skips the count because its only entry is older than the ' +
                'count of A2, so it never resets A1'
        assert replayQuantityAt(day(4)) == ["A1|A": 10, "A2|B": 7]

        where:
        countType            | countTypeId
        "product inventory"  | PRODUCT_INVENTORY
        "inventory baseline" | INVENTORY_BASELINE
    }

    void 'only the most recent count is used, with a #countType'() {
        given:
        insertTransaction(countTypeId, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 100),
                transactionEntry(item: itemA2, bin: binA, quantity: 50),
        ])
        insertTransaction(INVENTORY, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 80)])
        insertTransaction(INVENTORY, day(3), [transactionEntry(item: itemA1, bin: binA, quantity: 70)])
        insertTransaction(countTypeId, day(4), [transactionEntry(item: itemA2, bin: binB, quantity: 5)])
        insertTransaction(INVENTORY, day(5), [transactionEntry(item: itemA2, bin: binB, quantity: 6)])

        expect:
        assert quantityAt(day(2)) == ["A1|A": 100, "A2|A": 50]
        assert quantityAt(day(4)) == ["A1|A": 70, "A2|A": 50]
        assert quantityAt(day(5)) == ["A2|B": 5]
        assert quantityAt(day(6)) == ["A2|B": 6]

        where:
        countType            | countTypeId
        "product inventory"  | PRODUCT_INVENTORY
        "inventory baseline" | INVENTORY_BASELINE
    }

    void 'transactions are ordered by transaction date, then by date created, with a #countType'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)], day(1))
        insertTransaction(countTypeId, day(3), [transactionEntry(item: itemA1, bin: binA, quantity: 50)], day(3))

        and: 'a credit back-dated to before the count, but entered after it, is overwritten by the count'
        insertTransaction(CREDIT, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 1000)], day(4))

        and: 'a credit with the same transaction date as the count is only overwritten if it was entered before it'
        insertTransaction(CREDIT, day(3), [transactionEntry(item: itemA1, bin: binA, quantity: 100)], day(2))
        insertTransaction(CREDIT, day(3), [transactionEntry(item: itemA1, bin: binA, quantity: 7)], day(5))

        expect:
        assert quantityAt(day(10)) == ["A1|A": 57]

        where:
        countType            | countTypeId
        "product inventory"  | PRODUCT_INVENTORY
        "inventory baseline" | INVENTORY_BASELINE
    }

    void 'credits and debits that tie exactly with a #countType are applied after it'() {
        given: 'a count and its adjustments created together, with the same transaction date and date created'
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binA, quantity: 3),
        ])
        insertTransaction(countTypeId, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 12)], day(2))
        insertTransaction(CREDIT, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 5)], day(2))
        insertTransaction(DEBIT, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 2)], day(2))

        and: 'the same for an inventory transaction on a single item'
        insertTransaction(INVENTORY, day(3), [transactionEntry(item: itemB1, bin: binB, quantity: 4)], day(3))
        insertTransaction(CREDIT, day(3), [transactionEntry(item: itemB1, bin: binB, quantity: 1)], day(3))

        expect:
        assert quantityAt(day(4)) == ["A1|A": 15, "B1|B": 5]

        where:
        countType            | countTypeId
        "product inventory"  | PRODUCT_INVENTORY
        "inventory baseline" | INVENTORY_BASELINE
    }

    void 'when two counts tie exactly, only one of them is used'() {
        given: 'a product inventory and an inventory baseline of the same product'
        insertTransaction(PRODUCT_INVENTORY, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)], day(1))
        insertTransaction(INVENTORY_BASELINE, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 20)], day(1))

        and: 'two inventory transactions of the same item'
        insertTransaction(INVENTORY, day(2), [transactionEntry(item: itemB1, bin: binA, quantity: 30)], day(2))
        insertTransaction(INVENTORY, day(2), [transactionEntry(item: itemB1, bin: binA, quantity: 40)], day(2))

        when:
        Map<String, Integer> quantity = quantityAt(day(3))

        then: 'which one is arbitrary, but they must not be added together'
        assert quantity["A1|A"] in [10, 20]
        assert quantity["B1|A"] in [30, 40]
    }

    void 'fetches every product when no products are given, but only at the given facility'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemB1, quantity: 3),
        ])

        and: 'stock of the same item at another facility'
        Location otherFacility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        sessionFactory.currentSession.flush()
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, quantity: 99)], day(1), otherFacility)

        expect:
        assert quantityAt(day(2), null) == ["A1|A": 10, "B1|-": 3]
        assert quantityAt(day(2), []) == ["A1|A": 10, "B1|-": 3]
        assert quantityAt(day(2), [productB]) == ["B1|-": 3]
    }

    void 'getQuantityOnHandAtDateAsMap uses the transaction history for past dates'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 4)])

        when:
        List<AvailableItem> availableItems = productAvailabilityService.getQuantityOnHandAtDateAsMap(
                facility, [productA], day(2)).values().toList()

        then:
        assert availableItems.size() == 1
        assert availableItems[0].inventoryItem.id == itemA1.id
        assert availableItems[0].binLocation.id == binA.id
        assert availableItems[0].quantityOnHand == 10
    }

    void 'getAvailableItemsAtDate still uses the in-memory replay'() {
        given: 'a history where the replay and the database calculation disagree'
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binA, quantity: 20),
        ])
        insertTransaction(PRODUCT_INVENTORY, day(2), [transactionEntry(item: itemA2, bin: binA, quantity: 5)])
        insertTransaction(INVENTORY, day(3), [transactionEntry(item: itemA2, bin: binB, quantity: 7)])

        expect:
        assert toMap(productAvailabilityService.getAvailableItemsAtDate(facility, [productA], day(4))) ==
                ["A1|A": 10, "A2|B": 7]
        assert toMap(productAvailabilityService.getQuantityOnHandAtDateAsMap(facility, [productA], day(4))
                .values().toList()) == ["A2|B": 7]
    }

    void 'matches the in-memory replay for a random transaction history'() {
        given:
        Random random = new Random(6201)
        List<InventoryItem> items = [itemA1, itemA2, itemB1]
        List<Location> bins = [binA, binB, null]
        // No INVENTORY transactions: when they come after a product inventory the replay can disagree with us
        // (see the test above). They're covered by the other tests instead.
        List<String> types = [CREDIT, CREDIT, CREDIT, DEBIT, DEBIT, PRODUCT_INVENTORY, INVENTORY_BASELINE]

        and: 'transactions with a few distinct transaction dates (so that they tie), entered in a random order'
        200.times { int i ->
            List<Map> entries = (1..(1 + random.nextInt(3))).collect {
                transactionEntry(item: items[random.nextInt(items.size())], bin: bins[random.nextInt(bins.size())],
                        quantity: random.nextInt(20))
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
     * A transaction entry for insertTransaction. Leave out the bin for the default bin location.
     */
    private static Map transactionEntry(Map args) {
        return [item: args.item as InventoryItem, bin: args.bin as Location, quantity: args.quantity as Integer]
    }

    /**
     * Inserts a transaction with the given entries (see transactionEntry).
     */
    private void insertTransaction(String transactionTypeId, Date transactionDate, List<Map> entries,
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

        entries.each { Map entry ->
            sessionFactory.currentSession.createSQLQuery("""
                    INSERT INTO transaction_entry (id, version, transaction_id, inventory_item_id, bin_location_id,
                                                   quantity)
                    VALUES (:id, 0, :transactionId, :inventoryItemId, :binLocationId, :quantity)
                """)
                    .setString("id", UUID.randomUUID().toString())
                    .setString("transactionId", transactionId)
                    .setString("inventoryItemId", (entry.item as InventoryItem).id)
                    .setString("binLocationId", (entry.bin as Location)?.id)
                    .setInteger("quantity", entry.quantity as Integer)
                    .executeUpdate()
        }
    }
}
