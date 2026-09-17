package org.pih.warehouse.core.serialization

import org.springframework.core.GenericTypeResolver

/**
 * Converter between an object and a format that is ready to be serialized.
 *
 * Is called the serialization mapper (instead of serializer) because it maps the object into a Map of raw data that
 * is ready to be serialized. It does not perform the serialization itself.
 *
 * The serialization is a separate process and will depend on the transportation method of the data (API response vs message queue, vs log file)
 *
 * If you are trying to convert a simple source object to json and don't depend on any other components, there
 * is no need to implement this interface. You can instead rely on Jackson annotations in your object.
 *
 * Thanks to this interface, we no longer need to manually call JSON.registerObjectMarshaller in BootStrap.groovy
 * for every new Dto that we add.
 *
 * @param <S>
 */
trait SerializationMapper<S extends Serializable> implements StructuredDataSerializer<S>, TabularDataSerializer<S> {

    Class<S> getSerializableType() {
        return (Class<S>) GenericTypeResolver.resolveTypeArgument(getClass(), SerializationMapper.class)
    }

    @Override
    Map<String, Object> serializeTabular(S serializable) {
        throw new UnsupportedOperationException("We do not support mapping to tabular data for ${serializableType}.")
    }
}