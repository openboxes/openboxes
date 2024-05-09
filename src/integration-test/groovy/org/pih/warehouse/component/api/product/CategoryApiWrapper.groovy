package org.pih.warehouse.component.api.product

import io.restassured.path.json.JsonPath
import io.restassured.specification.RequestSpecification
import org.grails.web.json.JSONObject

import org.pih.warehouse.component.api.base.ApiWrapper

class CategoryApiWrapper extends ApiWrapper {

    CategoryApi categoryApi

    CategoryApiWrapper(RequestSpecification defaultRequestSpec) {
        super(defaultRequestSpec)

        categoryApi = new CategoryApi(defaultRequestSpec)
    }

    JsonPath listOK() {
        return categoryApi.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getOK(String categoryId) {
        return categoryApi.get(categoryId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    String createOK(JSONObject body) {
        JsonPath json = categoryApi.create(body.toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        return json.getString('id')
    }

    JsonPath deleteOK(String categoryId) {
        return categoryApi.delete(categoryId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }
}
