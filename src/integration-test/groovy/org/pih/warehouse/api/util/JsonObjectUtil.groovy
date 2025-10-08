package org.pih.warehouse.api.util

import org.grails.datastore.gorm.GormEntity
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

@TestComponent
class JsonObjectUtil {

    /**
     * Converts an entity to a Json Object containing a single id field. For example: "{ id: 1 }"
     *
     * This Json Object can be set in a request body and on the server side it will be bound to the full object
     * by fetching it from the database by id.
     */
    JSONObject asIdForRequestBody(GormEntity entity) {
        if (!entity) {
            return null
        }

        if (!entity.hasProperty('id')) {
            throw new IllegalArgumentException("${entity} does not have an 'id' field that we can extract")
        }

        return new JSONObject().put('id', entity.id as String)
    }

    /**
     * Converts a collection of entities to a Json Array of Json Objects, each containing a single id field.
     * For example: "[{ id: 1 }, ...]"
     *
     * This Json Array can be set in a request body and on the server side each element will be bound to the
     * full object by fetching it from the database by id.
     */
    JSONArray asIdsForRequestBody(Collection<GormEntity> entities) {
        JSONArray array = new JSONArray()
        if (!entities) {
            return array
        }

        for (GormEntity entity in entities) {
            array.add(asIdForRequestBody(entity))
        }
        return array
    }

    /**
     * Converts a Date instance to a String for use in a request body.
     */
    String asDateForRequestBody(Date date) {
        return date?.toInstant()?.toString()
    }
}
