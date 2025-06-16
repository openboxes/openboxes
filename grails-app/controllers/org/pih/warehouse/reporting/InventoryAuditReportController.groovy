package org.pih.warehouse.reporting

import grails.converters.JSON
import org.pih.warehouse.api.PaginationCommand
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.ReportService

class InventoryAuditReportController {

    ReportService reportService

    def getInventoryAuditDetails(GetInventoryAuditReportCommand command) {
        def data = reportService.getInventoryAuditReportDetails(command)
        render([
                data      : data,
                count     : data?.size() ?: 0,
                max       : command.max,
                offset    : command.offset,
                totalCount: data.totalCount,
        ] as JSON)

    }

    def getInventoryAuditSummary(GetInventoryAuditReportCommand command) {
        def data = reportService.getInventoryAuditReportSummary(command)
        render([
                data      : data,
                count     : data?.size() ?: 0,
                max       : command.max,
                offset    : command.offset,
                totalCount: data.totalCount,
        ] as JSON)

    }

}


class GetInventoryAuditReportCommand extends PaginationCommand {
    Location facility
    Product product
    Date startDate
    Date endDate

    static constraints = {
        facility(nullable: false)
        product(nullable: true)
        startDate(nullable: true)
        endDate(nullable: true)
    }


}
