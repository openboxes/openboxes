package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component

/**
 * For converting objects into Double types.
 */
@Component
class DoubleParser extends Parser<Double, ParserContext<Double>> {

    @Override
    boolean isDefaultParserForType() {
        return true
    }

    /**
     * Converts the given String to a Double.
     *
     * Prefer the parse(Object) method when possible as it is much more flexible to variable input.
     *
     * @param toParse the object to convert
     * @param context The context object containing information about how to parse the string
     *
     */
    static Double parseString(String toParse, ParserContext<Double> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return context?.defaultValue
        }

        return toParse.toDouble()
    }

    @Override
    protected Double parseImpl(Object toParse, ParserContext<Double> context) {
        switch (toParse) {
            case String:
                return parseString(toParse as String, context)
            case BigDecimal:
                return toParse.doubleValue()
            case Double:
                return toParse
            default:
                return toParse as Double
        }
    }
}
