package org.pih.warehouse.importer

import org.pih.warehouse.core.file.UploadedFile
import org.pih.warehouse.core.file.FileExtension

/**
 * Defines the ability to take in a MultipartFile (via the UploadedFile wrapper class)
 * and transform it to a List<Map> of rows.
 */
interface FileImporter<Config extends DataFileImporterConfig> {

    /**
     * Imports the file, binding its rows to a simple List of Map of fields.
     */
    FileImportResult importFile(UploadedFile file, Config config)

    /**
     * @return the list of file extensions that the importer can handle
     */
    List<FileExtension> getSupportedFileExtensions()
}
