package org.pih.warehouse.exporter

/**
 * Style templates for use when generating Excel files.
 */
enum ExcelStyle {

    /**
     * No styling. Simply display the rows as given.
     */
    PLAIN,

    /**
     * Formats the rows as a table of data with a header row.
     */
    TABLE,
}