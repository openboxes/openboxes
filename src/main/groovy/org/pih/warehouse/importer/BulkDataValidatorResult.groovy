package org.pih.warehouse.importer

/**
 * Holds the result of validating a collection of {@link Importable} data.
 */
class BulkDataValidatorResult {

    /**
     * The collection of errors that occurred during the bulk data validation process.
     */
    BulkDataErrors validationErrors = new BulkDataErrors()
}
