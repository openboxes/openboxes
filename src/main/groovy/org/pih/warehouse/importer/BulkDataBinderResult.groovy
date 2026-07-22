package org.pih.warehouse.importer

/**
 * Holds the result of binding some bulk data to a strongly typed object.
 */
class BulkDataBinderResult<T extends Importable> {

    /**
     * The strongly typed objects output by the data binder.
     */
    List<T> boundRows = []

    /**
     * The collection of errors that occurred during the bulk data binding process.
     */
    BulkDataErrors bindErrors = new BulkDataErrors()
}
