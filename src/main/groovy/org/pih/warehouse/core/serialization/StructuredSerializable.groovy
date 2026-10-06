package org.pih.warehouse.core.serialization

/**
 * Indicates that the object can be serialized into a structured format by the given serializer.
 *
 * @param <S> The serializer of the object.
 */
interface StructuredSerializable<S extends StructuredDataSerializer> {

}
