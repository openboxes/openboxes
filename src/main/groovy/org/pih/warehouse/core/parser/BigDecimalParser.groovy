package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * For converting objects into BigDecimal types.
 */
@Component
class BigDecimalParser extends Parser<BigDecimal, ParserContext<BigDecimal>> {

    @Override
    boolean isDefaultParserForType() {
        return true
    }

    /**
     * Converts the given String to a BigDecimal.
     *
     * Prefer the parse(Object) method when possible as it is much more flexible to variable input.
     *
     * @param toParse the String to convert
     * @param context The context object containing information about how to parse the string
     */
    static BigDecimal parseString(String toParse, ParserContext<BigDecimal> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return context?.defaultValue
        }

        return toParse.toBigDecimal()
    }

    @Override
    protected BigDecimal parseImpl(Object toParse, ParserContext<BigDecimal> context) {
        switch (toParse) {
            case String:
                return parseString(toParse as String, context)
            case Double:
                return BigDecimal.valueOf(toParse)
            case Long:
                return BigDecimal.valueOf(toParse)
            case BigDecimal:
                return toParse
            default:
                return toParse as BigDecimal
        }
    }
}
