package org.pih.warehouse.component.api.product

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.component.api.base.ApiWrapper

@TestComponent
@InheritConstructors
class ProductApiWrapper extends ApiWrapper<ProductApi> {

    JsonPath listOK() {
        return api.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getDemandOK(String productId) {
        return api.getDemand(productId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response getDemand404(String productId) {
        return api.getDemand(productId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    JsonPath getDemandSummaryOK(String productId) {
        return api.getDemandSummary(productId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response getDemandSummary404(String productId) {
        return api.getDemandSummary(productId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    JsonPath getProductSummaryOK(String productId) {
        return api.getProductSummary(productId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response getProductSummary404(String productId) {
        return api.getProductSummary(productId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
