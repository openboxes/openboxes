package org.pih.warehouse.importer

class CsvFileImporterConfig extends DataFileImporterConfig {

    private static final String DEFAULT_DELIMITER = ","

    /**
     * The character(s) that separate the columns within the file.
     */
    String delimiter = DEFAULT_DELIMITER
}
