package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import org.pih.warehouse.PaginatedList
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.CycleCountReportCommand

import javax.sql.DataSource
import java.sql.Timestamp

@Transactional
class InventoryTransactionSummaryService {

    DataSource dataSource
    SessionFactory sessionFactory

    NativeQuery buildInventoryTransactionsQuery(String queryString, CycleCountReportCommand command, boolean paginate) {
        NativeQuery query = sessionFactory
            .currentSession
            .createNativeQuery(queryString)
            .setParameter("facility", command.facility.id)
            .setParameter("startDate", command.startDate)
            .setParameter("endDate", command.endDate)
        if (command.products) {
            query.setParameterList("products", command.products.id)
        }
        if (paginate) {
            query.setFirstResult(command.offset).setMaxResults(command.max)
        }

        return query
    }

    PaginatedList<InventoryTransactionsSummary> getInventoryTransactionsSummary(CycleCountReportCommand command) {
        String inventoryTransactionsQuery = """
            SELECT
                transaction.id AS transactionId,
                product.id as productId,
                facility.id as facilityId,
                -- If product inventory summary id is null, it means, that the transaction we query against is a single adjustment
                -- to calculate quantity before for such adjustment (since it is not associated with a baseline transaction),
                -- we need to sum quantity from the latest baseline before this adjustment,
                -- and check if there are more "alone" adjustment between such baseline and the current calculated adjustment
                CASE
                    WHEN pis.transaction_id IS NULL THEN (
                          COALESCE((
                              -- In 99% scenarios there is going to be only one baseline with a particular transaction_date for a product
                              -- but due to some stale old data, where we could experience duplicate baselines.
                              -- So we take MAX here for two reasons:
                              -- 1) to avoid exception with "Subquery did not return unique result"
                              -- 2) we don't take SUM, but MAX not to double the result
                              SELECT MAX(quantity_balance)
                              FROM product_inventory_summary pis
                              WHERE pis.product_code = product.product_code
                                AND pis.facility_id = facility.id
                                -- Find the latest baseline before the calculated adjustment
                                AND pis.baseline_transaction_date = (
                                  SELECT MAX(pis2.baseline_transaction_date)
                                  FROM product_inventory_summary pis2
                                  WHERE pis2.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                                    AND pis2.facility_id = facility.id
                                    AND pis2.baseline_transaction_date < transaction.transaction_date
                                )
                          ), 0)
                        +
                         COALESCE((
                             -- Sum the quantity of all adjustments between the calculated adjustment and the latest baseline
                             SELECT SUM(quantity_sum)
                             FROM inventory_movement_summary
                             WHERE inventory_movement_summary.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                             AND inventory_movement_summary.inventory_id = transaction.inventory_id
                             -- Condition to find the date range between the latest baseline and the current calculated adjustment
                             AND (
                                 (SELECT MAX(baseline_transaction_date)
                                  FROM product_inventory_summary
                                  WHERE product_inventory_summary.product_code = product.product_code
                                    AND product_inventory_summary.facility_id = facility.id
                                    AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                                 ) IS NULL
                                     OR inventory_movement_summary.transaction_date > (
                                     SELECT MAX(baseline_transaction_date)
                                     FROM product_inventory_summary
                                     WHERE product_inventory_summary.product_code = product.product_code
                                       AND product_inventory_summary.facility_id = facility.id
                                       AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                                     )
                             )
            
                             AND inventory_movement_summary.transaction_date < transaction.transaction_date
                         ), 0)
                    )
                    -- If we have an associated baseline, we just take its quantity, we don't need to search for anything "between"
                    ELSE COALESCE(pis.quantity_balance, 0)
                END AS quantityBefore,
                 -- The condition for calculate quantity after is just quantity before + current sum of adjustment
                COALESCE((
                     SELECT MAX(quantity_balance)
                     FROM product_inventory_summary pis
                     WHERE pis.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                       AND pis.facility_id = facility.id
                       AND pis.baseline_transaction_date = (
                         SELECT MAX(pis2.baseline_transaction_date)
                         FROM product_inventory_summary pis2
                         WHERE pis2.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                           AND pis2.facility_id = facility.id
                           AND pis2.baseline_transaction_date < transaction.transaction_date
                       )
                 ), 0)
                 +
                COALESCE((
                    SELECT SUM(quantity_sum)
                    FROM inventory_movement_summary
                    WHERE inventory_movement_summary.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                      AND inventory_movement_summary.inventory_id = transaction.inventory_id
                      AND (
                        (SELECT MAX(baseline_transaction_date)
                         FROM product_inventory_summary
                         WHERE product_inventory_summary.product_code = product.product_code
                           AND product_inventory_summary.facility_id = facility.id
                           AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                        ) IS NULL
                            OR inventory_movement_summary.transaction_date > (
                            SELECT MAX(baseline_transaction_date)
                            FROM product_inventory_summary
                            WHERE product_inventory_summary.product_code = product.product_code
                              AND product_inventory_summary.facility_id = facility.id
                              AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                            )
                        )
                      AND inventory_movement_summary.transaction_date < transaction.transaction_date
                    ), 0)
                + SUM(transaction_entry.quantity) AS quantityAfter,
                SUM(transaction_entry.quantity) AS quantityDifference,
                transaction.transaction_date as dateRecorded,
                transaction.created_by_id as recordedById,
                pis.transaction_id as baselineTransactionId
            FROM transaction_entry
            JOIN transaction ON transaction.id = transaction_entry.transaction_id
            JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
            JOIN product ON inventory_item.product_id = product.id
            JOIN location facility ON facility.inventory_id = transaction.inventory_id
            LEFT JOIN product_inventory_summary pis
               ON pis.product_id = inventory_item.product_id
               AND pis.facility_id = facility.id
               -- An adjustment is treated as associated with the baseline if the time diff between them is 1 second (baseline is created 1 second before the adjustment)
               AND TIMESTAMPDIFF(SECOND, pis.baseline_transaction_date, transaction.transaction_date) = 1
            WHERE transaction.transaction_type_id = '3' -- Adjustments
            AND facility.id = :facility 
            AND transaction.transaction_date BETWEEN :startDate AND :endDate 
            ${command.products ? 'AND product.id IN (:products)' : ''}
            GROUP BY transaction.id, inventory_item.product_id
            ORDER BY transaction.transaction_date DESC
        """

        List<InventoryTransactionsSummary> inventoryTransactions = buildInventoryTransactionsQuery(inventoryTransactionsQuery, command, true)
            .list()
            .collect {
                new InventoryTransactionsSummary(
                        transaction: Transaction.read(it[0]),
                        product: Product.read(it[1]),
                        facility: Location.read(it[2]),
                        quantityBefore: it[3],
                        quantityAfter: it[4],
                        quantityDifference: it[5],
                        dateRecorded: it[6],
                        recordedBy: User.read(it[7]),
                        baselineTransaction: Transaction.read(it[8])

                )}

        String totalCountQuery = """
            SELECT
                COUNT(*) as totalCount
            FROM transaction_entry
            JOIN transaction ON transaction.id = transaction_entry.transaction_id
            JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
            JOIN product ON inventory_item.product_id = product.id
            JOIN location facility ON facility.inventory_id = transaction.inventory_id
            LEFT JOIN product_inventory_summary pis
               ON pis.product_id = inventory_item.product_id
               AND pis.facility_id = facility.id
               -- An adjustment is treated as associated with the baseline if the time diff between them is 1 second (baseline is created 1 second before the adjustment)
               AND TIMESTAMPDIFF(SECOND, pis.baseline_transaction_date, transaction.transaction_date) = 1
            WHERE transaction.transaction_type_id = '3' -- Adjustments
            AND facility.id = :facility 
            AND transaction.transaction_date BETWEEN :startDate AND :endDate 
            ${command.products ? 'AND product.id IN (:products)' : ''}
            GROUP BY transaction.id, inventory_item.product_id
            
        """

        int totalCount = buildInventoryTransactionsQuery(totalCountQuery, command, false)
                .list()
                .size()

        return new PaginatedList<InventoryTransactionsSummary>(inventoryTransactions, totalCount)
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
