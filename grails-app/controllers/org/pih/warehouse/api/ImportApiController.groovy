package org.pih.warehouse.api

import javax.validation.Valid

import org.pih.warehouse.importer.BulkDataImportResult
import org.pih.warehouse.importer.ImportService
import org.pih.warehouse.importer.BulkDataImportRequest

class ImportApiController extends BaseApiController {

    ImportService importService

    /**
     * Generic API for importing a source object (such as a file) containing bulk data into the system.
     */
    def importData(@Valid BulkDataImportRequest command) {
        BulkDataImportResult result = importService.importData(command)
        renderResponse(result)
    }
}
