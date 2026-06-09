package org.pih.warehouse.core.parser

import org.pih.warehouse.core.Constants

/**
 * Context object for use when parsing lists.
 */
class ListParserContext<T> extends ParserContext<List<T>> {

    /**
     * The character(s) that separate the elements of the list.
     */
    String delimiter = Constants.DEFAULT_COLUMN_SEPARATOR

    /**
     * The context object to use when parsing the individual list entries.
     */
    ParserContext<T> listElementParserContext
}
