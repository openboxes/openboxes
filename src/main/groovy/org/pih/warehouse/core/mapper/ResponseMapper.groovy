package org.pih.warehouse.core.mapper

import org.springframework.core.GenericTypeResolver

/**
 * Converts a source object into a format suitable for use in an API response. Note that we are not serializing
 * the response here, only converting the object to a Map so that it is easier to serialize it later.
 *
 * If all you need to do is convert a simple source object to json, there is no need to implement this interface.
 * All you need to do is have the object implement {@link org.pih.warehouse.core.http.ResponseBodyFormattable}.
 *
 * Thanks to this interface, we no longer need to manually call JSON.registerObjectMarshaller in BootStrap.groovy
 * for every new Dto that we add.
 *
 * @param <Source> The object to convert.
 */
abstract class ResponseMapper<Source> {

    Class<Source> getSourceType() {
        return (Class<Source>) GenericTypeResolver.resolveTypeArgument(getClass(), ResponseMapper.class)
    }

    /**
     * Converts an object to a Map for use in an API response body, such as for JSON or XML.
     *
     * Unlike a bulk data row, the objects in this map do not need to be flat. Nesting complex objects
     * is allowed, so long as those objects also have a ResponseMapper or define a toJson() method.
     *
     * @param object The object to convert
     * @return a Map of values keyed on field name
     */
    abstract Map<String, Object> asResponseBody(Source source)

    /**
     * Converts an object to a Map for use bulk data export APIs, such as CSV or XLS/XLSX.
     *
     * A "bulk data" row differs from a response body representation of the data in that it must be flat.
     * This is because data files (such as .csv) cannot easily represent nested data structures.
     * As such, when being written to a file, all values of the map that this method returns will either
     * be formatted (if there is an associated *Formatter) or stringified.
     *
     * @param object The object to convert
     * @return a Map of values keyed on field name
     */
    abstract Map<String, Object> asExportRow(Source source)
}
