package org.pih.warehouse.component.api.product

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.component.api.base.ApiWrapper

@TestComponent
@InheritConstructors
class CategoryApiWrapper extends ApiWrapper<CategoryApi> {

    JsonPath listOK() {
        return api.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getOK(String categoryId) {
        return api.get(categoryId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    String createOK(JSONObject body) {
        JsonPath json = api.create(body.toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        return json.getString('id')
    }

    JsonPath deleteOK(String categoryId) {
        return api.delete(categoryId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }
}
