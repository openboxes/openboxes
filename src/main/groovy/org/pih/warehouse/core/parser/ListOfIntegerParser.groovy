package org.pih.warehouse.core.parser

import org.springframework.stereotype.Component

/**
 * For converting input objects into a List of Integer types.
 */
@Component
class ListOfIntegerParser extends ListParser<Integer> {

    IntegerParser integerParser

    ListOfIntegerParser(IntegerParser integerParser) {
        this.integerParser = integerParser
    }

    @Override
    protected Parser<Integer, ParserContext> getListElementParser() {
        return integerParser
    }
}
