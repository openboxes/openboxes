package org.pih.warehouse.importer

import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

import org.pih.warehouse.core.http.ContentType

/**
 * Wraps a MultipartFile containing some bulk data.
 */
class MultipartFileSource implements BulkDataSource<MultipartFile> {

    MultipartFile source

    @Override
    InputStream asInputStream() {
        return source.inputStream
    }

    @Override
    ContentType getContentType() {
        if (!source) {
            return null
        }
        return ContentType.getByMediaType(source.contentType)
    }

    /**
     * @return The name of the file including the extension (but excluding the path to the file).
     */
    String getFileName() {
        if (!source) {
            return null
        }
        return FilenameUtils.getName(source.originalFilename)
    }

    /**
     * @return The size of the file in bytes.
     */
    Long getFileSize() {
        return source?.size
    }

    static constraints = {
        // Grails (annoyingly) treats any method starting with "get" as a field to validate, even if there's
        // no underlying property, so we need to manually skip validation for those methods.
        contentType(nullable: true)
        fileName(nullable: true)
        fileSize(nullable: true)
    }
}
