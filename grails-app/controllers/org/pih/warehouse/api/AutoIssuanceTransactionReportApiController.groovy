package org.pih.warehouse.api

import grails.converters.JSON
import grails.validation.ValidationException
import org.pih.warehouse.PaginatedList
import org.pih.warehouse.core.date.DateFormatter
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.AutoIssuanceTransactionDto
import org.pih.warehouse.inventory.AutoIssuanceTransactionFormatter
import org.pih.warehouse.inventory.AutoIssuanceTransactionReportService
import org.pih.warehouse.report.AutoIssuanceTransactionsReportCommand

class AutoIssuanceTransactionReportApiController {

    AutoIssuanceTransactionReportService autoIssuanceTransactionReportService
    AutoIssuanceTransactionFormatter autoIssuanceTransactionFormatter
    DataService dataService
    DateFormatter dateFormatter

    def getAutoIssuanceTransactions(AutoIssuanceTransactionsReportCommand command) {
        if (!command.validate()) {
            throw new ValidationException("Invalid params", command.errors)
        }

        // If we specify a format=csv we want to download everything
        if (params.format == 'csv') {
            command.max = -1
        }

        PaginatedList<AutoIssuanceTransactionDto> autoIssuanceTransactions =
                autoIssuanceTransactionReportService.getAutoIssuanceTransactions(command)

        if (params.format == 'csv') {
            String text = dataService.generateCsv(autoIssuanceTransactionFormatter.toCsv(autoIssuanceTransactions))
            String fileName = "auto-issuance-transactions-${command.facility}-${dateFormatter.formatCurrentDateForFileName()}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
            render(contentType: "text/csv", text: CSVUtils.prependBomToCsvString(text), encoding: "UTF-8")
            return
        }

        render([
                data      : autoIssuanceTransactions.collect { it.asResponseBody() },
                totalCount: autoIssuanceTransactions.totalCount,
        ] as JSON)
    }
}
