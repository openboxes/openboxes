package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils

/**
 * For converting input objects (typically Strings) into Boolean types.
 */
class BooleanParser {

    static final Set<String> VALID_BOOLEAN_VALUES = ['t', 'f', 'true', 'false', '1', '0', 'y', 'n', 'yes', 'no']
    static final Set<String> TRUE_BOOLEAN_VALUES = ['t', 'true', '1', 'y', 'yes']

    /**
     * Converts the given string to a Boolean, defaulting to a given default if the given value is null or empty.
     */
    static Boolean parse(String value, Boolean defaultValue=null) {
        if (StringUtils.isBlank(value)) {
            return defaultValue
        }

        String parsedValue = value.trim().toLowerCase()

        if (!(parsedValue in VALID_BOOLEAN_VALUES)) {
            throw new IllegalArgumentException("Given string [${value}] is not a valid boolean value.")
        }

        return parsedValue in TRUE_BOOLEAN_VALUES
    }
}
