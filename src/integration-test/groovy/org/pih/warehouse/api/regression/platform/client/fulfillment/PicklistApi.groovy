package org.pih.warehouse.api.regression.platform.client.fulfillment

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The picklist API (/api/picklists...): one method per operation.
 */
@TestComponent
@InheritConstructors
class PicklistApi extends AuthenticatedApi {

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
        return call(Method.GET, '/picklists', null, null, query, responseSpec)
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/picklists', null, body, null, responseSpec)
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return call(Method.GET, '/picklists/{id}', id, null, null, responseSpec)
    }

    Response updateWithPost(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/picklists/{id}', id, body, null, responseSpec)
    }

    Response update(String id, String body, ResponseSpecification responseSpec) {
        return call(Method.PUT, '/picklists/{id}', id, body, null, responseSpec)
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/picklists/{id}', id, null, null, responseSpec)
    }

    Response clearPicklist(String id, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/picklists/{id}/items', id, null, null, responseSpec)
    }
}
