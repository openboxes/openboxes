package org.pih.warehouse.importer

import org.pih.warehouse.core.http.ResponseBodyFormattable

/**
 * Holds the final result of importing some bulk data.
 */
class BulkDataImportResult implements ResponseBodyFormattable {
    /**
     * The bulk data rows that have been bound to a strongly typed Importable object.
     */
    List<Importable> boundRows = []

    /**
     * A collection of all errors that occurred during the full import process.
     */
    BulkDataErrors importErrors = new BulkDataErrors()

    @Override
    Map<String, Object> asResponseBody() {
        return [
                boundRows: boundRows,
                // BulkDataErrors is a simple wrapper on a list of BulkDataError so simplify the response object
                // by directly mapping to the list.
                importErrors: importErrors.allErrors,
        ]
    }
}
