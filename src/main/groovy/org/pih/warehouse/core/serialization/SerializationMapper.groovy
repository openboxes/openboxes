package org.pih.warehouse.core.serialization

import org.springframework.core.GenericTypeResolver

/**
 * General purpose converter between an object and all our supported serialization-ready formats.
 *
 * Is called the serialization mapper (instead of just serializer) because the object is converted into a Map of raw
 * data that is ready to be serialized. It does not perform the serialization itself. The serialization is a separate
 * process, and will depend on the transportation method of the data (API response vs message queue, vs log file).
 *
 * If you only need to convert a simple object to JSON without performing any transforms on the object, you don't
 * need to implement this interface. You can instead simply rely on Jackson annotations in your object.
 *
 * @param <S> The class of the object being serialized.
 */
trait SerializationMapper<S extends Serializable> implements StructuredDataSerializer<S>, TabularDataSerializer<S> {

    Class<S> getSerializableType() {
        return (Class<S>) GenericTypeResolver.resolveTypeArgument(getClass(), SerializationMapper.class)
    }

    @Override
    Map<String, Object> serializeTabular(S serializable) {
        // If you want to support a tabular (CSV/XLS) data structure (which is required for bulk data exports),
        // you must override this method and declare how that mapping works since it likely is formatted differently
        // than structured (JSON) data.
        throw new UnsupportedOperationException("We do not support mapping to tabular data for ${serializableType}.")
    }
}
