package org.pih.warehouse.core.http

/**
 * Defines an object that can be formatted as an HTTP response body contents (such as JSON or XML).
 *
 * Note that we are not serializing the response here, only converting the object to a Map so that it is easier
 * to serialize it later.
 *
 * This interface exists purely for convenience when converting simple objects that don't depend on anything else.
 * If you need more complex behaviour, such as localization or calling into a component, don't extend this class.
 * Instead, create a child of {@link org.pih.warehouse.core.mapper.ResponseMapper}.
 *
 * Thanks to this interface, we no longer need to manually call JSON.registerObjectMarshaller in BootStrap.groovy
 * for every new Dto that we add.
 */
interface ResponseBodyFormattable {

    /**
     * Converts an object to a Map for use in an API response body, such as for JSON or XML.
     *
     * Nesting complex objects is allowed, so long as those objects also implement ResponseBodyFormattable
     * or have an associated ResponseMapper component.
     *
     * @return a Map of values keyed on field name
     */
    Map<String, Object> asResponseBody()
}
