package org.pih.warehouse.importer

import org.pih.warehouse.core.http.ContentType

/**
 * Customizes the Excel file reader, configuring it for a specific feature.
 */
trait ConfiguresExcelReader implements ConfiguresBulkDataReader {

    /**
     * @return the configuration to use when reading in the Excel file.
     */
    abstract ExcelReaderConfig getExcelReaderConfig()

    @Override
    BulkDataReaderConfig getBulkDataReaderConfig(ContentType contentType) {
        // Ignore the given content type. We're an Excel writer so always return the Excel config
        return getExcelReaderConfig()
    }

    @Override
    List<ContentType> getSupportedContentTypes() {
        return [ContentType.XLS, ContentType.XLSX]
    }
}
