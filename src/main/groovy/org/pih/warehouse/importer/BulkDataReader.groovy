package org.pih.warehouse.importer

/**
 * Reads in a source object containing bulk data, converting its contents to a Java-friendly structure.
 *
 * A Reader on its own does nothing with the contents of the source object. It exists purely to bind the bulk data
 * contained within a source object to a standardized format. From there, other components can process the data
 * without needing to know anything about where the data came from.
 */
interface BulkDataReader<Source extends BulkDataSource, Config extends BulkDataReaderConfig> {

    /**
     * Reads in the source object, binding its rows to a List of Map of fields.
     */
    BulkDataReaderResult read(Source source, Config config)
}
