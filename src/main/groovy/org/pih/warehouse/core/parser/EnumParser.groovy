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
     * Due to the complexities in resolving the enum type, in a majority of use cases, this method is likely
     * preferred over the non-static parse(Object) method.
     *
     * @param toParse the object to convert
     * @param enumClass the class of the Enum that we are parsing into
     * @param context the context object containing information about how to parse the string
     */
    static T parseString(String toParse, Class<T> enumClass, ParserContext<T> context=null) {
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
        // This parser is generic for all enums, and so
        if (context.typeToParseTo == null) {
            throw new IllegalArgumentException("Due to type erasure, when parsing Enums you must either specify ParserContext.typeToParseTo, or use the static EnumParser.parseString method")
        }

        switch (toParse) {
            case String:
                return parseString(toParse as String, context.typeToParseTo, context)
            default:
                throw new IllegalArgumentException("Cannot parse given value [${toParse}] to the ${targetType} Enum")
        }
    }
}
