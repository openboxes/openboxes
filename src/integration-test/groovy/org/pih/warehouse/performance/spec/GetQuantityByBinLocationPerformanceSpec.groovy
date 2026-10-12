package org.pih.warehouse.performance.spec

import org.hibernate.SessionFactory

import javax.sql.DataSource
import java.sql.Timestamp

import groovy.sql.Sql
import spock.lang.Shared

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.BinLocationItem
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.TransactionCode
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.performance.spec.base.PerformanceSpec

/**
 * Benchmarks InventoryService#getQuantityByBinLocation(Location, Location) against a facility-scale
 * dataset (comparable to the ~28,000 products seen in production) to demonstrate the performance
 * difference between computing stock for the whole facility (old behaviour, still reachable via the
 * single-arg getQuantityByBinLocation(Location) used by ReportController) versus querying a single
 * bin location directly (current behaviour of the two-arg overload).
 */
class GetQuantityByBinLocationPerformanceSpec extends PerformanceSpec {

    static final int PRODUCT_COUNT = 28_000
    static final int BIN_COUNT = 50
    static final int TARGET_BIN_ITEM_COUNT = 50
    static final String RESET_REGRESSION_PRODUCT_CODE = "PERF-TEST-RESET-REGRESSION"

    InventoryService inventoryService
    SessionFactory sessionFactory
    DataSource dataSource

    @Shared
    Location facility

    @Shared
    Location targetBin

    /**
     * Creates the facility/bins (GORM, inside their own withNewTransaction{} so they're actually committed
     * - not just flushed - before anything else runs), then bulk-seeds transaction entries via plain JDBC.
     *
     * PerformanceSpec's setup() wraps this whole call in @Transactional, which is what lets setupData() make
     * ordinary GORM calls at all (plain GORM calls issued directly from test code, as opposed to a call into
     * a @Transactional Grails service, have no Hibernate session bound to the thread otherwise). But
     * @Transactional only commits once setup() *returns*, and the raw-JDBC bulk seed below opens its own
     * separate pooled connection for speed - which cannot see still-uncommitted GORM writes. Wrapping just
     * the facility/bin creation in withNewTransaction{} forces a real, immediate commit of that small amount
     * of data, which the bulk seed's own connection (and the old code path's gpars worker threads, later, in
     * the feature method) can then see.
     */
    @Override
    void setupData() {
        List<Location> bins
        TransactionType transactionType
        TransactionType creditType

        Location.withNewTransaction {
            facility = new LocationTestBuilder().name("Perf Test Facility").asFacility().build(true)
            Inventory inventory = new Inventory(warehouse: facility)
            inventory.save(failOnError: true, flush: true)
            facility.inventory = inventory
            facility.save(failOnError: true, flush: true)

            bins = (1..BIN_COUNT).collect { int i ->
                new LocationTestBuilder().name("Perf Test Bin ${i}").asBinLocation(facility).build(true)
            }
            targetBin = bins[0]

            transactionType = TransactionType.get(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)
            assert transactionType != null: "TransactionType ${Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID} not found"
            creditType = TransactionType.findByTransactionCode(TransactionCode.CREDIT)
            assert creditType != null: "No TransactionType with transactionCode CREDIT found"

            // Hibernate's lazy-association bytecode enhancement nulls out to-one associations like
            // "inventory" once the owning entity is detached (this transaction commits/closes before the
            // feature method runs). Re-fetch with an explicit join so inventory survives detachment - the
            // same pattern LocationTestBuilder.findOrBuildMainFacility() uses for exactly this reason.
            facility = Location.createCriteria().get {
                eq("id", facility.id)
                join("inventory")
            } as Location
        }

        seedTransactionEntries(facility, bins, targetBin, transactionType)
        seedResetRegressionProduct(facility, targetBin, bins[1], creditType, transactionType)
    }

    @Override
    void cleanupData() {
        // Everything here runs as raw SQL rather than GORM delete() calls. Location.afterDelete() always
        // publishes a RefreshProductAvailabilityEvent (grails-app/domain/org/pih/warehouse/core/Location.groovy),
        // which in this test environment runs synchronously and ends up calling
        // InventoryService.getTransactionEntriesByLocation on one of our bins - which throws ("Location must
        // have an inventory") because bins never have their own inventory, only the facility does. Raw SQL
        // avoids triggering that pre-existing, unrelated limitation entirely.
        Sql sql = new Sql(dataSource)
        try {
            sql.execute("DELETE FROM transaction_entry WHERE product_id IN (SELECT id FROM product WHERE product_code LIKE 'PERF-TEST-%')")
            sql.execute("DELETE FROM transaction WHERE inventory_id = ?", [facility?.inventory?.id])
            sql.execute("DELETE FROM inventory_item WHERE product_id IN (SELECT id FROM product WHERE product_code LIKE 'PERF-TEST-%')")
            sql.execute("DELETE FROM product WHERE product_code LIKE 'PERF-TEST-%'")

            // Bins first (location.parent_location_id -> location.id), then the facility itself (which still
            // references inventory via location.inventory_id - deleting the referencing row is always fine),
            // then the now-unreferenced inventory row (location.inventory_id -> inventory.id).
            sql.execute("DELETE FROM location WHERE parent_location_id = ?", [facility.id])
            sql.execute("DELETE FROM location WHERE id = ?", [facility.id])
            if (facility?.inventory?.id) {
                sql.execute("DELETE FROM inventory WHERE id = ?", [facility.inventory.id])
            }
        } finally {
            sql.close()
        }
    }

