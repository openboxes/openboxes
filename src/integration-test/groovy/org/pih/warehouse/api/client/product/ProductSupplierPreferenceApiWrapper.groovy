package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper

@TestComponent
@InheritConstructors
class ProductSupplierPreferenceApiWrapper extends ApiWrapper<ProductSupplierPreferenceApi> {

    String createOK(JSONObject body) {
        return api.create(body.toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
                .jsonPath()
                .getString("data.id")
    }

    JsonPath deleteOK(String productSupplierPreferenceId) {
        return api.delete(productSupplierPreferenceId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC).jsonPath()
    }
}
