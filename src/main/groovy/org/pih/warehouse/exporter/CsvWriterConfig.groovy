package org.pih.warehouse.exporter

/**
 * Configuration for writing data to CSV.
 */
class CsvWriterConfig extends BulkDataWriterConfig {

    private static final String DEFAULT_DELIMITER = ","

    /**
     * The character(s) that separate the columns within the data.
     */
    String delimiter = DEFAULT_DELIMITER
}
