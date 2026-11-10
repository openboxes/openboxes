package org.pih.warehouse.importer

/**
 * Configuration for reading Excel files.
 */
class ExcelReaderConfig extends BulkDataReaderConfig {

    /**
     * The name of the sheet / tab within the Excel file that contains the data to read.
     */
    String sheetName
}
