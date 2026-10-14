package org.pih.warehouse.core.mapper

import org.apache.commons.collections4.map.MultiKeyMap
import org.springframework.stereotype.Component

import org.pih.warehouse.core.serialization.SerializationMapper

/**
 * Caches components relating to mapping/converting data.
 */
@Component
class MapperComponentResolver {

    private final Map<Class, SerializationMapper> serializationMappersBySourceType = [:]
    private final MultiKeyMap<Class, Mapper> mappersBySourceAndTargetType = new MultiKeyMap<>()

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    MapperComponentResolver(final Optional<List<SerializationMapper>> serializationMappers,
                            final Optional<List<Mapper>> mappers) {
        populateSerializationMapperMap(serializationMappers.orElse([]))
        populateMapperMap(mappers.orElse([]))
    }

    private void populateSerializationMapperMap(List<SerializationMapper> serializationMappers) {
        for (serializationMapper in serializationMappers) {
            Class serializableType = serializationMapper.serializableType
            if (serializationMappersBySourceType.containsKey(serializableType)) {
                throw new RuntimeException("Found multiple serialization mappers for serializable type " +
                        "${serializableType}. Only one is allowed.")
            }
            serializationMappersBySourceType.put(serializableType, serializationMapper)
        }
    }

    private void populateMapperMap(List<Mapper> mappers) {
        for (mapper in mappers) {
            Class sourceType = mapper.sourceType
            Class targetType = mapper.targetType
            if (mappersBySourceAndTargetType.containsKey(sourceType, targetType)) {
                throw new RuntimeException("Found multiple mappers for source type ${sourceType} and " +
                        "target type ${targetType}. Only one is allowed.")
            }
            mappersBySourceAndTargetType.put(sourceType, targetType, mapper)

            // If the mapper is bi-directional, store the inverse mapping relationship as well since it can handle both.
            if (mapper instanceof BidirectionalMapper) {
                if (mappersBySourceAndTargetType.containsKey(targetType, sourceType)) {
                    throw new RuntimeException("Found multiple mappers for source type ${targetType} and " +
                            "target type ${sourceType}. Only one is allowed.")
                }
                mappersBySourceAndTargetType.put(targetType, sourceType, mapper)
            }
        }
    }

    /**
     * @return The serialization mapper associated with the given source type.
     */
    SerializationMapper getSerializationMapper(Class sourceType) {
        serializationMappersBySourceType.get(sourceType)
    }

    /**
     * @return All serialization mappers. Should only be used when registering JSON marshallers.
     */
    Map<Class, SerializationMapper> getAllSerializationMappers() {
        return serializationMappersBySourceType
    }

    /**
     * @return The mapper associated with the given source and target type.
     */
    def <Source, Target> Mapper<Source, Target> getMapper(Class<Source> sourceType, Class<Target> targetType) {
        return (Mapper<Source, Target>) mappersBySourceAndTargetType.get(sourceType, targetType)
    }
}
