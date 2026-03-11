package org.pih.warehouse.core.file

import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.date.DateFormatter

/**
 * Convenience methods for generating localized file names.
 */
@Component
class FileNameGenerator {

    final String FIELD_DELIMITER = '_'

    @Autowired
    DateFormatter dateFormatter

    /**
     * Generates a file name for the given file type using the given fields.
     */
    String generate(FileType fileType, Collection<Object> fields) {
        String fileName = ""

        Iterator<Object> iterator = fields.iterator()
        while(iterator.hasNext()) {
            Object field = iterator.next()
            if (!field) {
                continue
            }

            switch (field) {
                case Instant:
                case LocalDate:
                case ZonedDateTime:
                case Date:
                    fileName += dateFormatter.formatForFileName(field)
                    break
                case String:
                    fileName += field.trim()
                    break
                default:
                    fileName += field.toString().trim()
                    break
            }

            if (iterator.hasNext()) {
                fileName += FIELD_DELIMITER
            }
        }

        if (fileType) {
             fileName += fileType.extension
        }

        return sanitizeFileName(fileName)
    }

    private String sanitizeFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return ''
        }

        return fileName.trim()
                // Remove all characters that are invalid in Windows file names
                .replaceAll("[\\\\/:*?\"<>|]", '')
    }
}
