package org.pih.warehouse.core.parser

import org.springframework.stereotype.Component

/**
 * For converting input objects into a List of String types.
 */
@Component
class ListOfStringParser extends ListParser<String> {

    StringParser stringParser

    ListOfStringParser(StringParser stringParser) {
        this.stringParser = stringParser
    }

    @Override
    boolean isDefaultParserForType() {
        // Due to type erasure, this is the default for *all* list parsers, not just lists of Strings.
        return true
    }

    @Override
    protected Parser<String, ParserContext> getListElementParser() {
        return stringParser
    }
}
