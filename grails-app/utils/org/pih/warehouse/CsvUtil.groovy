package org.pih.warehouse

class CsvUtil {
    static final Set<String> validBooleanValues = ['t', 'f', 'true', 'false', '1', '0', 'y', 'n', 'yes', 'no']
    static final Set<String> trueBooleanValues = ['t', 'true', '1', 'y', 'yes']

    /**
     * Parses a CSV boolean field.
     *
     * @param value the string value from the CSV field
     * @param rowCount the row number in the CSV file, used for error reporting
     * @return Boolean true if the value represents a true boolean, false if false
     * @throws RuntimeException if the value is not empty and not a valid boolean
     */
    static Boolean parseCsvBooleanField(String value, int rowCount) {
        if (!value) {
            return true
        }

        String parsedValue = value.trim().toLowerCase()

        if (!(parsedValue in validBooleanValues)) {
            throw new RuntimeException("Active field has to be either empty or a boolean value (${validBooleanValues.join(', ')}) at row ${rowCount}")
        }

        return parsedValue in trueBooleanValues
    }
}
