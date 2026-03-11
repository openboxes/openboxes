package org.pih.warehouse.core.http

import org.pih.warehouse.core.file.FileType

/**
 * Enumerates the HTTP content types that we operate on within the application.
 */
enum ContentType {

    CSV("text/csv", FileType.CSV),
    JSON("application/json", FileType.JSON),
    PDF("application/pdf", FileType.PDF),
    XLS("application/vnd.ms-excel", FileType.XLS),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FileType.XLSX)

    /**
     * The media type, indicating the data type of the content.
     *
     * Note that MIME type is just the content type but without any character encoding (ie excluding "charset=utf-8").
     */
    String mimeType

    /**
     * The file type associated with the content.
     *
     * If the content type is not associated with a file, this will be FileType.NONE
     */
    FileType fileType

    ContentType(String mimeType, FileType fileType) {
        this.mimeType = mimeType
        this.fileType = fileType
    }
}