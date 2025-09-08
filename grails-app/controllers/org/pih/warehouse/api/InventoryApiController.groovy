package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryImportDataService

class InventoryApiController {

    InventoryImportDataService inventoryImportDataService

    def importCsv() {
        String fileData = request.inputStream.text

        if (fileData.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty")
        }

        if (request.contentType != "text/csv") {
            throw new IllegalArgumentException("File must be in CSV format")
        }

        ImportDataCommand command = new ImportDataCommand(
                data: CSVUtils.csvToObjects(fileData),
                date: new Date(System.currentTimeMillis() - 1000),
                location: Location.get(params.facilityId)
        )

        inventoryImportDataService.validateData(command)
        inventoryImportDataService.importData(command)

        render(status: 200)
    }
}
