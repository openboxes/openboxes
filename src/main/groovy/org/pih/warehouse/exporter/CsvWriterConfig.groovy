package org.pih.warehouse.exporter

import org.pih.warehouse.core.validation.ObjectValidatable

/**
 * Configuration for writing data to CSV.
 */
class CsvWriterConfig extends BulkDataWriterConfig implements ObjectValidatable<BulkDataWriterConfigValidator> {

    private static final String DEFAULT_DELIMITER = ","

    /**
     * The character(s) that separate the columns within the data.
     */
    String delimiter = DEFAULT_DELIMITER
}
