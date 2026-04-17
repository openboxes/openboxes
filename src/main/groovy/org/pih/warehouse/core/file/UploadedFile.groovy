package org.pih.warehouse.core.file

import grails.validation.Validateable
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

import org.pih.warehouse.core.http.ContentType

/**
 * Wraps a MultipartFile that has been uploaded to the system.
 */
class UploadedFile implements Validateable {

    MultipartFile file

    /**
     * @return The content type of the file.
     */
    ContentType getFileContentType() {
        if (!file) {
            return null
        }
        return ContentType.getByMediaType(file.contentType)
    }

    /**
     * @return The name of the file including the extension (but excluding the path to the file).
     */
    String getFileName() {
        if (!file) {
            return null
        }
        return FilenameUtils.getName(file.originalFilename)
    }

    /**
     * @return The size of the file in bytes.
     */
    Long getFileSize() {
        return file?.size
    }

    static constraints = {
        // Grails (annoyingly) treats any method starting with "get" as a field to validate, even if there's
        // no underlying property, so we need to manually skip validation for those methods.
        fileContentType(nullable: true)
        fileName(nullable: true)
        fileSize(nullable: true)
    }
}
