package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.InventoryLevelImportDataService
import org.pih.warehouse.inventory.InventoryLevel

class InventoryLevelApiController {

    DataService dataService
    DocumentService documentService
    InventoryLevelImportDataService inventoryLevelImportDataService

    def list() {
        Location facility = Location.get(params.facilityId)
        if (!facility)
            throw new IllegalArgumentException("Unable to locate facility with id ${params.facilityId}")

        List inventoryLevels = InventoryLevel.createCriteria().list {
            eq("inventory", facility.inventory)
            isNull("internalLocation")
        }

        withFormat {
            "xls" {
                def data = dataService.transformObjects(inventoryLevels, InventoryLevel.PROPERTIES)
                documentService.generateExcel(response.outputStream, data)
                response.setHeader 'Content-disposition', "attachment; filename=\"inventory-levels.xls\""
                response.outputStream.flush()
                return
            }
            "csv" {
                String text = inventoryLevelImportDataService.exportInventoryLevels(inventoryLevels)
                response.contentType = "text/csv"
                response.setHeader("Content-disposition", "attachment; filename=\"inventory-levels.csv\"")
                render(text)
                return
            }

            "*" {
                render([data: inventoryLevels] as JSON)
            }
        }
    }
}
