package org.pih.warehouse.core.formatter

import org.springframework.stereotype.Component

/**
 * A convenience wrapper on all of the default formatter components.
 * Allows you to format any object to a String, so long there is a default formatter associated with that type.
 */
@Component
class DefaultTypeFormatter {

    /**
     * Maps a type to the default formatter associated with that type. For example [Integer : IntegerFormatter].
     */
    private final Map<Class, Formatter> formattersByType = [:]

    DefaultTypeFormatter(final Optional<List<Formatter>> formatters) {
        // Build the map of default formatters, keyed on the source type of the formatter
        for (Formatter formatter in formatters.orElse([])) {
            if (!formatter.isDefaultFormatterForType()) {
                continue
            }

            Class type = formatter.sourceType
            if (formattersByType.containsKey(type)) {
                throw new RuntimeException("Found multiple default formatters for type ${type}. Only one is allowed.")
            }
            formattersByType.put(type, formatter)
        }
    }

    /**
     * Converts the given object to a String using the default formatter associated with that type.
     * For example, if toFormat.class == Integer, this will use the IntegerFormatter.
     *
     * @throws IllegalArgumentException If the given type does not have a default formatter.
     */
    String format(Object toFormat, FormatterContext context=null) {
        Formatter formatter = formattersByType.get(toFormat?.class)
        if (!formatter) {
            throw new IllegalArgumentException("No default formatter exists for class ${toFormat?.class}")
        }
        return formatter.format(toFormat, context)
    }
}
