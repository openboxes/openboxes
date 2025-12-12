package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.PaginatedList
import org.pih.warehouse.core.date.DateFormatter
import org.pih.warehouse.data.DataService
import org.pih.warehouse.inventory.InventoryTransactionSummaryService
import org.pih.warehouse.inventory.InventoryTransactionsSummary
import org.pih.warehouse.report.CycleCountReportCommand

class InventoryTransactionSummaryApiController {

    InventoryTransactionSummaryService inventoryTransactionSummaryService
    DataService dataService
    DateFormatter dateFormatter

    def getInventoryTransactionsSummary(CycleCountReportCommand command) {
        if (!command.validate()) {
            throw new ValidationException("Invalid params", command.errors)
        }

        // If we specify a format=csv we want to download everything
        if (params.format == 'csv') {
            command.max = -1
        }

        PaginatedList<InventoryTransactionsSummary> inventoryTransactions = inventoryTransactionSummaryService.getInventoryTransactionsSummary(command)

        if (params.format == 'csv') {
            String text = dataService.generateCsv(inventoryTransactions*.toCsv())
            String fileName = "inventory-transaction-summary-${command.facility}-${dateFormatter.formatCurrentDateForFileName()}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
            render(contentType: "text/csv", text: text)
            return
        }

        render([data: inventoryTransactions.list, totalCount: inventoryTransactions.totalCount] as JSON)
    }
}
