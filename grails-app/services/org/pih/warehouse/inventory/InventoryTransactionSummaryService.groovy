package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.CycleCountReportCommand

import javax.sql.DataSource
import java.sql.Timestamp

@Transactional
class InventoryTransactionSummaryService {

    DataSource dataSource

    List<InventoryTransactionsSummary> getInventoryTransactionsSummary(CycleCountReportCommand command) {
        List<InventoryTransactionsSummary> inventoryTransactions = InventoryTransactionsSummary.createCriteria().list(command.paginationParams) {
            eq("facility", command.facility)
            if (command.startDate) {
                gte("dateRecorded", command.startDate)
            }
            if (command.endDate) {
                lte("dateRecorded", command.endDate)
            }
            if (command.products) {
                "in"("product", command.products)
            }
            order("dateRecorded", "desc")
        }

        return inventoryTransactions
    }

    void refreshInventoryMovementSummaryView(RefreshInventoryTransactionsSummaryEvent event) {
        Sql sql = new Sql(dataSource)
        if (event.isDelete) {
            Map<String, Object> params = [
                    transactionId: event.transactionId
            ]
            String query = """
                DELETE FROM inventory_movement_summary WHERE transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
            return
        }
        event.entriesByProduct.each { product, entries ->
            Timestamp transactionDate = new Timestamp(event.transactionDate.time)

            Map<String, Object> params = [
                    transactionId: event.transactionId,
                    productId: product.id,
                    transactionDate: transactionDate,
                    inventoryId: event.inventoryId,
                    productCode: product.productCode,
                    quantitySum: entries.sum { entry ->
                        event.transactionTypeId == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID ? -entry.quantity : entry.quantity
                    }
            ]
            String query = """
                INSERT INTO inventory_movement_summary (
                    transaction_id,
                    product_id,
                    transaction_date,
                    inventory_id,
                    product_code,
                    quantity_sum
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :inventoryId,
                    :productCode,
                    :quantitySum
                )
            """
            sql.executeInsert(params, query)
        }
    }

    /**
     *
     * @param transactionIds
     * @param obsoleteProduct
     * @param primaryProduct
     * Since we move adjustments/transfer in/out from the obsolete product to the primary, we have to update inventory_movement_summary view
     * by swapping product_id (and product_code!!!) for the primary product id (and primary product code) for obsolete product's rows.
     */
    void refreshInventoryMovementSummaryViewAfterProductMerge(Set<String> transactionIds, Product obsoleteProduct, Product primaryProduct) {
        Sql sql = new Sql(dataSource)
        transactionIds.each {
            Map<String, Object> params = [
                    transactionId: it,
                    obsoleteProductId: obsoleteProduct.id,
                    obsoleteProductCode: obsoleteProduct.productCode,
                    primaryProductId: primaryProduct.id,
                    primaryProductCode: primaryProduct.productCode,
            ]
            String query = """
                UPDATE inventory_movement_summary 
                SET product_id = :primaryProductId, product_code = :primaryProductCode
                WHERE product_id = :obsoleteProductId 
                AND product_code = :obsoleteProductCode
                AND transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
        }
    }

