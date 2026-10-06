package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.core.LocationGroup

@TestComponent
@InheritConstructors
class LocationGroupApiWrapper extends ApiWrapper<LocationGroupApi> {

    String createOK(LocationGroup locationGroup) {
        String body = new JSONObject()
                .put('name', locationGroup.name)
                .toString()
        return api.create(body, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getString("data.id")
    }

    LocationGroup updateOK(String locationGroupId, JSONObject body) {
        return api.update(locationGroupId, body.toString(), responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getObject("data", LocationGroup.class)
    }

    JsonPath deleteOK(String locationGroupId) {
        return api.delete(locationGroupId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC).jsonPath()
    }
}
