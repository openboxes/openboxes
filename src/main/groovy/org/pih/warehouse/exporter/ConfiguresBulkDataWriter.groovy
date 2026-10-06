package org.pih.warehouse.exporter

import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.importer.BulkDataType

/**
 * Customizes the bulk data writer, configuring it for a specific feature.
 */
trait ConfiguresBulkDataWriter {

    /**
     * @return The configuration to use when writing the bulk data.
     */
    abstract BulkDataWriterConfig getBulkDataWriterConfig(ContentType contentType)

    /**
     * @return the data type that this configuration is meant to be used for. Bulk data type has a one-to-one
     *         map to a configurer.
     */
    abstract BulkDataType getBulkDataType()

    /**
     * @return The set of content types that can be handled by the writer.
     */
    abstract Set<ContentType> getSupportedContentTypes()
}
