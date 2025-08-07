package org.pih.warehouse.inventory

import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.pih.warehouse.report.CycleCountReportCommand

class InventoryTransactionSummaryService {

    List<InventoryTransactionsSummary> getInventoryTransactionsSummary(CycleCountReportCommand command) {
        List<InventoryTransactionsSummary> inventoryTransactions = InventoryTransactionsSummary.createCriteria().list(command.paginationParams) {
            // Join transaction and transaction entries to reduce n+1 queries while getting comments out of entries
            createAlias("product", "p", JoinType.INNER_JOIN)
            createAlias("transaction", "t", JoinType.INNER_JOIN)
            createAlias("t.transactionEntries", "te", JoinType.INNER_JOIN)

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

            // Return distinct values (duplicates might be returned while joining the transactionEntries)
            setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        }

        // Since setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY) has never worked properly with pagination
        // (it returns totalCount including the eventual duplicates), make another query with the same filter clauses
        // to calculate the totalCount and override it
        Long inventoryTransactionsCount = InventoryTransactionsSummary.createCriteria().get {
            projections {
                countDistinct("id")
            }

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
        }
        // Override the original totalCount with the correct value
        inventoryTransactions.totalCount = inventoryTransactionsCount

        return inventoryTransactions
    }
}
