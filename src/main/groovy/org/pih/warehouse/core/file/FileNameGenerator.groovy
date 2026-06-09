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

    private final String FILE_NAME_PART_DELIMITER = '_'

    @Autowired
    DateFormatter dateFormatter

    /**
     * Generates a file name for the given file extension using the given fields.
     *
     * @param fileExtension the file extension to use when generating the file name
     * @param fields the fields to include in the file name
     * @param delimiter the separator between each field in the file name
     */
    String generate(FileExtension fileExtension, Collection<Object> fields, String delimiter=FILE_NAME_PART_DELIMITER) {
        String fileName = ""
        for (Object field in fields) {
            String formattedField = formatFieldForFileName(field)

            // Formatting the field may filter out all characters. Exclude the field entirely in this case
            if (StringUtils.isBlank(formattedField)) {
                continue
            }

            // Otherwise we're adding the field to the name, but only prepend the delimiter if it's not the first field
            if (StringUtils.isNotEmpty(fileName)) {
                fileName += delimiter
            }

            fileName += formattedField
        }

        if (fileExtension) {
             fileName += fileExtension.extension
        }

        return fileName
    }

    private String formatFieldForFileName(Object field) {
        String fieldString
        switch (field) {
            case Instant:
            case LocalDate:
            case ZonedDateTime:
            case Date:
                fieldString = dateFormatter.formatForFileName(field)
                break
            case String:
                fieldString = field
                break
            case null:
                fieldString = null
                break
            default:
                fieldString = field.toString()
                break
        }

        return sanitizeFieldForFileName(fieldString)
    }

    private String sanitizeFieldForFileName(String field) {
        if (field == null) {
            return ''
        }

        return field.trim()
                // Remove all characters that are invalid in Windows file names
                .replaceAll("[\\\\/:*?\"<>|]", '')
    }
}
