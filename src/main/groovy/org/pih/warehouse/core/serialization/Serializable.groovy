package org.pih.warehouse.core.serialization

/**
 * Marker
 */
trait Serializable<S extends SerializationMapper> implements StructuredSerializable<S>,
                                                             TabularSerializable<S>,
                                                             java.io.Serializable {

}