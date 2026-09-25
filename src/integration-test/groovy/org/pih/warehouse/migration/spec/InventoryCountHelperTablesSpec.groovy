package org.pih.warehouse.migration.spec

import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.product.CategoryTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryCountService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import util.LiquibaseUtil

import javax.sql.DataSource

/**
 * The cycle count helper tables (adjustment_candidate, inventory_baseline_candidate,
 * product_inventory_candidate) are built by views/changelog.xml only when they do not exist yet, and
 * rebuilt on demand by InventoryCountService.refreshInventoryCountCandidates. These tests plant sentinel
 * rows that no transaction justifies: a sentinel that survives proves the table was left alone, a sentinel
 * that disappears proves the table was rebuilt from the transaction data.
 *
 * DDL does not roll back and the database is shared by every integration spec, so every case cleans up
 * the rows it planted, and cleanup rebuilds any helper table a failed case may have left missing. Not a
 * smoke spec on purpose: it reruns the migrations and rebuilds tables, which a deploy-time health check
 * must never do.
 */
class InventoryCountHelperTablesSpec extends IntegrationSpec {

    static final List<String> HELPER_TABLES = [
            'adjustment_candidate',
            'inventory_baseline_candidate',
            'product_inventory_candidate',
    ]

    static final Map<String, List<String>> EXPECTED_INDEXES = [
            adjustment_candidate        : ['idx_product_inventory_date'],
            inventory_baseline_candidate: ['idx_inventory_product_date', 'idx_transaction_id', 'idx_product_inventory_base'],
            product_inventory_candidate : ['idx_inventory_product_date', 'idx_transaction_id', 'idx_product_inventory_base'],
    ]

    InventoryCountService inventoryCountService
    DataSource dataSource

    Sql sql

    void setup() {
        sql = new Sql(dataSource)
    }

    void cleanup() {
        HELPER_TABLES.each { String table ->
            if (!tableExists(table)) {
                inventoryCountService.refreshInventoryCountCandidates(table)
            }
            sql.execute("DELETE FROM ${table} WHERE transaction_id LIKE 'sentinel-%'".toString())
        }
    }

    void "running the migrations again leaves existing helper tables alone"() {
        given: "a sentinel row in every helper table"
        HELPER_TABLES.each { String table -> plantSentinel(table) }

        when:
        LiquibaseUtil.executeMigrations()
        selectFromDependentViews()

        then: "the tables were not rebuilt and the views over them still work"
        noExceptionThrown()
        HELPER_TABLES.each { String table -> assert sentinelCount(table) == 1 }
    }

    void "a missing helper table is rebuilt by the migrations without touching the others"() {
        given: "a sentinel in the adjustment table and no baseline table"
        plantSentinel('adjustment_candidate')
        sql.execute('DROP TABLE inventory_baseline_candidate')

        when:
        LiquibaseUtil.executeMigrations()

        then: "the baseline table is back, with its indexes"
        assert tableExists('inventory_baseline_candidate')
        assert indexNames('inventory_baseline_candidate') == EXPECTED_INDEXES.inventory_baseline_candidate as Set

        and: "the adjustment table was left alone"
        assert sentinelCount('adjustment_candidate') == 1
    }

    void "the manual refresh rebuilds all helper tables from the transaction data"() {
        given: "a sentinel row in every helper table"
        HELPER_TABLES.each { String table -> plantSentinel(table) }

        when:
        inventoryCountService.refreshInventoryCountCandidates(null)
        selectFromDependentViews()

        then: "every table was rebuilt, with its indexes, and the views over them still work"
        noExceptionThrown()
        HELPER_TABLES.each { String table ->
            assert sentinelCount(table) == 0
            assert indexNames(table) == EXPECTED_INDEXES[table] as Set
        }
    }

    void "the manual refresh can rebuild a single helper table"() {
        given:
        plantSentinel('adjustment_candidate')
        plantSentinel('inventory_baseline_candidate')

        when:
        inventoryCountService.refreshInventoryCountCandidates('adjustment_candidate')

        then:
        assert sentinelCount('adjustment_candidate') == 0
        assert sentinelCount('inventory_baseline_candidate') == 1
    }

    void "the manual refresh rejects a table it does not own"() {
        when:
        inventoryCountService.refreshInventoryCountCandidates(tableName)

        then:
        thrown(IllegalArgumentException)

        where:
        tableName << ['transaction', '']
    }

