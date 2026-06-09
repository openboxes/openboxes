package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * For converting objects into Integer types.
 */
@Component
class IntegerParser extends Parser<Integer, ParserContext<Integer>> {

    @Override
    boolean isDefaultParserForType() {
        return true
    }

    /**
     * Converts the given String to an Integer.
     *
     * Prefer the parse(Object) method when possible as it is much more flexible to variable input.
     *
     * @param toParse the String to convert
     * @param context The context object containing information about how to parse the string
     */
    static Integer parseString(String toParse, ParserContext<Integer> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return context?.defaultValue
        }

        try {
            return toParse.toInteger()
        } catch (NumberFormatException ignore) {
            // The String.toInteger() method will error if given a String like "1.1" but since our default
            // behaviour is to round down decimals, we allow stringified decimals to be converted to integers.
            return toParse.toDouble().toInteger()
        }
    }

    @Override
    protected Integer parseImpl(Object toParse, ParserContext<Integer> context) {
        switch (toParse) {
            case String:
                return parseString(toParse as String, context)
            case Double:
                return toParse.toInteger()
            case Long:
                return toParse.toInteger()
            case BigDecimal:
                return toParse.toInteger()
            case Integer:
                return toParse
            default:
                return toParse as Integer
        }
    }
}
