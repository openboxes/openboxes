package org.pih.warehouse.core.mapper

import org.apache.commons.collections4.map.MultiKeyMap
import org.springframework.stereotype.Component

/**
 * Caches components relating to mapping/converting data.
 */
@Component
class MapperComponentResolver {

    private final Map<Class, ResponseMapper> responseMappersBySourceType = [:]
    private final MultiKeyMap<Class, Mapper> mappersBySourceAndTargetType = [:]

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    MapperComponentResolver(final Optional<List<ResponseMapper>> responseMappers,
                            final Optional<List<Mapper>> mappers) {
        populateResponseMapperMap(responseMappers.orElse([]))
        populateMapperMap(mappers.orElse([]))
    }

    private void populateResponseMapperMap(List<ResponseMapper> responseMappers) {
        for (responseMapper in responseMappers) {
            Class sourceType = responseMapper.sourceType
            if (responseMappersBySourceType.containsKey(sourceType)) {
                throw new RuntimeException(
                        "Found multiple response mappers for source type ${sourceType}. Only one is allowed.")
            }
            responseMappersBySourceType.put(sourceType, responseMapper)
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
     * @return The response mapper associated with the given source type.
     */
    ResponseMapper getResponseMapper(Class sourceType) {
        responseMappersBySourceType.get(sourceType)
    }

    /**
     * @return All response mappers. Should only be used when registering JSON marshallers.
     */
    Map<Class, ResponseMapper> getAllResponseMappers() {
        return responseMappersBySourceType
    }

    /**
     * @return The mapper associated with the given source and target type.
     */
    def <Source, Target> Mapper<Source, Target> getMapper(Class<Source> sourceType, Class<Target> targetType) {
        return (Mapper<Source, Target>) mappersBySourceAndTargetType.get(sourceType, targetType)
    }
}