    void "the manual refresh agrees with the incremental maintenance of the adjustment table"() {
        given: "an adjustment transaction saved through the domain, which the listener records in adjustment_candidate"
        Map<String, String> ids = createAdjustmentTransaction()

        expect: "the listener recorded the transaction"
        assert candidateCount('adjustment_candidate', ids.transactionId, ids.productId) == 1

        when: "the table is rebuilt from the transaction data"
        inventoryCountService.refreshInventoryCountCandidates('adjustment_candidate')

        then: "the rebuilt table holds the same row"
        assert candidateCount('adjustment_candidate', ids.transactionId, ids.productId) == 1

        when: "the transaction is deleted through the domain and the table rebuilt again"
        deleteTransaction(ids.transactionId)
        int countAfterDelete = candidateCount('adjustment_candidate', ids.transactionId, ids.productId)
        inventoryCountService.refreshInventoryCountCandidates('adjustment_candidate')

        then: "the listener removed the row and the rebuild agrees"
        assert countAfterDelete == 0
        assert candidateCount('adjustment_candidate', ids.transactionId, ids.productId) == 0

        cleanup:
        deleteTransaction(ids.transactionId)
        deleteProduct(ids.productId)
    }

    void "the migration SQL files split into the statements the migration runs"() {
        expect:
        assert InventoryCountService.readMigrationStatements('views/adjustment-candidate.sql').size() == 3
        assert InventoryCountService.readMigrationStatements('views/inventory-baseline-candidate.sql').size() == 5
        assert InventoryCountService.readMigrationStatements('views/product-inventory-candidate.sql').size() == 5
    }

    /**
     * Domain saves need a transaction (and its session); see the note on ApiSpec. Returns the ids so the
     * feature method never touches detached instances.
     */
    @Transactional
    Map<String, String> createAdjustmentTransaction() {
        Location facility = new LocationTestBuilder().findOrBuildMainFacility()
        Category rootCategory = new CategoryTestBuilder().rootCategory().findOrBuild()
        Product product = new ProductTestBuilder().randomizedName().category(rootCategory).build(true)
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: "sentinel-lot").save(failOnError: true)
        Transaction transaction = new Transaction(
                transactionType: TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID),
                inventory: facility.inventory,
                transactionDate: new Date(),
        )
        transaction.addToTransactionEntries(new TransactionEntry(inventoryItem: inventoryItem, product: product, quantity: 1))
        transaction.save(failOnError: true, flush: true)
        return [transactionId: transaction.id, productId: product.id]
    }

    @Transactional
    void deleteTransaction(String transactionId) {
        Transaction.get(transactionId)?.delete(flush: true)
    }

    @Transactional
    void deleteProduct(String productId) {
        Product product = Product.get(productId)
        if (product) {
            InventoryItem.findAllByProduct(product)*.delete(flush: true)
            product.delete(flush: true)
        }
    }

    private void selectFromDependentViews() {
        sql.rows('SELECT * FROM product_physical_count_history LIMIT 1')
        sql.rows('SELECT * FROM inventory_counts LIMIT 1')
    }

    private void plantSentinel(String table) {
        sql.execute("""INSERT INTO ${table} (transaction_id, product_id, transaction_date, inventory_id, facility_id)
                       VALUES ('sentinel-${table}', 'sentinel', NOW(), 'sentinel', 'sentinel')""".toString())
    }

    private int sentinelCount(String table) {
        GroovyRowResult row = sql.firstRow("SELECT COUNT(*) AS c FROM ${table} WHERE transaction_id LIKE 'sentinel-%'".toString())
        return row.c as int
    }

    private int candidateCount(String table, String transactionId, String productId) {
        GroovyRowResult row = sql.firstRow(
                "SELECT COUNT(*) AS c FROM ${table} WHERE transaction_id = :transactionId AND product_id = :productId".toString(),
                [transactionId: transactionId, productId: productId])
        return row.c as int
    }

    private boolean tableExists(String table) {
        GroovyRowResult row = sql.firstRow(
                'SELECT COUNT(*) AS c FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = :table',
                [table: table])
        return (row.c as int) == 1
    }

    private Set<String> indexNames(String table) {
        return sql.rows("SHOW INDEX FROM ${table}".toString())*.Key_name.toSet()
    }
}
