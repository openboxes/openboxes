package org.pih.warehouse.core.http

import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Defines an object that can be formatted for an HTTP response, either as the contents of the response body (JSON)
 * or as a row in a bulk data export file (CSV, XLS).
 *
 * Despite the name, we are not actually serializing the response here, only converting the object to a Map that will
 * later be serialized.
 *
 * If you are not overriding the default serialization behaviour, you do not need to implement this trait.
 *
 * This trait exists solely for overriding the default serialization behaviour when you don't depend on application
 * context, such as localization or calling into a component. If you do need that behaviour, don't extend this class.
 * Instead, create a {@link ResponseMapper} component (and have your object implement {@link HasResponseMapper}).
 *
 * Thanks to this interface, we no longer need to manually call JSON.registerObjectMarshaller in BootStrap.groovy
 * for every new Dto that we add.
 */
trait HttpSerializable {

    /**
     * Converts an object to a Map for use in an API response body, such as for JSON or XML.
     *
     * Nesting complex objects is allowed. If the object implements this trait or {@link ResponseMapper},
     * then they will be serialized that way, otherwise they will use the default serialization process.
     *
     * @return a Map of values keyed on field name
     */
    abstract Map<String, Object> asResponseBody()

    /**
     * Converts an object to a Map for use in bulk data export APIs, such as CSV or XLS/XLSX.
     *
     * A "bulk data" row differs from a response body representation of the data in that it must be flat.
     * This is because tabular data files (such as .csv) cannot easily represent nested data structures.
     *
     * As such, exporting will fail (with a loud, helpful message) if any values in the returned map are not primitives
     * or don't have a {@link org.pih.warehouse.core.formatter.Formatter} defined. For convenience, we default to
     * returning the same result as when formatting for a response body, but this method MUST be overridden if
     * asResponseBody returns any non-formattable values (such as class instances or nested collections).
     *
     * @return a Map of values keyed on field name
     */
    Map<String, Object> asExportRow() {
        return asResponseBody()
    }
}
