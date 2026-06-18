package org.pih.warehouse.core.http

import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.PagedResultList
import org.springframework.stereotype.Component

/**
 * For converting objects into JSON strings for use in API responses.
 */
@Component
class JsonSerializer {

    final ObjectMapper objectMapper

    JsonSerializer(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper
    }

    /**
     * Converts the given object to a standardized JSON string for use in an API response.
     *
     * @param toSerialize The object to convert to JSON
     * @param context Configures the serialization process
     */
    String serialize(Object toSerialize, JsonSerializerContext context) {
        Map jsonResponse = buildJsonResponse(toSerialize, context)
        return objectMapper.writeValueAsString(jsonResponse)
    }

    /**
     * Build Map representing a standardized JSON response to be serialized.
     *
     * The fields that will be included are:
     * - data: The response body content
     * - status: The HTTP status code of the response
     * - count: If data is a list, this counts the number of elements being returned in that list
     * - totalCount: If data is a paginated list, this counts the total number of elements across *all* pages (not only
     *               the count of elements in the current page of data that is being returned in this response body).
     */
    private Map<String, Object> buildJsonResponse(Object toSerialize, JsonSerializerContext context) {
        Map<String, Object> json = [
                data: toSerialize,
                status: context.status,
        ]
        if (toSerialize instanceof Collection) {
            json.put("count", toSerialize.size())
            if (toSerialize instanceof PagedResultList) {
                json.put("totalCount", toSerialize.totalCount)
            }
        }

        // We will rely on Jackson to serialize the additional fields for us.
        json.putAll(context.additionalFields)

        return json
    }
}
