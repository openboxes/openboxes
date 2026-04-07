package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * For converting objects into String types.
 */
@Component
class StringParser extends Parser<String, ParserContext<String>> {

    @Override
    boolean isDefaultParserForType() {
        return true
    }

    /**
     * Sanitizes the given String.
     *
     * Prefer the parse(Object) method when possible as it is much more flexible to variable input.
     *
     * @param toParse the String to convert
     * @param context The context object containing information about how to parse the string
     */
    static String parseString(String toParse, ParserContext<String> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return context?.defaultValue == null ? null : context.defaultValue
        }

        return toParse.trim()
    }

    @Override
    protected String parseImpl(Object toParse, ParserContext context) {
        switch (toParse) {
            case String:
                return parseString(toParse as String, context)
            default:
                return toParse.toString()
        }
    }
}
