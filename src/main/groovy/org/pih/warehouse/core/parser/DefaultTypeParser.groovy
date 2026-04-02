package org.pih.warehouse.core.parser

import org.springframework.stereotype.Component

/**
 * A convenience wrapper on all of the default parser components.
 */
@Component
class DefaultTypeParser {

    /**
     * Maps a type to the default parser associated with that type. For example, Integer : IntegerParser.
     */
    private final Map<Class, Parser> parsersByType = [:]

    DefaultTypeParser(List<Parser> parsers) {
        // Build the map of default parsers, keyed on the output type of the parser
        for (Parser parser in parsers) {
            if (!parser.isDefaultParserForType()) {
                continue
            }

            Class type = parser.getTargetType()
            if (parsersByType.containsKey(type)) {
                throw new RuntimeException("Found multiple default parsers for type ${type}. Only one is allowed.")
            }
            parsersByType.put(type, parser)
        }
    }

    /**
     * Converts the given object to the given type using the default parser associated with that type.
     * For example, if clazzToParseTo == Integer, this will parse using the IntegerParser.
     *
     * @throws IllegalArgumentException If the given type does not have a default parser.
     */
    public <T> T parse(Object toParse, Class<T> clazzToParseTo, ParserContext<T> context=null) {
        Parser parser = parsersByType.get(clazzToParseTo)
        if (!parser) {
            throw new IllegalArgumentException("No default parser exists for class ${clazzToParseTo}")
        }
        return parser.parse(toParse, context) as T
    }
}
