package org.pih.warehouse.api

import grails.converters.JSON
import javax.validation.Valid

import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.Location
import org.pih.warehouse.data.DataService
import org.pih.warehouse.importer.InventoryLevelImportDataService
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryLevelService
import org.pih.warehouse.inventory.PreferredBinLocationsCommand
import org.pih.warehouse.inventory.PreferredBinLocationsDto

class InventoryLevelApiController extends BaseApiController {

    DataService dataService
    DocumentService documentService
    InventoryLevelImportDataService inventoryLevelImportDataService
    InventoryLevelService inventoryLevelService

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

    /**
     * Returns the preferred bin location of each given product, keyed by product id. Products without
     * an inventory level or with no preferred bin location configured are omitted from the response.
     */
    def getPreferredBinLocations(@Valid PreferredBinLocationsCommand command) {
        PreferredBinLocationsDto preferredBinLocations = inventoryLevelService.getPreferredBinLocations(command)
        renderResponse(preferredBinLocations)
    }
}
