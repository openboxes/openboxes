package org.pih.warehouse.core.file

import org.apache.commons.lang.StringUtils

/**
 * Enumerates the file extensions that we operate on within the application.
 */
enum FileExtension {

    NONE(""),
    CSV(".csv"),
    DOC(".doc"),
    DOCX(".docx"),
    JSON(".json"),
    PDF(".pdf"),
    TXT(".txt"),
    XLS(".xls"),
    XLSX(".xlsx"),
    XML(".xml")

    /**
     * The file extension associated with that file type.
     */
    String extension

    FileExtension(String extension) {
        this.extension = extension
    }

    static FileExtension fromExtensionString(String extension) {
        String extensionSanitized = extension?.trim()?.toLowerCase()

        if (StringUtils.isBlank(extensionSanitized)) {
            return NONE
        }

        if (!extensionSanitized.startsWith('.')) {
            extensionSanitized = '.' + extensionSanitized
        }

        return values().find { it.extension == extensionSanitized }
    }
}
