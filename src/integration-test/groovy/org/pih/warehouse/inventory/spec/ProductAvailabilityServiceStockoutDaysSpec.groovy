package org.pih.warehouse.inventory.spec

import grails.gorm.transactions.Rollback
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

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
 * Tests ProductAvailabilityService.getStockoutDays, which must agree with getQuantityOnHandAtDate at the end of every
 * day of the period.
 *
 * Transactions are inserted with SQL for the same reasons as in ProductAvailabilityServiceQuantityOnHandAtDateSpec.
 * Unless a test says otherwise, the period is the 10 days from January 11 to January 20, 2025 (see stockoutDays).
 */
@Rollback
class ProductAvailabilityServiceStockoutDaysSpec extends IntegrationSpec {

    private static final String CREDIT = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
    private static final String DEBIT = Constants.CONSUMPTION_TRANSACTION_TYPE_ID
    private static final String INVENTORY = Constants.INVENTORY_TRANSACTION_TYPE_ID
    private static final String PRODUCT_INVENTORY = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
    private static final String INVENTORY_BASELINE = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID

    private static final int START = 11
    private static final int DAYS = 10

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
        assert stockoutDays() == [:]
    }

    void 'a product that is in stock for the whole period has no stockout days'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(15), [transactionEntry(item: itemA1, bin: binA, quantity: 9)])

        expect:
        assert stockoutDays() == [A: 0]
    }

    void 'counts the days from when the stock runs out to the day before it is restocked'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(13, 3600), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(CREDIT, day(17, 3600), [transactionEntry(item: itemA1, bin: binA, quantity: 5)])

        expect: 'out of stock at the end of the 13th, 14th, 15th and 16th'
        assert stockoutDays() == [A: 4]
    }

    void 'only counts the stock at the end of the day'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(13, 3600), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(CREDIT, day(13, 7200), [transactionEntry(item: itemA1, bin: binA, quantity: 5)])

        expect:
        assert stockoutDays() == [A: 0]
    }

    void 'sums the stock over every inventory item and bin, and counts negative stock as a stockout'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binB, quantity: 2),
        ])
        insertTransaction(DEBIT, day(12), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(15), [transactionEntry(item: itemA2, quantity: 5)])

        expect: 'A2 has 2 in bin B and -5 in the default bin from the 15th: -3 in total'
        assert stockoutDays() == [A: 6]
    }

    void 'a product with no stock and no transactions in the period is out of stock for the whole period'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(2), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])

        expect:
        assert stockoutDays() == [A: DAYS]
    }

    void 'a product whose first transaction is in the period is only counted from that day'() {
        given:
        insertTransaction(CREDIT, day(14), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(18), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])

        expect: 'out of stock at the end of the 18th, 19th and 20th, and not counted before the 14th'
        assert stockoutDays() == [A: 3]
    }

    void 'transactions after the period are ignored'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(DEBIT, day(21), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(CREDIT, day(25), [transactionEntry(item: itemB1, bin: binA, quantity: 10)])

        expect:
        assert stockoutDays() == [A: 0]
    }

    void 'a #countType in the period replaces the stock of every inventory item of the product'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binB, quantity: 20),
        ])
        insertTransaction(countTypeId, day(14), [transactionEntry(item: itemA1, bin: binA, quantity: 0)])
        insertTransaction(CREDIT, day(19), [transactionEntry(item: itemA2, bin: binB, quantity: 1)])

        expect: 'A2 was reset to zero by the count, so the product is out of stock from the 14th to the 18th'
        assert stockoutDays() == [A: 5]

        where:
        countType            | countTypeId
        "product inventory"  | PRODUCT_INVENTORY
        "inventory baseline" | INVENTORY_BASELINE
    }

    void 'an inventory transaction in the period replaces the stock of only that inventory item'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemA2, bin: binA, quantity: 3),
        ])
        insertTransaction(INVENTORY, day(13), [transactionEntry(item: itemA1, bin: binB, quantity: 0)])
        insertTransaction(DEBIT, day(15), [transactionEntry(item: itemA2, bin: binA, quantity: 3)])
        insertTransaction(INVENTORY, day(18), [transactionEntry(item: itemA1, bin: binA, quantity: 4)])

        expect: 'A1 drops to zero on the 13th, A2 on the 15th, and A1 is counted at 4 on the 18th'
        assert stockoutDays() == [A: 3]
    }

    void 'credits and debits that tie exactly with a count are applied after it'() {
        given:
        insertTransaction(CREDIT, day(1), [transactionEntry(item: itemA1, bin: binA, quantity: 10)])
        insertTransaction(PRODUCT_INVENTORY, day(12), [transactionEntry(item: itemA1, bin: binA, quantity: 3)], day(12))
        insertTransaction(DEBIT, day(12), [transactionEntry(item: itemA1, bin: binA, quantity: 3)], day(12))

        expect:
        assert stockoutDays() == [A: 9]
    }

    void 'only counts the given products, at the given facility'() {
        given:
        insertTransaction(CREDIT, day(1), [
                transactionEntry(item: itemA1, bin: binA, quantity: 10),
                transactionEntry(item: itemB1, bin: binA, quantity: 10),
        ])
        insertTransaction(DEBIT, day(16), [transactionEntry(item: itemB1, bin: binA, quantity: 10)])

        and: 'a stockout of the same item at another facility'
        Location otherFacility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        sessionFactory.currentSession.flush()
        insertTransaction(DEBIT, day(12), [transactionEntry(item: itemA1, quantity: 99)], day(12), otherFacility)

        expect:
        assert stockoutDays(null) == [A: 0, B: 5]
        assert stockoutDays([]) == [A: 0, B: 5]
        assert stockoutDays([productB]) == [B: 5]
    }

    void 'matches the quantity on hand at the end of each day for a random transaction history'() {
        given:
        Random random = new Random(6201)
        List<InventoryItem> items = [itemA1, itemA2, itemB1]
        List<Location> bins = [binA, binB, null]
        // Debits outnumber credits so that the products run out of stock now and then
        List<String> types = [CREDIT, CREDIT, DEBIT, DEBIT, DEBIT, PRODUCT_INVENTORY, INVENTORY_BASELINE, INVENTORY]

        and: 'transactions on random days before, during and after the period, entered in a random order'
        150.times { int i ->
            List<Map> entries = (1..(1 + random.nextInt(2))).collect {
                transactionEntry(item: items[random.nextInt(items.size())], bin: bins[random.nextInt(bins.size())],
                        quantity: random.nextInt(8))
            }
            insertTransaction(types[random.nextInt(types.size())], day(1 + random.nextInt(25), random.nextInt(86400)),
                    entries, day(40, i))
        }

        when:
        Map<String, Integer> expected = [A: 0, B: 0]
        (START..<(START + DAYS)).each { int d ->
            Map<String, BigDecimal> quantityByProduct = [A: 0, B: 0]
            productAvailabilityService.getQuantityOnHandAtDate(facility, [productA, productB], day(d + 1)).each {
                quantityByProduct[it.inventoryItem.lotNumber[0]] += it.quantityOnHand
            }
            quantityByProduct.each { String product, BigDecimal quantity ->
                if (quantity <= 0) {
                    expected[product]++
                }
            }
        }

        then: 'every product has transactions before the period, so every day of the period counts'
        assert stockoutDays() == expected
        assert expected.values().any { it > 0 } && expected.values().any { it < DAYS }
    }

    /**
     * @return the stockout days of each product in the period, keyed on "A" or "B"
     */
    private Map<String, Integer> stockoutDays(Collection<Product> products = [productA, productB]) {
        Map<String, String> names = [(productA.id): "A", (productB.id): "B"]
        return productAvailabilityService.getStockoutDays(facility, products, day(START), DAYS)
                .collectEntries { String productId, Integer days -> [names[productId], days] }
    }

    /**
     * @return midnight on the given day of January 2025, plus the given number of seconds
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
