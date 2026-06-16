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
        Object result = doSerialize(toSerialize)

        // Note that grails.converters.JSON is the one that will do the actual serialization to JSON. Eventually
        // we want our JsonSerializer to perform the serialization itself here, but for now we rely on Grails.
        // TODO: Investigate ObjectMapper.writeValueAsString because it's much faster and supports Jackson annotations.
        return buildJsonResponse(toSerialize, result, context) as JSON
    }

    private Object doSerialize(Object toSerialize) {
        switch (toSerialize) {
            case Collection:
                return serializeCollection(toSerialize)
            case Map:
                return serializeMap(toSerialize)
            case null:
                return null
            default:
                return serializeObject(toSerialize)
        }
    }

    /**
     * Build a standardized JSON response Map to be serialized. Fields include:
     * - data: The response body content
     * - status: The HTTP status code of the response
     * - count: If data is a list, this counts the number of elements being returned in that list
     * - totalCount: If data is a paginated list, this counts the total number of elements across *all* pages (not only
     *               the count of elements in the current page of data that is being returned in this response body).
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

    /**
     * Loop the elements of the collection, serializing each element (into a Map if we're able, otherwise it is left
     * as is to be serialized by the framework).
     */
    private List serializeCollection(Collection toSerialize) {
        List result = []
        if (!toSerialize) {
            return result
        }
        for (elementToSerialize in toSerialize) {
            // We might be able to get a performance bump if we pre-fetch the mapper to use (if there is one)
            // by looking at the first element here instead of computing it per element in doSerialize. This would
            // require assuming that every element of the collection is of the same type (which is probably true).
            result.add(doSerialize(elementToSerialize))
        }
        return result
    }

    /**
     * Loop the values of the map, serializing each value (into its own Map if we're able, otherwise it is left
     * as is to be serialized by the framework).
     */
    private Map serializeMap(Map toSerialize) {
        Map serializedMap = [:]
        if (!toSerialize) {
            return serializedMap
        }
        for (entry in toSerialize) {
            // We might be able to get a performance bump if we pre-fetch the mapper to use (if there is one)
            // by looking at some map value here instead of computing it per value in doSerialize. This would
            // require assuming that every value of the map is of the same type (which is probably true).
            serializedMap.put(entry.key, doSerialize(entry.value))
        }
        return serializedMap
    }

    private Object serializeObject(Object toSerialize) {
        Map mappedData = null

        ResponseMapper responseMapper = mapperComponentResolver.getResponseMapper(toSerialize.class)
        if (responseMapper) {
            mappedData = responseMapper.asResponseBody(toSerialize)
        }

        else if (toSerialize instanceof ResponseBodyFormattable) {
            mappedData = toSerialize.asResponseBody()
        }

        // Maintained for backwards compatibility. New Dtos should not rely on this method.
        else if (toSerialize.metaClass.respondsTo(toSerialize, "toJson")) {
            mappedData = toSerialize.toJson()
        }

        /*
         * If we were able to perform any of the above, we will have serialized the data to a Map. We pass the Map
         * for further serializing so that we can also serialize the map's values. Doing so saves us from needing
         * to manually serialize child elements in the asResponseBody() methods of our objects/mappers.
         *
         * For example, we can define an object like:
         *
         * class X implements ResponseBodyFormattable {
         *
         *     Y childObject
         *
         *     Map<String, Object> asResponseBody() {
         *         return [
         *             childObject: childObject,
         *         ]
         *     }
         * }
         *
         * As long as Y implements ResponseBodyFormattable, has a ResponseMapper, or has a toJson() method,
         * the childObject will be automatically serialized.
         */
        if (mappedData != null) {
            return serializeMap(mappedData)
        }

        // We don't know how to serialize the object so we will rely on the framework to do it for us.
        // TODO: map it ourselves! Loop its fields and call doSerialize on all of them!
        //       no... then we can never use Jackson annotations nor rely on ObjectMapper for much of anything.
        //       better would be to be able to customize ObjectMapper to do this for us! When serializing a field
        //       after jackson annotations have been processed, do logic in doSerialize. Essentially move all logic
        //       in this component there and just have BaseController call ObjectMapper!

        //       can maybe do this by defining a class CustomJsonSerializer extends JsonSerializer<Object>
        //       which overrides ALL objects then stick my doSerialize logic in there?

        return toSerialize
    }
}
