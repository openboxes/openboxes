package org.pih.warehouse.exporter

import grails.validation.Validateable

/**
 * Configuration for writing data to Excel files.
 */
class ExcelWriterConfig extends BulkDataWriterConfig implements Validateable {

    /**
     * The name of the sheet / tab within the Excel file to write to.
     */
    String sheetName = "Sheet1"

    /**
     * The styling to use when generating the Excel file.
     */
    ExcelStyle excelStyle = ExcelStyle.PLAIN
}
