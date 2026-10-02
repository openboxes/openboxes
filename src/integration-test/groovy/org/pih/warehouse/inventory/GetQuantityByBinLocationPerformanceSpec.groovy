package org.pih.warehouse.inventory

import java.sql.Timestamp

import groovy.sql.Sql
import spock.lang.Shared

import org.pih.warehouse.common.base.IntegrationSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location

/**
 * Benchmarks InventoryService#getQuantityByBinLocation(Location, Location) against a facility-scale
 * dataset (comparable to the ~28,000 products seen in production) to demonstrate the performance
 * difference between computing stock for the whole facility (old behaviour, still reachable via the
 * single-arg getQuantityByBinLocation(Location) used by ReportController) versus querying a single
 * bin location directly (current behaviour of the two-arg overload).
 */
class GetQuantityByBinLocationPerformanceSpec extends IntegrationSpec {

    static final int PRODUCT_COUNT = 28_000
    static final int BIN_COUNT = 50
    static final int TARGET_BIN_ITEM_COUNT = 50

    def inventoryService
    def sessionFactory
    def dataSource

    @Shared
    Location facility

    @Shared
    Location targetBin

    /**
     * Creates the facility/bins and bulk-seeds transaction entries.
     *
     * Plain GORM calls made directly from test code (as opposed to calls into a @Transactional Grails
     * service) have no Hibernate session bound to the thread, so domain saves/reads here must be wrapped
     * in Location.withNewTransaction{}. We deliberately use withNewTransaction (which commits as soon as
     * the closure returns) rather than annotating the feature method with @Transactional: the latter would
     * only commit once the whole test finished, which is too late - the old code path spawns its own
     * worker threads (separate DB connections) that need the seeded data to already be committed.
     */
    private void seedFixtures() {
        TransactionType transactionType
        List<Location> bins

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

            // Hibernate's lazy-association bytecode enhancement nulls out to-one associations like
            // "inventory" once the owning entity is detached (session/transaction closed). Re-fetch with
            // an explicit join so inventory is eagerly loaded and survives detachment - the same pattern
            // LocationTestBuilder.findOrBuildMainFacility() uses for exactly this reason.
            facility = Location.createCriteria().get {
                eq("id", facility.id)
                join("inventory")
            } as Location
        }

        seedTransactionEntries(facility, bins, targetBin, transactionType)
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

    def "scoped bin-location query matches and outperforms full-facility computation"() {
        given:
        seedFixtures()
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

        then: "both approaches return the same set of bin contents"
        toComparableKeys(oldResult) == toComparableKeys(newResult)
        oldResult.size() == TARGET_BIN_ITEM_COUNT

        and: "the scoped query is dramatically cheaper"
        println "BEFORE (full-facility, ${PRODUCT_COUNT} products): ${oldMs} ms, ${oldQueries} queries"
        println "AFTER  (scoped to one bin):                        ${newMs} ms, ${newQueries} queries"
        newQueries < oldQueries
        newMs <= oldMs
    }

    private static List<List> toComparableKeys(List<BinLocationItem> items) {
        return items.collect { [it.inventoryItem?.id, it.quantity] }.sort { it[0] }
    }
}
