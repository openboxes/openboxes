package org.pih.warehouse.reporting

import grails.converters.JSON
import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.ReportService

class InventoryAuditReportController {

    DataService dataService
    ReportService reportService

    def getInventoryAuditDetails(InventoryAuditCommand command) {
        def data = reportService.getInventoryAuditDetails(command)
        render([
                data      : data,
                count     : data?.size() ?: 0,
                max       : command.max,
                offset    : command.offset,
                totalCount: data.totalCount,
        ] as JSON)

    }

    def getInventoryAuditSummary(InventoryAuditCommand command) {

        // If we specify a format=csv we want to download everything
        if (params.format == 'csv') {
            command.max = -1
        }

        def data = reportService.getInventoryAuditSummary(command)
        if (params.format == "csv") {
            def text = dataService.generateCsv(data*.toCsv())
            response.setHeader("Content-disposition", "attachment; filename=\"inventory-audit-summary.csv\"")
            render(contentType: "text/csv", text: text)
            return
        }

        render([
                data      : data,
                count     : data?.size() ?: 0,
                max       : command.max,
                offset    : command.offset,
                totalCount: data.totalCount,
        ] as JSON)

    }
}

class InventoryAuditCommand extends PaginationCommand {
    Location facility
    List<Product> products
    Date startDate
    Date endDate

    static constraints = {
        facility(nullable: false)
        products(nullable: true)
        startDate(nullable: true)
        endDate(nullable: true)
    }
}
