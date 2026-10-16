package org.pih.warehouse.report.spec

import grails.gorm.transactions.Rollback
import grails.util.GrailsWebMockUtil
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.util.RandomUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.inventory.product.availability.AvailableItemMap
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.report.ReportService

/**
 * Tests ReportService.getTransactionReport, which sums the credits and debits of the period in the database.
 *
 * Transactions are inserted with SQL instead of GORM so that we control date_created (GORM always sets it to now)
 * and so that saving them doesn't trigger a product availability refresh. Each test runs in its own transaction
 * that is rolled back afterwards, and uses its own facility, so tests can't see each other's data.
 */
@Rollback
class ReportServiceTransactionReportSpec extends IntegrationSpec {

    private static final String ADJUSTMENT_CREDIT = Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID
    private static final String TRANSFER_IN = Constants.TRANSFER_IN_TRANSACTION_TYPE_ID
    private static final String CONSUMPTION = Constants.CONSUMPTION_TRANSACTION_TYPE_ID
    private static final String TRANSFER_OUT = Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID
    private static final String ADJUSTMENT_DEBIT = Constants.ADJUSTMENT_DEBIT_TRANSACTION_TYPE_ID
    private static final String PRODUCT_INVENTORY = Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID
    private static final String INVENTORY_BASELINE = Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID

    @Autowired
    ReportService reportService

    @Autowired
    ProductAvailabilityService productAvailabilityService

    @Autowired
    SessionFactory sessionFactory

    Location facility
    Location otherFacility
    Category category

    Product productA
    InventoryItem itemA1
    InventoryItem itemA2
    Product productB
    InventoryItem itemB1
    Product productC
    InventoryItem itemC1

    Tag tag1
    Tag tag2
    ProductCatalog catalog

    // The report period: transactions after day 5 (exclusive) and before day 15 (exclusive)
    Date startDate = day(5)
    Date endDate = day(15)

    void setup() {
        // The report uses Product.displayName, which looks up the locale in the session of the current request
        GrailsWebMockUtil.bindMockWebRequest()
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    /**
     * Product A has tags 1 and 2 and is in the catalog, product B has tag 1, and product C is in the catalog.
     */
    private void setupFacilityAndProducts() {
        facility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        otherFacility = new LocationTestBuilder()
                .name(new RandomUtil().randomStringFieldValue("facility"))
                .asFacility()
                .build(true)
        category = new CategoryTestBuilder().build(true)

        productA = new ProductTestBuilder().randomizedName().category(category).build(true)
        itemA1 = new InventoryItem(product: productA, lotNumber: "A1").save(failOnError: true)
        itemA2 = new InventoryItem(product: productA, lotNumber: "A2").save(failOnError: true)
        productB = new ProductTestBuilder().randomizedName().category(category).build(true)
        itemB1 = new InventoryItem(product: productB, lotNumber: "B1").save(failOnError: true)
        productC = new ProductTestBuilder().randomizedName().category(category).build(true)
        itemC1 = new InventoryItem(product: productC, lotNumber: "C1").save(failOnError: true)

        tag1 = new Tag(tag: new RandomUtil().randomStringFieldValue("tag")).save(failOnError: true)
        tag2 = new Tag(tag: new RandomUtil().randomStringFieldValue("tag")).save(failOnError: true)
        String code = new RandomUtil().randomStringFieldValue("catalog")
        catalog = new ProductCatalog(code: code, name: code).save(failOnError: true)
        sessionFactory.currentSession.flush()

        // Inserted with SQL because the products were already saved and flushed by their builders
        [[productA, tag1], [productA, tag2], [productB, tag1]].each { Product product, Tag tag ->
            executeSql("INSERT INTO product_tag (product_id, tag_id) VALUES (:productId, :tagId)",
                    [productId: product.id, tagId: tag.id])
        }
        [productA, productC].each { Product product ->
            executeSql("""INSERT INTO product_catalog_item (id, version, active, date_created, last_updated,
                                                            product_id, product_catalog_id)
                          VALUES (:id, 0, 1, NOW(), NOW(), :productId, :catalogId)""",
                    [id: UUID.randomUUID().toString(), productId: product.id, catalogId: catalog.id])
        }
        // So that the report loads the products with their tags and catalogs
        sessionFactory.currentSession.clear()
    }

    private void executeSql(String sql, Map<String, String> params) {
        def query = sessionFactory.currentSession.createSQLQuery(sql)
        params.each { name, value -> query.setString(name, value) }
        query.executeUpdate()
    }

    void 'sums credits and debits per product over the period'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(ADJUSTMENT_CREDIT, day(1), [[itemA1, 100]])
        insertTransaction(TRANSFER_IN, day(6), [[itemA1, 10], [itemA2, 5]])
        insertTransaction(CONSUMPTION, day(7), [[itemA1, 3]])
        insertTransaction(ADJUSTMENT_DEBIT, day(8), [[itemA2, 2]])
        insertTransaction(TRANSFER_IN, day(9), [[itemB1, 7]])

        when:
        Map<String, Map> rows = report()

        then:
        rows.keySet() == ["A", "B"] as Set
        rows.A.subMap("Opening", "Credits", "Debits", "Net Change", "Closing") ==
                [Opening: 100, Credits: 15, Debits: 5, "Net Change": 10, Closing: 110]
        rows.B.subMap("Opening", "Credits", "Debits", "Net Change", "Closing") ==
                [Opening: 0, Credits: 7, Debits: 0, "Net Change": 7, Closing: 7]
    }

