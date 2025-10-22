package org.pih.warehouse

import org.apache.commons.lang.StringUtils

class CsvUtil {
    static final Set<String> VALID_BOOLEAN_VALUES = ['t', 'f', 'true', 'false', '1', '0', 'y', 'n', 'yes', 'no']
    static final Set<String> TRUE_BOOLEAN_VALUES = ['t', 'true', '1', 'y', 'yes']

    /**
     * Parses a CSV boolean field.
     *
     * @param value the string value from the CSV field
     * @param rowCount the row number in the CSV file, used for error reporting
     * @param defaultValue the value to return if the field is empty or blank
     * @return Boolean true if the value represents a true boolean, false if false
     * @throws RuntimeException if the value is not empty or blank and not a valid boolean
     */
    static Boolean parseCsvBooleanField(String value, int rowCount, Boolean defaultValue = false) {
        if (StringUtils.isBlank(value)) {
            return defaultValue
        }

        String parsedValue = value.trim().toLowerCase()

        if (!(parsedValue in VALID_BOOLEAN_VALUES)) {
            throw new RuntimeException("Active field has to be either empty or a boolean value ${VALID_BOOLEAN_VALUES} at row ${rowCount}")
        }

        return parsedValue in TRUE_BOOLEAN_VALUES
    }
}
