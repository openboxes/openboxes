package org.pih.warehouse.component.api.generic

import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.json.JSONObject

import org.pih.warehouse.component.api.base.ApiWrapper
import org.pih.warehouse.util.generic.GenericResource

class GenercApiWrapper extends ApiWrapper {

    GenericApi genericApi

    GenercApiWrapper(RequestSpecification defaultRequestSpec) {
        super(defaultRequestSpec)

        genericApi = new GenericApi(defaultRequestSpec)
    }

    JsonPath listOK(GenericResource resource) {
        return genericApi.list(resource, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getOK(GenericResource resource, String id) {
        return genericApi.get(resource, id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response get404(GenericResource resource, String id) {
        return genericApi.get(resource, id, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    JsonPath createOK(GenericResource resource, JSONObject body) {
        return genericApi.create(resource, body.toString(), responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath()
    }

    JsonPath updateOK(GenericResource resource, String id, JSONObject body) {
        return genericApi.update(resource, id, body.toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response deleteOK(GenericResource resource, String id) {
        Response response = genericApi.delete(resource, id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        createdResources.get(resource).remove(id)

        return response
    }
}
