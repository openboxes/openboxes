package org.pih.warehouse.importer

/**
 * Holds the final result of importing some bulk data.
 */
class BulkDataImportResult {
    /**
     * The bulk data rows that have been bound to a strongly typed Importable object.
     */
    List<Importable> boundRows = []

    /**
     * A collection of all errors that occurred during the full import process.
     */
    List<BulkDataError> importErrors = []
}
