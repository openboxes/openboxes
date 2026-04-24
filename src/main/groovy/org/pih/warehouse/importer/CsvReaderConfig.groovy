package org.pih.warehouse.importer

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Configuration for reading CSV data.
 */
class CsvReaderConfig extends BulkDataReaderConfig {

    private static final String DEFAULT_DELIMITER = ","

    /**
     * The character(s) that separate the columns within the data.
     */
    String delimiter = DEFAULT_DELIMITER

    /**
     * The character set that we should use when reading in the CSV data.
     */
    Charset charset = StandardCharsets.UTF_8
}
