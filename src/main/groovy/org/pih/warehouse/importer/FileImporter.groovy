package org.pih.warehouse.importer

import org.pih.warehouse.core.file.UploadedFile
import org.pih.warehouse.core.file.FileExtension

/**
 * Defines the ability to take in a MultipartFile (via the UploadedFile wrapper class)
 * and transform it to a List<Map> of rows.
 */
interface FileImporter<Config extends DataFileImporterConfig> {

    /**
     *
     */
    abstract FileImportResult importFile(UploadedFile file, Config config)

    /**
     *
     */
    abstract List<FileExtension> getSupportedFileExtensions()
}
