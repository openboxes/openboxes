package org.pih.warehouse.importer

/**
 * Enumerates the different severity levels of import / bulk data binding errors that can occur.
 */
enum BulkDataErrorSeverity {

    /**
     * Signifies that a row/col/field was able to be bound but there were issues that
     * the user should be made aware of. Attempting to persist this data should still succeed.
     */
    WARNING,

    /**
     * Signifies that a row/col/field was unable to be bound. Attempting to persist this data will
     * fail or create invalid state.
     */
    ERROR,
}
