package org.pih.warehouse.importer

import grails.validation.ValidationException
import org.springframework.context.annotation.Lazy

import org.pih.warehouse.core.http.ContentType

/**
 * Reads in a source object containing bulk data, converting its contents to a Java-friendly structure.
 *
 * A Reader on its own does nothing with the contents of the source object. It exists purely to bind the bulk data
 * contained within a source object to a standardized format. From there, other components can process the data
 * without needing to know anything about where the data came from.
 */
abstract class BulkDataReader<Config extends BulkDataReaderConfig> {

    final private BulkDataImportComponentResolver componentResolver

    // The component resolver is annotated with @Lazy because it wires in the readers, creating a circular dependency.
    // Fortunately the reader doesn't immediately use the component resolver so we can simply delay fetching it.
    BulkDataReader(@Lazy final BulkDataImportComponentResolver componentResolver) {
        this.componentResolver = componentResolver
    }

    /**
     * Contains the logic for reading in the source object and binding its rows to a List of Map of fields.
     */
    protected abstract BulkDataReaderResult doRead(BulkDataSource source, Config config)

    /**
     * @return The list of content types that the reader can handle.
     */
    abstract List<ContentType> getSupportedContentTypes()

    /**
     * Validates the source object, throwing exceptions if it is not valid for the reader.
     */
    protected void validateSource(BulkDataSource source) {
        if (!source?.validate()) {
            throw new ValidationException("Source is invalid", source?.errors)
        }

        if (!supportedContentTypes.contains(source.contentType)) {
            throw new IllegalArgumentException("Reader does not support content-type ${source.contentType}. Only the following content-types are allowed: ${supportedContentTypes}")
        }
    }

    /**
     * Reads in the source object, binding its rows to a List of Map of fields.
     *
     * For use when we want to rely on the default reader configuration for a given bulk data type.
     *
     * @param source The source object to read in.
     * @param bulkDataType Determines which configurer to use when reading the data source.
     * @return BulkDataReaderResult The result of reading the bulk data source.
     */
    BulkDataReaderResult read(BulkDataSource source, BulkDataType bulkDataType) {
        ContentType contentType = source.contentType
        ConfiguresBulkDataReader readerConfigurer = componentResolver.getBulkDataReaderConfigurer(
                contentType, bulkDataType)
        if (!readerConfigurer) {
            throw new RuntimeException("No bulk data reader config was found for content type ${contentType} " +
                    "and bulk data type ${bulkDataType}")
        }
        return read(source, readerConfigurer.getBulkDataReaderConfig(contentType) as Config)
    }

    /**
     * Reads in the source object, binding its rows to a List of Map of fields.
     *
     * For use when we want to provide custom reader configuration that overrides the default.
     *
     * @param source The source object to read in.
     * @param config Configuration for reading in the source object.
     * @return BulkDataReaderResult The result of reading the bulk data source.
     */
    BulkDataReaderResult read(BulkDataSource source, Config config) {
        validateSource(source)
        return doRead(source, config)
    }
}
