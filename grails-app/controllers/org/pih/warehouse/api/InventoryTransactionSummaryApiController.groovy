package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.inventory.InventoryTransactionSummaryService
import org.pih.warehouse.inventory.InventoryTransactionsSummary
import org.pih.warehouse.report.CycleCountReportCommand

class InventoryTransactionSummaryApiController {

    InventoryTransactionSummaryService inventoryTransactionSummaryService

    def getInventoryTransactionsSummary(CycleCountReportCommand command) {
        if (!command.validate()) {
            throw new ValidationException("Invalid params", command.errors)
        }
        List<InventoryTransactionsSummary> inventoryTransactions = inventoryTransactionSummaryService.getInventoryTransactionsSummary(command)

        render([data: inventoryTransactions, totalCount: inventoryTransactions.totalCount] as JSON)
    }
}
