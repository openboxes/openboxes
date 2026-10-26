package org.pih.warehouse.api.regression.platform.client.stockmovement

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The stock movement item API (/api/stockMovementItems...): one method per operation.
 */
@TestComponent
@InheritConstructors
class StockMovementItemApi extends AuthenticatedApi {

    private Response call(Method method, String path, String id, String body, Map<String, ?> query,
                          ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (id != null) {
            builder.addPathParam('id', id)
        }
        if (body != null) {
            builder.setBody(body)
        }
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, method, path)
    }

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovementItems', null, null, query, responseSpec)
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovementItems/{id}', id, null, null, responseSpec)
    }

    Response details(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/stockMovementItems/{id}/details', id, null, null, responseSpec)
    }

    Response cancelItem(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovementItems/{id}/cancelItem', id, body, null, responseSpec)
    }

    Response clearPicklist(String id, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovementItems/{id}/clearPicklist', id, null, null, responseSpec)
    }

    Response createPicklist(String id, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovementItems/{id}/createPicklist', id, null, null, responseSpec)
    }

    Response updatePicklist(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovementItems/{id}/updatePicklist', id, body, null, responseSpec)
    }

    Response revertPick(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/stockMovementItems/{id}/picklistItems', id, null, null, responseSpec)
    }

    Response eraseItem(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/stockMovementItems/{id}/removeItem', id, null, null, responseSpec)
    }

    Response revertItem(String id, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovementItems/{id}/revertItem', id, null, null, responseSpec)
    }

    Response substituteItem(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/stockMovementItems/{id}/substituteItem', id, body, null, responseSpec)
    }
}
