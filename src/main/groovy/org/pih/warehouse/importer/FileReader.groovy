package org.pih.warehouse.importer

import grails.validation.ValidationException

import org.pih.warehouse.core.file.FileExtension

/**
 * Reads in a source file containing bulk data, converting its rows to a Java-friendly structure.
 */
abstract class FileReader<Source extends BulkDataSource, Config extends BulkDataReaderConfig>
        implements BulkDataReader<Source, Config> {

    /**
     * Reads in the file and binds its rows to a List of Map of fields.
     */
    abstract protected BulkDataReaderResult readFile(Source source, Config config)

    /**
     * @return the list of file extensions that the reader can handle
     */
    abstract List<FileExtension> getSupportedFileExtensions()

    protected void validateFile(Source source) {
        if (!source?.validate()) {
            throw new ValidationException("File is invalid", source?.errors)
        }

        if (!supportedFileExtensions.contains(source.contentType?.fileExtension)) {
            throw new IllegalArgumentException("This file reader does not support ${source.contentType} files. Only the following file extensions are allowed: ${supportedFileExtensions}")
        }
    }

    @Override
    BulkDataReaderResult read(Source source, Config config) {
        validateFile(source)
        return readFile(source, config)
    }
}
