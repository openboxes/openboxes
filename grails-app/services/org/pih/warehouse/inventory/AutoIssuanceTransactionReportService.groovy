package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import org.pih.warehouse.PaginatedList
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.report.AutoIssuanceTransactionsReportCommand

import java.sql.Timestamp

@Transactional(readOnly = true)
class AutoIssuanceTransactionReportService {

    SessionFactory sessionFactory
    ProductService productService
    ProductAvailabilityService productAvailabilityService

    NativeQuery buildAutoIssuanceTransactionsQuery(String queryString, AutoIssuanceTransactionsReportCommand command, boolean paginate) {
        NativeQuery query = sessionFactory
                .currentSession
                .createNativeQuery(queryString)
                .setParameter("inventoryId", command.facility.inventory.id)
                .setParameter("transactionTypeId", Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
                .setParameter("startDate", command.startDate)
                .setParameter("endDate", command.endDate)
        if (command.products) {
            query.setParameterList("products", command.products.id)
        }
        if (command.binLocations) {
            query.setParameterList("binLocations", command.binLocations.id)
        }
        if (paginate) {
            query.setFirstResult(command.offset).setMaxResults(command.max)
        }
        return query
    }

    /**
     * Rows are outbound (TRANSFER_OUT) transaction entries for requisitions that were auto-issued
     * (autoIssuanceRequested), grouped by product + bin location + transaction so that multiple
     * lots of the same product/bin picked within a single transaction collapse into one row.
     */
    PaginatedList<AutoIssuanceTransactionDto> getAutoIssuanceTransactions(AutoIssuanceTransactionsReportCommand command) {
        String queryString = """
            SELECT
                product.id                      AS productId,
                product.product_code            AS productCode,
                product.name                    AS productName,
                bin_location.id                 AS binLocationId,
                bin_location.name               AS binLocationName,
                transaction.id                  AS transactionId,
                transaction.transaction_number  AS transactionNumber,
                transaction.transaction_date    AS transactionDate,
                requisition.id                  AS requisitionId,
                requisition.request_number      AS requisitionNumber,
                SUM(transaction_entry.quantity) AS quantityReduced
            FROM transaction_entry
            JOIN transaction ON transaction.id = transaction_entry.transaction_id
            JOIN requisition ON requisition.id = transaction.requisition_id
            JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
            JOIN product ON product.id = inventory_item.product_id
            LEFT JOIN location bin_location ON bin_location.id = transaction_entry.bin_location_id
            WHERE transaction.transaction_type_id = :transactionTypeId
              AND transaction.inventory_id = :inventoryId
              AND requisition.auto_issuance_requested = true
              AND transaction.transaction_date BETWEEN :startDate AND :endDate
              ${command.products ? 'AND product.id IN (:products)' : ''}
              ${command.binLocations ? 'AND bin_location.id IN (:binLocations)' : ''}
            GROUP BY product.id, bin_location.id, transaction.id, requisition.id
            ORDER BY ${resolveOrderByClause(command.sort, command.order)}
        """

        List<Object[]> rows = buildAutoIssuanceTransactionsQuery(queryString, command, true).list() as List<Object[]>

        List<String> productIds = rows.collect { (String) it[0] }.unique()
        List<String> binLocationIds = rows.collect { (String) it[3] }.findAll().unique()

        Map<ProductAndBinKey, Timestamp> dateLastCountedByProductAndBin =
                productService.getDateLastCountedByProductAndBin(command.facility, productIds, binLocationIds)

        Map<ProductAndBinKey, Integer> quantityOnHandByProductAndBin =
                productAvailabilityService.getQuantityOnHandByProductAndBin(command.facility, productIds)

        List<AutoIssuanceTransactionDto> data = rows.collect { Object[] row ->
            String productId = (String) row[0]
            String binLocationId = (String) row[3]
            ProductAndBinKey productAndBinKey = new ProductAndBinKey(productId, binLocationId)
            new AutoIssuanceTransactionDto(
                    productId: productId,
                    productCode: (String) row[1],
                    productName: (String) row[2],
                    binLocationId: binLocationId,
                    binLocationName: (String) row[4],
                    transactionId: (String) row[5],
                    transactionNumber: (String) row[6],
                    transactionDate: (Date) row[7],
                    requisitionId: (String) row[8],
                    requisitionNumber: (String) row[9],
                    quantityReduced: ((Number) row[10]).intValue(),
                    dateLastCounted: dateLastCountedByProductAndBin[productAndBinKey],
                    quantityOnHand: quantityOnHandByProductAndBin[productAndBinKey] ?: 0,
            )
        }

        String totalCountQueryString = """
            SELECT product.id
            FROM transaction_entry
            JOIN transaction ON transaction.id = transaction_entry.transaction_id
            JOIN requisition ON requisition.id = transaction.requisition_id
            JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
            JOIN product ON product.id = inventory_item.product_id
            LEFT JOIN location bin_location ON bin_location.id = transaction_entry.bin_location_id
            WHERE transaction.transaction_type_id = :transactionTypeId
              AND transaction.inventory_id = :inventoryId
              AND requisition.auto_issuance_requested = true
              AND transaction.transaction_date BETWEEN :startDate AND :endDate
              ${command.products ? 'AND product.id IN (:products)' : ''}
              ${command.binLocations ? 'AND bin_location.id IN (:binLocations)' : ''}
            GROUP BY product.id, bin_location.id, transaction.id, requisition.id
        """
        int totalCount = buildAutoIssuanceTransactionsQuery(totalCountQueryString, command, false).list().size()

        return new PaginatedList<AutoIssuanceTransactionDto>(data, totalCount)
    }

    /**
     * Whitelists which columns/direction can be interpolated into the native SQL ORDER BY clause -
     * command.sort/command.order must never be interpolated directly, since this is raw SQL, not a
     * Criteria/HQL property reference. The "product"/"binLocation" cases must match the column ids
     * the frontend sends as `sort` (see autoIssuanceTransactionReportColumn.js) - there's no shared
     * constant between frontend and backend, so a rename on one side silently breaks sorting.
     */
    private static String resolveOrderByClause(String sort, String order) {
        String direction = (order?.toLowerCase() == "desc") ? "DESC" : "ASC"
        // transaction.id is appended as a tiebreaker
        switch (sort) {
            case "product":
                return "product.name ${direction}, product.product_code ${direction}, transaction.id ASC"
            case "binLocation":
                return "bin_location.name ${direction}, transaction.id ASC"
            default:
                return "transaction.transaction_date DESC, transaction.id ASC"
        }
    }
}
