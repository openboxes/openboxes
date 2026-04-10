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
        return ContentType.getByMediaType(file.contentType)
    }

    /**
     * @return The name of the file including the extension (but excluding the path to the file).
     */
    String getFileName() {
        return FilenameUtils.getName(file.originalFilename)
    }

    /**
     * @return The size of the file in bytes.
     */
    long getFileSize() {
        return file.size
    }

    static constraints = {
        file(nullable: false)
    }
}