    /**
     * Bulk-inserts products/inventory items/transactions/transaction entries via plain batched JDBC
     * (not GORM save()), so that seeding 28,000 products doesn't itself become the bottleneck: GORM
     * save() would trigger Product/InventoryItem/Transaction's afterInsert hooks (each publishing a
     * Spring ApplicationEvent) once per row. A Hibernate StatelessSession was tried first, but GORM maps
     * dateCreated/lastUpdated as Hibernate "generated" columns populated only by that same event
     * mechanism, which StatelessSession explicitly skips - leaving those NOT NULL columns unset. Raw SQL
     * sidesteps both problems; synthetic ids only need to be unique among the rows we insert ourselves.
     */
    private void seedTransactionEntries(
            Location facility, List<Location> bins, Location targetBin, TransactionType transactionType
    ) {
        Timestamp now = new Timestamp(System.currentTimeMillis())
        List<String> productIds = []
        List<String> inventoryItemIds = []
        List<String> transactionIds = []

        Sql sql = new Sql(dataSource)
        try {
            sql.withTransaction {
                sql.withBatch(500, '''
                    INSERT INTO product (id, version, name, product_code, date_created, last_updated)
                    VALUES (?, 0, ?, ?, ?, ?)
                ''') { ps ->
                    (1..PRODUCT_COUNT).each { int i ->
                        String id = UUID.randomUUID().toString()
                        productIds << id
                        ps.addBatch([id, "Perf Test Product ${i}".toString(), "PERF-TEST-${i}".toString(), now, now])
                    }
                }

                sql.withBatch(500, '''
                    INSERT INTO inventory_item (id, version, product_id, lot_number, date_created, last_updated)
                    VALUES (?, 0, ?, ?, ?, ?)
                ''') { ps ->
                    (0..<PRODUCT_COUNT).each { int idx ->
                        String id = UUID.randomUUID().toString()
                        inventoryItemIds << id
                        ps.addBatch([id, productIds[idx], "LOT-${idx + 1}".toString(), now, now])
                    }
                }

                sql.withBatch(500, '''
                    INSERT INTO transaction (id, version, inventory_id, transaction_type_id, transaction_date, date_created, last_updated)
                    VALUES (?, 0, ?, ?, ?, ?, ?)
                ''') { ps ->
                    (0..<PRODUCT_COUNT).each { int idx ->
                        String id = UUID.randomUUID().toString()
                        transactionIds << id
                        ps.addBatch([id, facility.inventory.id, transactionType.id, now, now, now])
                    }
                }

                sql.withBatch(500, '''
                    INSERT INTO transaction_entry (id, version, transaction_id, product_id, inventory_item_id, bin_location_id, quantity)
                    VALUES (?, 0, ?, ?, ?, ?, ?)
                ''') { ps ->
                    (0..<PRODUCT_COUNT).each { int idx ->
                        int i = idx + 1
                        // Concentrate the first TARGET_BIN_ITEM_COUNT entries into targetBin so its real
                        // workload stays small while the rest of the facility scales to PRODUCT_COUNT.
                        // bins[0] is targetBin, so the "other bins" branch must never land on index 0
                        // (e.g. a plain i % bins.size() hits 0 - and thus targetBin again - every 50 rows).
                        Location binLocation = i <= TARGET_BIN_ITEM_COUNT ? targetBin : bins[1 + (i % (bins.size() - 1))]
                        ps.addBatch([UUID.randomUUID().toString(), transactionIds[idx], productIds[idx],
                                     inventoryItemIds[idx], binLocation.id, 10])
                    }
                }
            }
        } finally {
            sql.close()
        }
    }

