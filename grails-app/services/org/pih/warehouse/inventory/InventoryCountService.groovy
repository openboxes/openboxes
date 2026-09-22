package org.pih.warehouse.inventory

import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import io.sentry.Sentry
import io.sentry.SentryLevel
import org.pih.warehouse.core.Constants

import javax.sql.DataSource

@Transactional
class InventoryCountService {

    DataSource dataSource

    /**
     * Baselines created by the old product inventory transaction migration are not real inventory counts, so they
     * are excluded from inventory_baseline_candidate.
     */
    private static final String MIGRATION_BASELINE_COMMENT =
            'Inventory baseline created during old product inventory transactions migration for products that had ' +
            'stock but no inventory baseline transaction as a most recent transaction'

    /**
     * The two candidate tables, keyed by table name. Only these names are ever interpolated into the statements
     * below - they are compile-time constants, never caller input.
     *
     * The selects have to stay in step with views/inventory-counts-helper-views.sql, which builds the same two
     * tables on first boot. refreshCandidateTables() reports to Sentry when they drift apart.
     */
    private static final Map<String, String> CANDIDATE_TABLES = [
            adjustment_candidate         : "t.transaction_type_id = '3'", // adjustments
            inventory_baseline_candidate : "t.transaction_type_id = '12'" + // baseline inventory transaction
                    " AND (t.comment <> '${MIGRATION_BASELINE_COMMENT}' OR t.comment IS NULL)".toString(),
    ].asImmutable()

    void refreshAdjustmentCandidatesView(Inventory inventory, List<String> productIds, String transactionId, Date transactionDate) {
        productIds.each {
            String dateString = transactionDate.format(Constants.ISO_DATE_TIME_FORMAT)
            Sql sql = new Sql(dataSource)
            Map<String, Object> params = [
                    transactionId: transactionId,
                    productId: it,
                    transactionDate: dateString,
                    inventoryId: inventory.id,
                    facilityId: inventory.warehouse.id
            ]
            String query = """
                INSERT INTO adjustment_candidate (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    facility_id
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :inventoryId,
                    :facilityId
                )
                -- The table carries a unique key on (transaction_id, product_id, facility_id). Re-inserting a row
                -- that is already there is a no-op rather than an error, so a retried save cannot roll back the
                -- caller's transaction.
                ON DUPLICATE KEY UPDATE transaction_date = VALUES(transaction_date)
            """
            sql.executeInsert(params, query)
        }
    }

    /**
     * @param transactionId
     * The adjustment_candidate table is maintained incrementally, so the rows of a deleted adjustment transaction
     * have to be removed from it, otherwise it keeps reporting the transaction as an inventory count.
     */
    void deleteAdjustmentCandidates(String transactionId) {
        Sql sql = new Sql(dataSource)
        Map<String, Object> params = [
                transactionId: transactionId
        ]
        String query = """
            DELETE FROM adjustment_candidate
            WHERE transaction_id = :transactionId
        """
        sql.executeUpdate(params, query)
    }

    /**
     * @param transactionIds
     * @param obsoleteProductId
     * @param primaryProductId
     * Since we move adjustments from the obsolete product to the primary, we have to update adjustment_candidate view
     * by swapping product_id for the primary product id for obsolete product's rows.
     */
    void refreshAdjustmentCandidatesViewAfterProductMerge(Set<String> transactionIds, String obsoleteProductId, String primaryProductId) {
        Sql sql = new Sql(dataSource)
        transactionIds.each {
            Map<String, Object> params = [
                    transactionId: it,
                    obsoleteProductId: obsoleteProductId,
                    primaryProductId: primaryProductId
            ]
            // A single transaction can touch both the obsolete and the primary product, in which case the row
            // for the primary product already exists and the UPDATE below would violate the unique key on
            // (transaction_id, product_id, facility_id). Drop the obsolete row in that case - the primary one
            // already represents the count for this transaction.
            String deleteCollidingQuery = """
                DELETE obsolete FROM adjustment_candidate obsolete
                JOIN adjustment_candidate primary_row
                  ON primary_row.transaction_id = obsolete.transaction_id
                  AND primary_row.facility_id = obsolete.facility_id
                  AND primary_row.product_id = :primaryProductId
                WHERE obsolete.transaction_id = :transactionId
                AND obsolete.product_id = :obsoleteProductId
            """
            sql.executeUpdate(params, deleteCollidingQuery)
            String query = """
                UPDATE adjustment_candidate
                SET product_id = :primaryProductId
                WHERE product_id = :obsoleteProductId
                AND transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
        }
    }

    /**
     * @param transactionIds
     * @param obsoleteProductId
     * @param primaryProductId
     * Since we move baseline transactions from the obsolete product to the primary, we have to update inventory_baseline_candidate view
     * by swapping product_id for the primary product id for obsolete product's rows.
     */
    void refreshInventoryBaselineCandidatesViewAfterProductMerge(Set<String> transactionIds, String obsoleteProductId, String primaryProductId) {
        Sql sql = new Sql(dataSource)
        transactionIds.each {
            Map<String, Object> params = [
                    transactionId: it,
                    obsoleteProductId: obsoleteProductId,
                    primaryProductId: primaryProductId
            ]
            // A single transaction can touch both the obsolete and the primary product, in which case the row
            // for the primary product already exists and the UPDATE below would violate the unique key on
            // (transaction_id, product_id, facility_id). Drop the obsolete row in that case - the primary one
            // already represents the count for this transaction.
            String deleteCollidingQuery = """
                DELETE obsolete FROM inventory_baseline_candidate obsolete
                JOIN inventory_baseline_candidate primary_row
                  ON primary_row.transaction_id = obsolete.transaction_id
                  AND primary_row.facility_id = obsolete.facility_id
                  AND primary_row.product_id = :primaryProductId
                WHERE obsolete.transaction_id = :transactionId
                AND obsolete.product_id = :obsoleteProductId
            """
            sql.executeUpdate(params, deleteCollidingQuery)
            String query = """
                UPDATE inventory_baseline_candidate
                SET product_id = :primaryProductId
                WHERE product_id = :obsoleteProductId
                AND transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
        }
    }

