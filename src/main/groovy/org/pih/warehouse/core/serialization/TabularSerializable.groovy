package org.pih.warehouse.core.serialization

/**
 * Indicates that the object can be serialized into a tabular format by the given serializer.
 *
 * @param <S> The serializer of the object.
 */
interface TabularSerializable<S extends TabularDataSerializer> {

}
