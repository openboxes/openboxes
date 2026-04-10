package org.pih.warehouse.core.http

import org.springframework.http.MediaType

import org.pih.warehouse.core.file.FileExtension

/**
 * Enumerates the HTTP header content types that we operate on within the application.
 */
enum ContentType {

    CSV(FileExtension.CSV, MediaType.parseMediaType("text/csv")),
    DOCX(FileExtension.DOCX, MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    JSON(FileExtension.JSON, MediaType.APPLICATION_JSON),
    PDF(FileExtension.PDF, MediaType.APPLICATION_PDF),
    TXT(FileExtension.TXT, MediaType.TEXT_PLAIN),
    XLS(FileExtension.XLS, MediaType.parseMediaType("application/vnd.ms-excel")),
    XLSX(FileExtension.XLSX, MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    XML(FileExtension.XML, MediaType.TEXT_XML)

    /**
     * The file extension associated with the content.
     *
     * If the content type is not associated with a file, this should be defined with a value of null so that
     * it can be distinguished from the FileExtension.NONE case (which should be used if the content type is
     * associated with a file that does not have an extension).
     */
    FileExtension fileExtension

    /**
     * The media type (which is what the content type header uses) indicating the data type of the content.
     * Media type is MIME type (ex: "application/json") + an optional character encoding (ex: "charset=utf-8")
     */
    MediaType mediaType

    ContentType(FileExtension fileExtension, MediaType mediaType) {
        this.fileExtension = fileExtension
        this.mediaType = mediaType
    }

    static ContentType getByMediaType(String mediaType) {
        return values().find {
            it.mediaType == MediaType.parseMediaType(mediaType)
        }
    }
}
