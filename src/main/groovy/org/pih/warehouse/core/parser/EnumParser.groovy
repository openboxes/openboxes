package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils

/**
 * For converting input objects (typically Strings) into Enum types.
 */
class EnumParser {

    /**
     * Converts the given string to an instance of the given Enum.
     */
    static <T extends Enum> T parse(String value, Class<T> enumClass) {
        if (StringUtils.isBlank(value)) {
            return null
        }

        // Because enum values are constants, the naming convention is for them to be uppercase so try that first.
        String valueSanitized = value.trim().toUpperCase()
        try {
            return Enum.valueOf(enumClass, valueSanitized)
        } catch (IllegalArgumentException ignored) {
            // Do nothing
        }

        // In the rare case where we have an enum that is not uppercase, we can try a case-insensitive compare instead.
        for (T enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().compareToIgnoreCase(valueSanitized) == 0) {
                return enumValue
            }
        }
        return null
    }
}
