package org.pih.warehouse.api.client.generic

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class GenericApiWrapper extends ApiWrapper<GenericApi> {


    JsonPath listOK(GenericResource resource) {
        return api.list(resource, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getOK(GenericResource resource, String id) {
        return api.get(resource, id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response get404(GenericResource resource, String id) {
        return api.get(resource, id, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    JsonPath createOK(GenericResource resource, JSONObject body) {
        return api.create(resource, body.toString(), responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath()
    }

    JsonPath updateOK(GenericResource resource, String id, JSONObject body) {
        return api.update(resource, id, body.toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response deleteOK(GenericResource resource, String id) {
        return api.delete(resource, id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
    }
}
