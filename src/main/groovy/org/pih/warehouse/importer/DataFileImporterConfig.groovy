package org.pih.warehouse.importer

/**
 * Only needed for step 1 where we import the file and format it to a Map
 */
abstract class DataFileImporterConfig {

    /**
     * zero index based. Defaults to 1 because there is usually a header row.
     */
    int startRow = 1

    /**
     *
     */
    Map<String, String> columnToFieldMapping = [:]
}
