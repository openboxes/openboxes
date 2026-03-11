package org.pih.warehouse.core.file

/**
 * Enumerates the file types that we operate on within the application.
 */
enum FileType {

    NONE(""),
    CSV(".csv"),
    JSON(".json"),
    PDF(".pdf"),
    XLS(".xls"),
    XLSX(".xlsx")

    /**
     * The file extension associated with that file type.
     */
    String extension

    FileType(String extension) {
        this.extension = extension
    }
}