package org.pih.warehouse.inventory

import org.pih.warehouse.report.CycleCountReportCommand

class InventoryTransactionSummaryService {

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
}
