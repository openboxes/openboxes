package org.pih.warehouse.importer

import grails.validation.ValidationException

import org.pih.warehouse.core.file.UploadedFile
import org.pih.warehouse.core.file.FileExtension

/**
 * Defines the ability to take in a MultipartFile (via the UploadedFile wrapper class)
 * and transform it to a List<Map> of rows.
 */
abstract class FileImporter<Config extends DataFileImporterConfig> {

    /**
     * Contains the actual logic for importing the file and binding its rows to a simple List of Map of fields.
     */
    abstract FileImporterResult importFileImpl(UploadedFile file, Config config)

    /**
     * @return the list of file extensions that the importer can handle
     */
    abstract List<FileExtension> getSupportedFileExtensions()

    /**
     * Imports the file, binding its rows to a simple List of Map of fields.
     */
    FileImporterResult importFile(UploadedFile file, Config config) {
        if (!file?.validate()) {
            throw new ValidationException("Imported file is invalid", file?.errors)
        }

        if (!supportedFileExtensions.contains(file.fileContentType?.fileExtension)) {
            throw new IllegalArgumentException("This importer does not support ${file.fileContentType} files. Only the following file extensions are allowed: ${supportedFileExtensions}")
        }

        return importFileImpl(file, config)
    }
}
