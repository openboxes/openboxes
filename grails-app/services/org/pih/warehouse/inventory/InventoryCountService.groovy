package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.pih.warehouse.core.Constants

import javax.sql.DataSource

@Transactional
class InventoryCountService {

    DataSource dataSource

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
}
