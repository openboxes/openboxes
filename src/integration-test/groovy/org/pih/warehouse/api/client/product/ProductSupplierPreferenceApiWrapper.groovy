package org.pih.warehouse.api.client.product

import groovy.transform.InheritConstructors
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.product.ProductSupplierPreference

@TestComponent
@InheritConstructors
class ProductSupplierPreferenceApiWrapper extends ApiWrapper<ProductSupplierPreferenceApi> {

    ProductSupplierPreference createOK(ProductSupplierPreference productSupplierPreference) {
        return create(productSupplierPreference, HttpStatus.SC_CREATED)
                .jsonPath()
                .getObject("data", ProductSupplierPreference.class)
    }

    Response create(ProductSupplierPreference productSupplierPreference, int statusCode) {
        String body = asRequestBody(productSupplierPreference).toString()
        return api.create(body, responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }

    ProductSupplierPreference updateOK(ProductSupplierPreference productSupplierPreference) {
        String body = asRequestBody(productSupplierPreference).toString()
        return api.update(productSupplierPreference.id, body, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()
                .getObject("data", ProductSupplierPreference.class)
    }

    List<ProductSupplierPreference> createOrUpdateBatchOK(List<ProductSupplierPreference> productSupplierPreferences) {
        return createOrUpdateBatch(productSupplierPreferences, HttpStatus.SC_OK)
                .jsonPath()
                .getList("data", ProductSupplierPreference.class)
    }

    Response createOrUpdateBatch(List<ProductSupplierPreference> productSupplierPreferences, int statusCode) {
        String body = new JSONObject()
                .put('productSupplierPreferences', productSupplierPreferences.collect { asRequestBody(it) })
                .toString()
        return api.createOrUpdateBatch(body, responseSpecUtil.buildStatusCodeResponseSpec(statusCode))
    }

    JsonPath deleteOK(String productSupplierPreferenceId) {
        return api.delete(productSupplierPreferenceId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC).jsonPath()
    }

    private JSONObject asRequestBody(ProductSupplierPreference productSupplierPreference) {
        return new JSONObject()
                .put('id', productSupplierPreference.id)
                .put('productSupplier', jsonObjectUtil.asIdForRequestBody(productSupplierPreference.productSupplier))
                .put('preferenceType', jsonObjectUtil.asIdForRequestBody(productSupplierPreference.preferenceType))
                .put('comments', productSupplierPreference.comments)
    }
}
