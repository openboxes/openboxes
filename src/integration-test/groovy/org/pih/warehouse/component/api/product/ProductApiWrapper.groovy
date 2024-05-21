package org.pih.warehouse.component.api.product

import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

import org.pih.warehouse.component.api.base.ApiWrapper

class ProductApiWrapper extends ApiWrapper {

    ProductApi productApi

    ProductApiWrapper(RequestSpecification defaultRequestSpec) {
        super(defaultRequestSpec)

        productApi = new ProductApi(defaultRequestSpec)
    }

    JsonPath listOK() {
        return productApi.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    JsonPath getDemandOK(String productId) {
        return productApi.getDemand(productId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response getDemand404(String productId) {
        return productApi.getDemand(productId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    JsonPath getDemandSummaryOK(String productId) {
        return productApi.getDemandSummary(productId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response getDemandSummary404(String productId) {
        return productApi.getDemandSummary(productId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    JsonPath getProductSummaryOK(String productId) {
        return productApi. getProductSummary(productId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    Response getProductSummary404(String productId) {
        return productApi.getProductSummary(productId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
