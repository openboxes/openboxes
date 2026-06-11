package org.pih.warehouse.core.http

import com.fasterxml.jackson.databind.ObjectMapper
import grails.converters.JSON
import grails.gorm.PagedResultList
import org.springframework.stereotype.Component

import org.pih.warehouse.core.mapper.MapperComponentResolver
import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * For converting objects into JSON, typically for use in API responses.
 */
@Component
class JsonSerializer {

    final MapperComponentResolver mapperComponentResolver
    final ObjectMapper objectMapper

    JsonSerializer(final MapperComponentResolver mapperComponentResolver,
                   final ObjectMapper objectMapper) {
        this.mapperComponentResolver = mapperComponentResolver
        this.objectMapper = objectMapper
    }

    /**
     * Converts the given object to JSON.
     *
     * @param toSerialize The object to convert to JSON
     * @param context Configures the serialization process
     */
    JSON serialize(Object toSerialize, JsonSerializerContext context) {
        Object result = doSerialize(toSerialize, context)

        // Note that grails.converters.JSON is the one that will do the actual serialization to JSON. Eventually
        // we want our JsonSerializer to perform the serialization itself here, but for now we rely on Grails.
        // TODO: Investigate ObjectMapper.writeValueAsString because it's much faster and supports Jackson annotations.
        return buildJsonResponse(toSerialize, result, context) as JSON
    }

    private Object doSerialize(Object toSerialize, JsonSerializerContext context) {
        switch (toSerialize) {
            case Collection:
                return serializeCollection(toSerialize, context)
            case Map:
                // We assume that any map we're given has already been serialized.
                return toSerialize
            case null:
                return null
            default:
                return serializeObject(toSerialize)
        }
    }

    /**
     * @return A standardized JSON response Map to be serialized.
     */
    private Map<String, Object> buildJsonResponse(Object toSerialize, Object result, JsonSerializerContext context) {
        Map<String, Object> json = [
                data: result,
                status: context.status,
        ]
        if (toSerialize instanceof Collection) {
            json.put("count", toSerialize.size())
            if (toSerialize instanceof PagedResultList) {
                json.put("totalCount", toSerialize.totalCount)
            }
        }

        // We will rely on the framework to serialize the additional fields for us.
        json.putAll(context.additionalFields)

        return json
    }

    private Object serializeCollection(Collection toSerialize, JsonSerializerContext context) {
        List result = []
        for (elementToSerialize in toSerialize) {
            result.add(doSerialize(elementToSerialize, context))
        }
        return result
    }

    private Object serializeObject(Object toSerialize) {
        ResponseMapper responseMapper = mapperComponentResolver.getResponseMapper(toSerialize.class)
        if (responseMapper) {
            return responseMapper.asResponseBody(toSerialize)
        }

        if (toSerialize instanceof ResponseBodyFormattable) {
            return toSerialize.asResponseBody()
        }

        // Maintained for backwards compatibility. New Dtos should not rely on this method.
        if (toSerialize.metaClass.respondsTo(toSerialize, "toJson")) {
            return toSerialize.toJson()
        }

        // We don't know how to serialize the object so we will rely on the framework to do it for us.
        return toSerialize
    }
}