    /**
     * Regression fixture for the cross-bin PRODUCT_INVENTORY reset bug: a product with an earlier CREDIT
     * recorded in targetBin, followed by a later facility-wide PRODUCT_INVENTORY reset recorded in a
     * different bin. A correct scoped query must honor the reset (the product must not appear in targetBin's
     * results), not just return whatever entries happen to live in targetBin.
     */
    private void seedResetRegressionProduct(Location facility, Location targetBin, Location otherBin,
                                             TransactionType creditType, TransactionType productInventoryType) {
        Sql sql = new Sql(dataSource)
        try {
            String productId = UUID.randomUUID().toString()
            String inventoryItemId = UUID.randomUUID().toString()
            Timestamp earlier = new Timestamp(System.currentTimeMillis() - 86_400_000) // 1 day ago
            Timestamp later = new Timestamp(System.currentTimeMillis())

            sql.execute("INSERT INTO product (id, version, name, product_code, date_created, last_updated) VALUES (?, 0, ?, ?, ?, ?)",
                    [productId, "Perf Test Reset Regression Product", RESET_REGRESSION_PRODUCT_CODE, later, later])
            sql.execute("INSERT INTO inventory_item (id, version, product_id, lot_number, date_created, last_updated) VALUES (?, 0, ?, ?, ?, ?)",
                    [inventoryItemId, productId, "RESET-REGRESSION-LOT", later, later])

            // Earlier CREDIT of +10 recorded in targetBin.
            String creditTransactionId = UUID.randomUUID().toString()
            sql.execute("INSERT INTO transaction (id, version, inventory_id, transaction_type_id, transaction_date, date_created, last_updated) VALUES (?, 0, ?, ?, ?, ?, ?)",
                    [creditTransactionId, facility.inventory.id, creditType.id, earlier, earlier, earlier])
            sql.execute("INSERT INTO transaction_entry (id, version, transaction_id, product_id, inventory_item_id, bin_location_id, quantity) VALUES (?, 0, ?, ?, ?, ?, ?)",
                    [UUID.randomUUID().toString(), creditTransactionId, productId, inventoryItemId, targetBin.id, 10])

            // Later facility-wide PRODUCT_INVENTORY reset recorded against a DIFFERENT bin.
            String resetTransactionId = UUID.randomUUID().toString()
            sql.execute("INSERT INTO transaction (id, version, inventory_id, transaction_type_id, transaction_date, date_created, last_updated) VALUES (?, 0, ?, ?, ?, ?, ?)",
                    [resetTransactionId, facility.inventory.id, productInventoryType.id, later, later, later])
            sql.execute("INSERT INTO transaction_entry (id, version, transaction_id, product_id, inventory_item_id, bin_location_id, quantity) VALUES (?, 0, ?, ?, ?, ?, ?)",
                    [UUID.randomUUID().toString(), resetTransactionId, productId, inventoryItemId, otherBin.id, 3])
        } finally {
            sql.close()
        }
    }

    def "scoped bin-location query matches and outperforms full-facility computation"() {
        given:
        sessionFactory.statistics.statisticsEnabled = true

        when: "old approach: compute quantities for the whole facility, then filter in memory"
        sessionFactory.statistics.clear()
        long startOld = System.currentTimeMillis()
        List<BinLocationItem> oldResult = inventoryService.getQuantityByBinLocation(facility).findAll {
            it.binLocation == targetBin
        }
        long oldMs = System.currentTimeMillis() - startOld
        long oldQueries = sessionFactory.statistics.queryExecutionCount

        and: "new approach: query scoped directly to the target bin"
        sessionFactory.statistics.clear()
        long startNew = System.currentTimeMillis()
        List<BinLocationItem> newResult = inventoryService.getQuantityByBinLocation(facility, targetBin)
        long newMs = System.currentTimeMillis() - startNew
        long newQueries = sessionFactory.statistics.queryExecutionCount

        then: "both approaches return the same set of bin contents, including around resets"
        assert toComparableKeys(oldResult) == toComparableKeys(newResult)
        assert oldResult.size() == TARGET_BIN_ITEM_COUNT

        and: "the reset-regression product does not leak its stale targetBin credit into either result"
        assert oldResult.every { it.product.productCode != RESET_REGRESSION_PRODUCT_CODE }
        assert newResult.every { it.product.productCode != RESET_REGRESSION_PRODUCT_CODE }

        and: "the scoped query is dramatically cheaper"
        println "BEFORE (full-facility, ${PRODUCT_COUNT} products): ${oldMs} ms, ${oldQueries} queries"
        println "AFTER  (scoped to one bin):                        ${newMs} ms, ${newQueries} queries"
        assert newQueries < oldQueries
        assert newMs <= oldMs
    }

    private static List<List> toComparableKeys(List<BinLocationItem> items) {
        return items.collect { [it.inventoryItem?.id, it.quantity] }.sort { it[0] }
    }
}
