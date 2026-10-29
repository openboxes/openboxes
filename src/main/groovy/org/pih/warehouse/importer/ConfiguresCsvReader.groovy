package org.pih.warehouse.importer

import org.pih.warehouse.core.http.ContentType

/**
 * Customizes the CSV file reader, configuring it for a specific feature.
 */
trait ConfiguresCsvReader implements ConfiguresBulkDataReader {

    /**
     * @return the configuration to use when reading in the CSV file.
     */
    abstract CsvReaderConfig getCsvReaderConfig()

    @Override
    BulkDataReaderConfig getBulkDataReaderConfig(ContentType contentType) {
        // Ignore the given content type. We're a CSV writer so always return the CSV config
        return getCsvReaderConfig()
    }

    @Override
    List<ContentType> getSupportedContentTypes() {
        return [ContentType.CSV]
    }
}
