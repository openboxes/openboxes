package org.pih.warehouse.core.serialization

/**
 * Converter between an object and a structured/hierarchical representation of that object's data that is ready
 * to be serialized.
 *
 * @param <S> The class of the object being serialized.
 */
interface StructuredDataSerializer<S extends Serializable> {

    /**
     * Converts an object to a Map of structured/hierarchal data that is ready to be serialized.
     *
     * Used for: JSON, XML
     *
     * Structured data formats differ from their hierarchal counterparts in that they do not need to be flat. Nesting
     * complex, hierarchical objects will result in those child objects also being serialized.
     *
     * @param serializable The object to serialize
     * @return a Map of values keyed on field name
     */
    Map<String, Object> serialize(S serializable)
}