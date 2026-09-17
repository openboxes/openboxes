package org.pih.warehouse.core.serialization

/**
 * Converter between object and structured, hierarchical data format that is ready to be serialized.
 * @param <S>
 */
interface StructuredDataSerializer<S extends Serializable> {

    /**
     * Converts an object to a Map for use in an API response body, such as for JSON or XML.
     *
     * Unlike a bulk data row, the objects in this map do not need to be flat. Nesting complex, hierarchical objects
     * will result in those child objects also being serialized as a part of the response.
     *
     * @param serializable The object to convert
     * @return a Map of values keyed on field name
     */
    Map<String, Object> serialize(S serializable)
}