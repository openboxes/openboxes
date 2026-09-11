package org.pih.warehouse.core.mapper

import org.springframework.core.GenericTypeResolver

/**
 * Converts a source object into a format suitable for use in an API response. Note that we are not serializing
 * the response here, only converting the object to a Map so that it is easier to serialize it later.
 *
 * If you are trying to convert a simple source object to json and don't depend on any other components, there
 * is no need to implement this interface. You can instead rely on Jackson annotations in your object.
 *
 * Thanks to this interface, we no longer need to manually call JSON.registerObjectMarshaller in BootStrap.groovy
 * for every new Dto that we add.
 *
 * @param <Source> The object to convert.
 */
trait ResponseMapper<Source> {

    Class<Source> getSourceType() {
        return (Class<Source>) GenericTypeResolver.resolveTypeArgument(getClass(), ResponseMapper.class)
    }

    /**
     * Converts an object to a Map for use in an API response body, such as for JSON or XML.
     *
     * Unlike a bulk data row, the objects in this map do not need to be flat. Nesting complex, hierarchical objects
     * will result in those child objects also being serialized as a part of the response.
     *
     * @param source The object to convert
     * @return a Map of values keyed on field name
     */
    abstract Map<String, Object> asResponseBody(Source source)

    /**
     * Converts an object to a Map for use bulk data export APIs, such as CSV or XLS/XLSX.
     *
     * A "bulk data" row differs from a response body representation of the data in that it must be flat.
     * This is because tabular data files (such as .csv) cannot easily represent nested data structures.
     *
     * As such, exporting will fail (with a loud, helpful message) if any values in the returned map are not primitives
     * or don't have a {@link org.pih.warehouse.core.formatter.Formatter} defined. For convenience, we default to
     * returning the same result as when formatting for a response body, but this method MUST be overridden if
     * asResponseBody returns any non-formattable values (such as class instances or nested collections).
     *
     * @param source The object to convert
     * @return a Map of values keyed on field name
     */
    Map<String, Object> asExportRow(Source source) {
        return asResponseBody(source)
    }
}
