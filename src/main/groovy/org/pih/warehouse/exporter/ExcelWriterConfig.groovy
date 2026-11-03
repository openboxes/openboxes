package org.pih.warehouse.exporter

/**
 * Configuration for writing data to Excel files.
 */
class ExcelWriterConfig extends BulkDataWriterConfig {

    /**
     * The default sheet name that Excel uses when you create a new file.
     */
    private static final String EXCEL_DEFAULT_SHEET_NAME = "Sheet1"

    /**
     * The name of the sheet / tab within the Excel file to write to.
     */
    String sheetName = EXCEL_DEFAULT_SHEET_NAME

    /**
     * The styling to use when generating the Excel file.
     */
    ExcelStyle excelStyle = ExcelStyle.TABLE
}
