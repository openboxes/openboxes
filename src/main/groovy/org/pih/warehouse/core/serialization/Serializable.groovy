package org.pih.warehouse.core.serialization

/**
 * Indicates that the object can be serialized into all our supported formats by the given serializer.
 *
 * @param <S> The serializer of the object.
 */
trait Serializable<S extends SerializationMapper> implements StructuredSerializable<S>,
                                                             TabularSerializable<S>,
                                                             java.io.Serializable {

}
