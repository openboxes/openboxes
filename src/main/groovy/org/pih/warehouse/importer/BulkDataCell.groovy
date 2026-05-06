package org.pih.warehouse.importer

/**
 * Represents an individual cell at some row and column in the bulk data.
 */
class BulkDataCell {
    /**
     * Zero-indexed row containing the cell.
     */
    int row

    /**
     * Zero-indexed column containing the cell.
     */
    int column

    /**
     * The name of the field as specified in the data reader/binder configuration.
     */
    String fieldName

    /**
     * The raw value stored in the cell.
     */
    Object value
}
