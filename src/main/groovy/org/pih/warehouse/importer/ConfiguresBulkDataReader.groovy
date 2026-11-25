package org.pih.warehouse.importer

import org.pih.warehouse.core.http.ContentType

/**
 * Customizes the bulk data reader, configuring it for a specific feature.
 */
trait ConfiguresBulkDataReader {

    /**
     * @return the configuration to use when reading in the data file.
     */
    abstract BulkDataReaderConfig getBulkDataReaderConfig(ContentType contentType)

    /**
     * @return the data type that this configuration is meant to be used for. Bulk data type has a one-to-one
     *         map to a configurer.
     */
    abstract BulkDataType getBulkDataType()

    /**
     * @return The list of content types that can be handled by the reader that we configure.
     */
    abstract List<ContentType> getSupportedContentTypes()
}
