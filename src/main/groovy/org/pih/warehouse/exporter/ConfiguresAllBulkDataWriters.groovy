package org.pih.warehouse.exporter

import org.pih.warehouse.core.http.ContentType

/**
 * Configures all of the bulk data writers from a single, shared {@link BulkDataExportConfig}.
 *
 * Holds the writer logic that is common to both {@link BulkDataExportConfigurer} (for export-only features) and
 * {@link org.pih.warehouse.importer.BulkDataImportExportConfigurer} (for import + export features).
 */
trait ConfiguresAllBulkDataWriters implements ConfiguresCsvWriter, ConfiguresExcelWriter {

    /**
     * @return The export config shared by all the writers. Because this is called each time a writer config is
     *         fetched, implementations should cache the config instead of rebuilding it on every call.
     */
    abstract BulkDataExportConfig getCachedExportConfig()

    /**
     * We're configuring both the CSV and Excel writers so we support the content types of both.
     */
    @Override
    Set<ContentType> getSupportedContentTypes() {
        return writerContentTypes
    }

    /**
     * @return The content types supported by all of the writers.
     */
    Set<ContentType> getWriterContentTypes() {
        return csvWriterContentTypes + excelWriterContentTypes
    }

    @Override
    BulkDataWriterConfig getBulkDataWriterConfig(ContentType contentType) {
        if (excelWriterContentTypes.contains(contentType)) {
            return excelWriterConfig
        }
        if (csvWriterContentTypes.contains(contentType)) {
            return csvWriterConfig
        }
        throw new IllegalArgumentException("Content type ${contentType} is not supported.")
    }

    @Override
    List<Object> getFileNameArgs() {
        return cachedExportConfig.fileNameArgs
    }

    @Override
    CsvWriterConfig getCsvWriterConfig() {
        return new CsvWriterConfig(
                fields: cachedExportConfig.fieldConfigs,
        )
    }

    @Override
    ExcelWriterConfig getExcelWriterConfig() {
        return new ExcelWriterConfig(
                fields: cachedExportConfig.fieldConfigs,
                // We know that our bulk data exports are always going to be structured like a table
                // (header row + data) so we opt to add some quality of life styling to the Excel file.
                excelStyle: ExcelStyle.TABLE,
        )
    }
}
