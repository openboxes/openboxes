package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * For converting objects into Boolean types.
 */
@Component
class BooleanParser extends Parser<Boolean, ParserContext<Boolean>> {

    static final Set<String> VALID_BOOLEAN_VALUES = ['t', 'f', 'true', 'false', '1', '0', 'y', 'n', 'yes', 'no']
    static final Set<String> TRUE_BOOLEAN_VALUES = ['t', 'true', '1', 'y', 'yes']

    @Override
    boolean isDefaultParserForType() {
        return true
    }

    /**
     * Converts the given String to a Boolean.
     *
     * Prefer the parse(Object) method when possible as it is much more flexible to variable input.
     *
     * @param toParse the String to convert
     * @param context The context object containing information about how to parse the string
     */
    static Boolean parseString(String toParse, ParserContext<Boolean> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return context?.defaultValue
        }

        String parsedValue = toParse.trim().toLowerCase()

        if (!(parsedValue in VALID_BOOLEAN_VALUES)) {
            throw new IllegalArgumentException("Given string [${toParse}] is not a valid boolean value.")
        }

        return parsedValue in TRUE_BOOLEAN_VALUES
    }

    @Override
    protected Boolean parseImpl(Object toParse, ParserContext<Boolean> context) {
        switch (toParse) {
            case String:
                return parseString(toParse as String, context)
            case Boolean:
                return toParse
            default:
                return toParse as Boolean
        }
    }
}
