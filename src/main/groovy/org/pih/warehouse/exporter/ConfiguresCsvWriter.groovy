package org.pih.warehouse.exporter

import org.pih.warehouse.core.http.ContentType

/**
 * Customizes the CSV file writer, configuring it for a specific feature.
 */
trait ConfiguresCsvWriter implements ConfiguresBulkDataWriter {

    /**
     * @return The configuration to use when writing the bulk data to CSV.
     */
    abstract CsvWriterConfig getCsvWriterConfig()

    @Override
    BulkDataWriterConfig getBulkDataWriterConfig(ContentType contentType) {
        // Ignore the given content type. We're a CSV writer so always return the CSV config
        return getCsvWriterConfig()
    }

    @Override
    Set<ContentType> getSupportedContentTypes() {
        return [ContentType.CSV]
    }
}