    /**
     *
     * @param transactionIds
     * @param obsoleteProduct
     * @param primaryProduct
     * Since we move baseline transactions from the obsolete product to the primary, we have to update product_inventory_summary view
     * by swapping product_id (and product_code!!!) for the primary product id (and primary product code) for obsolete product's rows.
     * We might have a baseline that points to both primary and obsolete product (e.g. the one created during product merge), and since
     * the view is grouped by transaction and product.id, we mustn't update rows for obsolete products and swap them to primary product,
     * as we would have two rows for transaction X and product Y, so to "mimic" grouping while refreshing the view, we want to search for the
     * "common" transaction for primary and obsolete product, and just update the primary row for this transaction by adding the quantity balance
     * from the obsolete row.
     */
    void refreshProductInventorySummaryViewAfterProductMerge(Set<String> transactionIds, Product obsoleteProduct, Product primaryProduct) {
        Sql sql = new Sql(dataSource)
        transactionIds.each {
            Map<String, Object> params = [
                    transactionId: it,
                    obsoleteProductId: obsoleteProduct.id,
                    obsoleteProductCode: obsoleteProduct.productCode,
                    primaryProductId: primaryProduct.id,
                    primaryProductCode: primaryProduct.productCode,
            ]
            // First check if the transaction is a "common" transaction (it means - the transaction contains both entries from obsolete and primary product)
            Map<String, Object> productInventorySummaryRowForPrimaryProduct = sql.firstRow(params, """
                    SELECT transaction_id as transactionId, 
                           product_id as productId, 
                           product_code as productCode, 
                           quantity_balance as quantityBalance
                    FROM product_inventory_summary
                    WHERE product_id = :primaryProductId
                    AND product_code = :primaryProductCode
                    AND transaction_id = :transactionId
                   
            """)
            Map<String, Object> productInventorySummaryRowForObsoleteProduct = sql.firstRow(params, """
                    SELECT transaction_id as transactionId, 
                           product_id as productId, 
                           product_code as productCode, 
                           quantity_balance as quantityBalance
                    FROM product_inventory_summary
                    WHERE product_id = :obsoleteProductId
                    AND product_code = :obsoleteProductCode
                    AND transaction_id = :transactionId
                  
            """)
            boolean commonTransaction = productInventorySummaryRowForPrimaryProduct && productInventorySummaryRowForObsoleteProduct
            // If both queries from above return a common transaction, instead of duplicating a row for transaction X and product Y (primary)
            // We want to just sum the obsoleteProduct.quantityBalance + primaryProduct.quantityBalance and update primary product's row
            if (commonTransaction) {
                params.put("quantityBalance",
                    productInventorySummaryRowForPrimaryProduct.quantityBalance + productInventorySummaryRowForObsoleteProduct.quantityBalance)
            }
            // If it's a common transaction, just update the quantity balance for primary product's row, otherwise swap the product id and code from obsolete to primary
            String query = (commonTransaction) ? """
                UPDATE product_inventory_summary 
                SET quantity_balance = :quantityBalance
                WHERE product_id = :primaryProductId 
                AND product_code = :primaryProductCode
                AND transaction_id = :transactionId
            """  :
            """
                UPDATE product_inventory_summary 
                SET product_id = :primaryProductId, product_code = :primaryProductCode
                WHERE product_id = :obsoleteProductId 
                AND product_code = :obsoleteProductCode
                AND transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)

            // In the end remove the outdated "common" transaction (row for obsolete product, that we took the quantity balance from)
            // This step is a bit overcautious, as an obsolete product is probably not supposed to be used in the future, but for clarity - clear stale data
            if (commonTransaction) {
                String deleteQuery = """
                    DELETE FROM product_inventory_summary 
                    WHERE transaction_id = :transactionId
                    AND product_id = :obsoleteProductId
                    AND product_code = :obsoleteProductCode
                """
                sql.executeUpdate(params, deleteQuery)
            }
        }
    }

    void refreshProductInventorySummaryView(RefreshInventoryTransactionsSummaryEvent event) {
        Sql sql = new Sql(dataSource)
        if (event.isDelete) {
            Map<String, Object> params = [
                    transactionId: event.transactionId
            ]
            String query = """
                DELETE FROM product_inventory_summary WHERE transaction_id = :transactionId
            """
            sql.executeUpdate(params, query)
            return
        }
        Location facility = Location.findByInventory(Inventory.read(event.inventoryId))
        event.entriesByProduct.each { product, entries ->
            Timestamp transactionDate = new Timestamp(event.transactionDate.time)

            Map<String, Object> params = [
                    transactionId: event.transactionId,
                    productId: product.id,
                    transactionDate: transactionDate,
                    inventoryId: event.inventoryId,
                    productCode: product.productCode,
                    quantityBalance: entries.sum { entry -> entry.quantity },
                    facilityId: facility.id,
            ]
            String query = """
                INSERT INTO product_inventory_summary (
                    transaction_id,
                    product_id,
                    baseline_transaction_date,
                    facility_id,
                    product_code,
                    quantity_balance
                )
                VALUES (
                    :transactionId,
                    :productId,
                    :transactionDate,
                    :facilityId,
                    :productCode,
                    :quantityBalance
                )
            """
            sql.executeInsert(params, query)
        }
    }
}
