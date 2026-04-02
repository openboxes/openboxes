package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * For converting objects into Enum types.
 */
@Component
class EnumParser<T extends Enum> extends Parser<T, ParserContext<T>> {

    /**
     * Converts the given String to an instance of the given Enum.
     *
     * Prefer the parse(Object) method when possible as it is much more flexible to variable input.
     *
     * @param toParse the object to convert
     * @param enumClass the class of the Enum that we are parsing into
     * @param context the context object containing information about how to parse the string
     */
    static T parse(String toParse, Class<T> enumClass, ParserContext<T> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return context?.defaultValue == null ? null : context.defaultValue
        }

        // Because enum values are constants, the naming convention is for them to be uppercase so try that first.
        String toParseSanitized = toParse.trim().toUpperCase()
        try {
            return Enum.valueOf(enumClass, toParseSanitized)
        } catch (IllegalArgumentException ignored) {
            // Do nothing
        }

        // In the rare case where we have an enum that is not uppercase, we can try a case-insensitive compare instead.
        for (T enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().compareToIgnoreCase(toParseSanitized) == 0) {
                return enumValue
            }
        }
        return null
    }

    @Override
    protected T parseImpl(Object toParse, ParserContext<T> context) {
        switch (toParse) {
            case String:
                return parse(toParse as String, targetType)
            default:
                throw new IllegalArgumentException("Cannot parse given value [${toParse}] to the ${targetType} Enum")
        }
    }
}
