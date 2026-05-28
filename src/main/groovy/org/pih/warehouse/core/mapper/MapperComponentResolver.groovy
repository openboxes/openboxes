package org.pih.warehouse.core.mapper

import org.springframework.stereotype.Component

/**
 * Caches components relating to mapping/converting data.
 */
@Component
class MapperComponentResolver {

    private final HashMap<Class, ResponseMapper> responseMappersBySourceType = [:]

    // Components are wrapped with optional to avoid an error when no implementations are defined.
    MapperComponentResolver(final Optional<List<ResponseMapper>> responseMappers) {
        populateResponseMapperMap(responseMappers.orElse([]))
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

    /**
     * @return The response mapper associated with the given source type.
     */
    ResponseMapper getResponseMapper(Class sourceType) {
        responseMappersBySourceType.get(sourceType)
    }

    /**
     * @return All response mappers. Should only be used when registering JSON marshallers.
     */
    HashMap<Class, ResponseMapper> getAllResponseMappers() {
        return responseMappersBySourceType
    }
}