    void refreshInventoryBaselineCandidatesView(Inventory inventory, List<String> productIds, String transactionId, Date transactionDate) {
        productIds.each {
            String dateString = transactionDate.format(Constants.ISO_DATE_TIME_FORMAT)
            Sql sql = new Sql(dataSource)
            Map<String, Object> params = [
                    transactionId: transactionId,
                    productId: it,
                    transactionDate: dateString,
                    inventoryId: inventory.id,
                    facilityId: inventory.warehouse.id
            ]
            String query = """
                INSERT INTO inventory_baseline_candidate (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    facility_id
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :inventoryId,
                    :facilityId
                )
                -- The table carries a unique key on (transaction_id, product_id, facility_id). Re-inserting a row
                -- that is already there is a no-op rather than an error, so a retried save cannot roll back the
                -- caller's transaction.
                ON DUPLICATE KEY UPDATE transaction_date = VALUES(transaction_date)
            """
            sql.executeInsert(params, query)
        }
    }

    /**
     * @param transactionId
     * The inventory_baseline_candidate table is maintained incrementally, so the rows of a deleted baseline
     * transaction have to be removed from it, otherwise it keeps reporting the transaction as an inventory count.
     */
    void deleteInventoryBaselineCandidates(String transactionId) {
        Sql sql = new Sql(dataSource)
        Map<String, Object> params = [
                transactionId: transactionId
        ]
        String query = """
            DELETE FROM inventory_baseline_candidate
            WHERE transaction_id = :transactionId
        """
        sql.executeUpdate(params, query)
    }

    /**
     * @param transactionId
     * Product inventory transactions are deprecated, so the product_inventory_candidate table never receives new
     * rows, which is why it has no refresh counterpart. Its legacy rows still have to be removed when their
     * transaction is deleted, otherwise it keeps reporting the transaction as an inventory count.
     */
    void deleteProductInventoryCandidates(String transactionId) {
        Sql sql = new Sql(dataSource)
        Map<String, Object> params = [
                transactionId: transactionId
        ]
        String query = """
            DELETE FROM product_inventory_candidate
            WHERE transaction_id = :transactionId
        """
        sql.executeUpdate(params, query)
    }

    /**
     * Rebuilds both candidate tables from transaction_entry and reconciles the live tables against the result.
     *
     * The tables are created once, on first boot, by views/inventory-counts-helper-views.sql and are kept up to
     * date incrementally by the refresh/delete methods above. This is the periodic full rebuild that corrects any
     * drift between the incremental path and what transaction_entry actually says.
     *
     * Rows are reconciled with DELETE/INSERT/UPDATE against a freshly built temp table rather than swapping the
     * live table out. That keeps all DDL on the temp table, so the live table is never dropped, renamed or
     * truncated underneath inventory_counts, and the size of the delta is exactly the drift we want to report.
     *
     * Not transactional: the temp table DDL would implicitly commit an enclosing transaction anyway.
     */
    @NotTransactional
    void refreshCandidateTables() {
        CANDIDATE_TABLES.each { String table, String typeCriteria ->
            Map<String, Integer> delta = refreshCandidateTable(table, typeCriteria)
            reportDrift(table, delta)
        }
    }

