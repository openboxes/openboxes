package org.pih.warehouse.exporter

import org.pih.warehouse.core.http.ContentType

/**
 * Customizes the Excel file writer, configuring it for a specific feature.
 */
trait ConfiguresExcelWriter implements ConfiguresBulkDataWriter {

    /**
     * @return The configuration to use when writing the bulk data to Excel.
     */
    abstract ExcelWriterConfig getExcelWriterConfig()

    @Override
    BulkDataWriterConfig getBulkDataWriterConfig(ContentType contentType) {
        // Ignore the given content type. We're an Excel writer so always return the Excel config
        return getExcelWriterConfig()
    }

    @Override
    Set<ContentType> getSupportedContentTypes() {
        return [ContentType.XLS, ContentType.XLSX]
    }
}
