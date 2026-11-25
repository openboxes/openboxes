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
