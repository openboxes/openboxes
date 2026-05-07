package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.data.DataService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.packList.PackImportDataCommand

class PackListApiController {

    StockMovementService stockMovementService
    DataService dataService
    DocumentService documentService

    def exportPackTemplate() {
        String format = params.get("format", "csv")

        List<Map<String, String>> lineItems = stockMovementService.buildPackTemplateLineItems(params.id)

        String fileName = "PackListItems\$-${params.id}-template"

        switch (format) {
            case "csv":
                String csv = dataService.generateCsv(lineItems)
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.csv\"")
                render(contentType: "text/csv", text: csv, encoding: "UTF-8")
                break
            case "xls":
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${fileName}.xls\"")
                documentService.generateExcel(response.outputStream, lineItems)
                response.outputStream.flush()
                break
            default:
                throw new IllegalFormatException("Unable to determine the proper rendering format for request for format ${format}")
        }
    }

    def importPackListItems(PackImportDataCommand command) {
        List<String> errors = stockMovementService.processPackListImport(command, params.id)

        if (!errors.isEmpty()) {
            render([message: "Data imported with errors", errors: errors] as JSON)
        }

        render([message: "Data imported successfully"] as JSON)
    }
}