    void 'a credit with a negative quantity counts as a debit'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(ADJUSTMENT_CREDIT, day(6), [[itemA1, 10]])
        insertTransaction(ADJUSTMENT_CREDIT, day(7), [[itemA1, -4]])

        expect:
        report().A.subMap("Credits", "Debits") == [Credits: 10, Debits: 4]
    }

    void 'only counts credits and debits inside the period, at the facility'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(TRANSFER_IN, day(4), [[itemA1, 1]])
        insertTransaction(TRANSFER_IN, startDate, [[itemA1, 2]])
        insertTransaction(TRANSFER_IN, day(6), [[itemA1, 4]])
        insertTransaction(TRANSFER_IN, endDate, [[itemA1, 8]])
        insertTransaction(TRANSFER_IN, day(6), [[itemA1, 16]], day(6), otherFacility)
        insertTransaction(PRODUCT_INVENTORY, day(7), [[itemA1, 32]])

        expect: 'only the transfer on day 6; a transaction exactly at the start of the period is excluded'
        report().A.Credits == 4
    }

    void 'products without credits or debits in the period are not included'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(TRANSFER_IN, day(1), [[itemA1, 10]])
        insertTransaction(INVENTORY_BASELINE, day(6), [[itemB1, 5]])
        insertTransaction(TRANSFER_IN, day(6), [[itemC1, 1]])

        expect:
        report().keySet() == ["C"] as Set
    }

    void 'filters by category, tag, catalog and product'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(TRANSFER_IN, day(6), [[itemA1, 1], [itemB1, 2], [itemC1, 3]])

        expect:
        report(categories: [category]).keySet() == ["A", "B", "C"] as Set
        report(categories: [new CategoryTestBuilder().build(true)]).isEmpty()
        report(tags: [tag2]).keySet() == ["A"] as Set
        report(tags: [tag1]).keySet() == ["A", "B"] as Set
        report(catalogs: [catalog]).keySet() == ["A", "C"] as Set
        report(tags: [tag1], catalogs: [catalog]).keySet() == ["A"] as Set
        report(products: [productB, productC]).keySet() == ["B", "C"] as Set
    }

    void 'a product with several of the selected tags is only counted once'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(TRANSFER_IN, day(6), [[itemA1, 10]])

        expect: 'note that the previous implementation joined the tags and counted product A twice'
        report(tags: [tag1, tag2]).A.Credits == 10
    }

    void 'the detailed report has the quantity of each transaction type and the backdated corrections'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(TRANSFER_IN, day(6), [[itemA1, 10]])
        insertTransaction(ADJUSTMENT_CREDIT, day(7), [[itemA1, 4]])
        insertTransaction(ADJUSTMENT_DEBIT, day(8), [[itemA1, 1]])
        insertTransaction(CONSUMPTION, day(9), [[itemA1, 3]])
        // Created more than a day after its transaction date
        insertTransaction(ADJUSTMENT_CREDIT, day(10), [[itemA1, 6]], day(12))
        insertTransaction(CONSUMPTION, day(10), [[itemA1, 2]], day(12))
        // Created less than a day after its transaction date
        insertTransaction(TRANSFER_OUT, day(11), [[itemA1, 5]], day(11, 3600))

        when:
        Map row = report(includeDetails: true).A

        then:
        row["Transfer In"] == 10
        row["Consumption"] == 5
        row["Transfer Out"] == 5
        row["Adjustment (Credit and Debit)"] == 4 + 6 - 1
        row["Backdated inventory correction"] == 6 - 2
        row.subMap("Credits", "Debits", "Closing") == [Credits: 20, Debits: 11, Closing: 9]
    }

    void 'backdated transactions are each counted, even when they have the same dates'() {
        given:
        setupFacilityAndProducts()
        insertTransaction(ADJUSTMENT_CREDIT, day(6), [[itemA1, 5]], day(10))
        insertTransaction(ADJUSTMENT_CREDIT, day(6), [[itemA1, 7]], day(10))

        expect: 'note that the previous implementation only counted one of them'
        report(includeDetails: true).A["Backdated inventory correction"] == 12
    }

    void 'matches the previous implementation for a random transaction history'() {
        given:
        setupFacilityAndProducts()
        Random random = new Random(6201)
        List<InventoryItem> items = [itemA1, itemA2, itemB1, itemC1]
        List<String> types = [ADJUSTMENT_CREDIT, TRANSFER_IN, TRANSFER_IN, CONSUMPTION, CONSUMPTION, TRANSFER_OUT,
                              ADJUSTMENT_DEBIT, PRODUCT_INVENTORY, INVENTORY_BASELINE]

        and: 'transactions on and around the edges of the period, some of them backdated or with negative quantities'
        300.times {
            List<List> entries = (1..(1 + random.nextInt(3))).collect {
                [items[random.nextInt(items.size())], random.nextInt(30) - 5]
            }
            Date transactionDate = day(1 + random.nextInt(20), random.nextInt(3) * 1800)
            Date dateCreated = random.nextInt(4) == 0 ? transactionDate + 2 : transactionDate
            insertTransaction(types[random.nextInt(types.size())], transactionDate, entries, dateCreated)
        }

        expect: 'the same report with and without each filter (one tag at a time, see the test above)'
        [
                [:],
                [categories: [category]],
                [tags: [tag1]],
                [catalogs: [catalog]],
                [products: [productA, productC]],
        ].each { Map filters ->
            [false, true].each { Boolean includeDetails ->
                Map args = filters + [includeDetails: includeDetails]
                Map<String, Map> actual = report(args)
                Map<String, Map> expected = previousReport(args)
                assert actual.keySet() == expected.keySet(), "Different products for ${args}"
                expected.each { String product, Map row ->
                    Map differences = row.findAll { column, value -> actual[product][column] != value }
                            .collectEntries { column, value -> [(column): [expected: value, actual: actual[product][column]]] }
                    Set extraColumns = actual[product].keySet() - row.keySet()
                    assert !differences && !extraColumns, "Mismatch for ${args}, product ${product}: ${differences} ${extraColumns}"
                }
            }
        }
    }

    /**
     * @return the rows of the report, keyed on product alias (A, B or C)
     */
    private Map<String, Map> report(Map args = [:]) {
        return keyByProduct(reportService.getTransactionReport(facility, args.categories as List,
                args.tags as List, args.catalogs as List, args.products as List, startDate, endDate,
                args.includeDetails as Boolean))
    }

    /**
     * @return the rows of the report as calculated by the previous implementation, which loaded the transaction
     *         entries of the period into memory, keyed on product alias (A, B or C)
     */
    private Map<String, Map> previousReport(Map args = [:]) {
        List<TransactionEntry> entries = TransactionEntry.createCriteria().list {
            inventoryItem {
                if (args.products) {
                    'in'('product', args.products)
                }
                product {
                    if (args.categories) {
                        'in'('category', args.categories)
                    }
                    if (args.tags) {
                        tags {
                            'in'("id", args.tags*.id)
                        }
                    }
                    if (args.catalogs) {
                        productCatalogItems {
                            productCatalog {
                                'in'("id", args.catalogs*.id)
                            }
                        }
                    }
                }
            }
            transaction {
                transactionType {
                    'in'('transactionCode', [TransactionCode.CREDIT, TransactionCode.DEBIT])
                }
                gt('transactionDate', startDate)
                lt('transactionDate', endDate)
                inventory {
                    eq('warehouse', facility)
                }
            }
        } as List<TransactionEntry>

        Map<Product, List<TransactionEntry>> productsMap = entries.groupBy { it.inventoryItem.product }
        AvailableItemMap opening = productAvailabilityService.getQuantityOnHandAtDateAsMap(
                facility, productsMap.keySet().toList(), startDate)
        AvailableItemMap closing = productAvailabilityService.getQuantityOnHandAtDateAsMap(
                facility, productsMap.keySet().toList(), endDate)
        List<TransactionType> transactionTypes = TransactionType.findAllByTransactionCodeInList(
                [TransactionCode.CREDIT, TransactionCode.DEBIT])

        return keyByProduct(productsMap.collect { Product product, List<TransactionEntry> value ->
            Integer openingBalance = opening.getAllByProduct(product).quantityOnHand.sum() ?: 0
            Integer closingBalance = closing.getAllByProduct(product).quantityOnHand.sum() ?: 0
            Integer credits = reportService.getCreditTransactionEntries(value).sum { it.quantity } as Integer ?: 0
            Integer debits = reportService.getDebitTransactionEntries(value)
                    .sum { Math.abs(it.quantity) } as Integer ?: 0
            Map data = [
                    productCode: product.productCode,
                    name: product.name,
                    displayName: product.displayName ?: '',
                    categoryName: product.category.name,
                    pricePerUnit: product.pricePerUnit ?: '',
                    openingBalance: openingBalance,
                    credits: credits,
                    debits: debits,
                    closingBalance: closingBalance,
            ]
            if (!args.includeDetails) {
                return reportService.getTransactionReportRow(data + [netChange: closingBalance - openingBalance])
            }
            // The previous implementation called unique() without a closure, which compares transactions with
            // compareTo (by date) and so dropped backdated transactions with the same dates (see the test above).
            // Compare by id here so that the rest of the report can be compared.
            List<TransactionEntry> backdatedEntries = value.transaction
                    .unique { it.id }
                    .findAll { it.dateCreated > (it.transactionDate + 1) }
                    .transactionEntries
                    .flatten()
                    .findAll { it.inventoryItem.product == product }
            return reportService.getTransactionReportDetailedRow(data + [
                    productFamilyName: product.productFamily?.name ?: '',
                    formulary: product.productCatalogsToString(),
                    tag: product.tagsToString(),
                    detailedReportData: value
                            .groupBy { it.transaction.transactionType.name }
                            .collectEntries { name, entriesByType -> [(name): entriesByType.sum { it.quantity }] },
                    availableTransactionTypes: transactionTypes,
                    backdatedQuantity: backdatedEntries.sum {
                        reportService.isTransactionEntryCredit(it) ? Math.abs(it.quantity) : -Math.abs(it.quantity)
                    } ?: 0,
            ])
        })
    }

    private Map<String, Map> keyByProduct(List rows) {
        Map<String, String> aliases = [(productA.name): "A", (productB.name): "B", (productC.name): "C"]
        return rows.collectEntries { Map row -> [(aliases[row.Name]): row] }
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
     * Inserts a transaction with the given entries, each given as [inventory item, quantity].
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

        entries.eachWithIndex { entry, int index ->
            sessionFactory.currentSession.createSQLQuery("""
                    INSERT INTO transaction_entry (id, version, transaction_id, inventory_item_id, quantity,
                                                   transaction_entries_idx)
                    VALUES (:id, 0, :transactionId, :inventoryItemId, :quantity, :index)
                """)
                    .setString("id", UUID.randomUUID().toString())
                    .setString("transactionId", transactionId)
                    .setString("inventoryItemId", (entry[0] as InventoryItem).id)
                    .setInteger("quantity", entry[1] as Integer)
                    .setInteger("index", index)
                    .executeUpdate()
        }
    }
}
