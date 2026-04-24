package org.pih.warehouse.importer

import grails.validation.ValidationException

import org.pih.warehouse.core.http.ContentType

/**
 * Reads in a source object containing bulk data, converting its contents to a Java-friendly structure.
 *
 * A Reader on its own does nothing with the contents of the source object. It exists purely to bind the bulk data
 * contained within a source object to a standardized format. From there, other components can process the data
 * without needing to know anything about where the data came from.
 */
abstract class BulkDataReader<Source extends BulkDataSource, Config extends BulkDataReaderConfig> {

    /**
     * Contains the logic for reading in the source object and binding its rows to a List of Map of fields.
     */
    protected abstract BulkDataReaderResult doRead(Source source, Config config)

    /**
     * @return The list of content types that the reader can handle.
     */
    abstract List<ContentType> getSupportedContentTypes()

    /**
     * Validates the source object, throwing exceptions if it is not valid for the reader.
     */
    protected void validateSource(Source source) {
        if (!source?.validate()) {
            throw new ValidationException("Source is invalid", source?.errors)
        }

        if (!supportedContentTypes.contains(source.contentType)) {
            throw new IllegalArgumentException("Reader does not support content-type ${source.contentType}. Only the following content-types are allowed: ${supportedContentTypes}")
        }
    }

    /**
     * Reads in the source object, binding its rows to a List of Map of fields.
     */
    BulkDataReaderResult read(Source source, Config config) {
        validateSource(source)
        return doRead(source, config)
    }
}