    /**
     * @return the number of rows inserted, deleted and updated to bring the live table back in line with a full
     * rebuild. All three are zero when the incremental path has kept up, which is the expected case.
     */
    private Map<String, Integer> refreshCandidateTable(String table, String typeCriteria) {
        Sql sql = new Sql(dataSource)
        String tempTable = "${table}_tmp"
        try {
            sql.execute("DROP TABLE IF EXISTS ${tempTable}".toString())
            // Same shape and filtering as views/inventory-counts-helper-views.sql. The GROUP BY collapses the
            // per-lot inventory_item rows down to one row per transaction/product/facility.
            sql.execute("""
                CREATE TABLE ${tempTable} AS
                    SELECT te.transaction_id AS transaction_id,
                           ii.product_id AS product_id,
                           t.transaction_date,
                           t.inventory_id,
                           facility.id AS facility_id
                    FROM transaction_entry te
                             JOIN transaction t ON te.transaction_id = t.id
                             JOIN inventory_item ii ON ii.id = te.inventory_item_id
                             JOIN location facility ON facility.inventory_id = t.inventory_id
                    WHERE ${typeCriteria}
                    GROUP BY
                        te.transaction_id,
                        ii.product_id,
                        t.transaction_date,
                        t.inventory_id,
                        facility.id
            """.toString())
            sql.execute("""
                ALTER TABLE ${tempTable}
                    ADD UNIQUE INDEX uq_transaction_product_facility (transaction_id, product_id, facility_id)
            """.toString())

            // Rows the incremental path left behind: the transaction no longer qualifies, or never did.
            int deleted = sql.executeUpdate("""
                DELETE live FROM ${table} live
                LEFT JOIN ${tempTable} fresh
                  ON fresh.transaction_id = live.transaction_id
                  AND fresh.product_id = live.product_id
                  AND fresh.facility_id = live.facility_id
                WHERE fresh.transaction_id IS NULL
            """.toString())

            // Rows the incremental path never wrote.
            int inserted = sql.executeUpdate("""
                INSERT INTO ${table} (transaction_id, product_id, transaction_date, inventory_id, facility_id)
                SELECT fresh.transaction_id, fresh.product_id, fresh.transaction_date, fresh.inventory_id,
                       fresh.facility_id
                FROM ${tempTable} fresh
                LEFT JOIN ${table} live
                  ON live.transaction_id = fresh.transaction_id
                  AND live.product_id = fresh.product_id
                  AND live.facility_id = fresh.facility_id
                WHERE live.transaction_id IS NULL
            """.toString())

            // Rows that exist on both sides but disagree. transaction_date matters most: inventory_counts pairs a
            // baseline with its adjustment on an exact one second difference, so a stale date breaks the pairing.
            int updated = sql.executeUpdate("""
                UPDATE ${table} live
                JOIN ${tempTable} fresh
                  ON fresh.transaction_id = live.transaction_id
                  AND fresh.product_id = live.product_id
                  AND fresh.facility_id = live.facility_id
                SET live.transaction_date = fresh.transaction_date,
                    live.inventory_id = fresh.inventory_id
                WHERE live.transaction_date <> fresh.transaction_date
                   OR live.inventory_id <> fresh.inventory_id
            """.toString())

            return [inserted: inserted, deleted: deleted, updated: updated]
        } finally {
            try {
                sql.execute("DROP TABLE IF EXISTS ${tempTable}".toString())
            } catch (Exception e) {
                // Never let temp table cleanup mask the real failure - the next run drops it anyway
                log.warn("Could not drop ${tempTable}: ${e.message}")
            } finally {
                sql.close()
            }
        }
    }

    /**
     * Any non-zero delta means the incremental path and transaction_entry had diverged. The rebuild has already
     * corrected it, so this is a report rather than an alarm, but it is the only signal that the incremental path
     * has a bug - without it the drift is silently repaired every night and nobody finds out.
     *
     * Captured explicitly rather than left to log.warn: the Sentry logback appender only promotes ERROR and above
     * to issues, so a warning would arrive as a breadcrumb and never surface on its own.
     */
    private void reportDrift(String table, Map<String, Integer> delta) {
        if (delta.values().every { it == 0 }) {
            log.info("No drift in ${table}, incremental updates are in step with transaction_entry")
            return
        }
        String message = "Inventory count candidate drift corrected in ${table}: " +
                "${delta.inserted} inserted, ${delta.deleted} deleted, ${delta.updated} updated"
        log.warn(message)
        Sentry.withScope { scope ->
            scope.level = SentryLevel.WARNING
            scope.setTag("table", table)
            delta.each { String key, Integer value -> scope.setExtra("rows_${key}".toString(), value.toString()) }
            Sentry.captureMessage(message)
        }
    }
}
