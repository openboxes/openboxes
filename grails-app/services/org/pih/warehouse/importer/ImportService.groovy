package org.pih.warehouse.importer

import grails.gorm.transactions.Transactional

@Transactional
class ImportService {

    BulkDataImporter bulkDataImporter

    /**
     * Imports a source object (such as a file) containing bulk data into the system.
     */
    BulkDataImportResult importData(BulkDataImportRequest command) {
        return bulkDataImporter.importBulkDataSource(command)
    }
}
