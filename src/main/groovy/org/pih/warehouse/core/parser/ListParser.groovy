package org.pih.warehouse.core.parser

import org.apache.commons.lang.StringUtils

/**
 * For converting input objects into a List of objects.
 */
abstract class ListParser<T> extends Parser<List<T>, ListParserContext<T>> {

    /**
     * Returns an instance of the parser to use when parsing the individual elements of the list.
     */
    protected abstract Parser<T, ParserContext> getListElementParser()

    private List<T> getDefaultValue(ListParserContext<T> context) {
        if (context?.defaultValue == null) {
            return null
        }
        return context?.defaultValue as List<T>
    }

    /**
     * Converts the given String to a List.
     */
    private List<T> parseString(String toParse, ListParserContext<T> context=null) {
        if (StringUtils.isBlank(toParse)) {
            return getDefaultValue(context)
        }

        // Filters out blank and invalid elements. For example, if given " , , x,  y , ,, " it will output [x, y]
        return toParse.split(context?.delimiter)
                .findAll { StringUtils.isNotBlank(it) }
                .collect { listElementParser.parse(it?.trim(), context?.listElementParserContext) }
                .findAll { it != null }
    }

    /**
     * Converts the given Collection to a List.
     */
    private List<T> parseCollection(Collection toParse, ListParserContext<T> context=null) {
        if (!toParse) {
            return getDefaultValue(context)
        }

        // Filters out blank and invalid elements. For example, if given [, , x,  y , ,, ] it will output [x, y]
        return toParse.collect { listElementParser.parse(it, context?.listElementParserContext) }
                .findAll { it != null }
    }

    /**
     * Converts the given object into a singleton List of the parsed type.
     */
    private List<T> parseSingleElement(Object toParse, ListParserContext<T> context) {
        T result = listElementParser.parse(toParse, context.listElementParserContext)
        return result == null ? getDefaultValue(context) : [result]
    }

    @Override
    List<T> parseImpl(Object toParse, ListParserContext<T> context) {
        switch (toParse) {
            case String:
                return parseString(toParse as String, context)
            case Collection:
                return parseCollection(toParse, context)
            default:
                return parseSingleElement(toParse, context)
        }
    }
}
