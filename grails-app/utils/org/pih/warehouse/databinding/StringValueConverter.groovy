package org.pih.warehouse.databinding

import grails.databinding.converters.ValueConverter
import org.springframework.core.GenericTypeResolver

/**
 * A convenience base class that our ValueConverters can extend from. Useful since a vast majority of the time, our
 * user inputs (request body, uri + query params) are Strings.
 *
 * @param <T> The type that we're converting to.
 */
abstract class StringValueConverter<T> implements ValueConverter {

    /**
     * Defines how to convert a user-input String to the necessary data type.
     */
    abstract T convertString(String value)

    @Override
    boolean canConvert(Object value) {
        return value instanceof String
    }

    @Override
    Class<?> getTargetType() {
        // The Spring *magic* way of extracting the type from a class.
        return (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), StringValueConverter.class)
    }

    @Override
    Object convert(Object value) {
        return convertString((String) value)
    }
}
