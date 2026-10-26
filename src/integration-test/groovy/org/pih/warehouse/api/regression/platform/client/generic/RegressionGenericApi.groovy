package org.pih.warehouse.api.regression.platform.client.generic

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The generic API client keyed by a resource STRING instead of upstream's GenericResource enum:
 * upstream's operations plus updateWithPost, search and listByName.
 * Port: `GenericApi` -> `RegressionGenericApi`, `GenericResource.X` -> `RegressionGenericResource.X`.
 */
@TestComponent
@InheritConstructors
class RegressionGenericApi extends AuthenticatedApi {

    Response list(String resource, ResponseSpecification responseSpec) {
        return request(spec(resource, null, null), responseSpec, Method.GET, "/generic/{resource}")
    }

    Response get(String resource, String id, ResponseSpecification responseSpec) {
        return request(spec(resource, id, null), responseSpec, Method.GET, "/generic/{resource}/{id}")
    }

    Response create(String resource, String body, ResponseSpecification responseSpec) {
        return request(spec(resource, null, body), responseSpec, Method.POST, "/generic/{resource}")
    }

    Response update(String resource, String id, String body, ResponseSpecification responseSpec) {
        return request(spec(resource, id, body), responseSpec, Method.PUT, "/generic/{resource}/{id}")
    }

    Response delete(String resource, String id, ResponseSpecification responseSpec) {
        return request(spec(resource, id, null), responseSpec, Method.DELETE, "/generic/{resource}/{id}")
    }

    /** POST /api/generic/{resource}/{id}: the same update as PUT. */
    Response updateWithPost(String resource, String id, String body, ResponseSpecification responseSpec) {
        return request(spec(resource, id, body), responseSpec, Method.POST, "/generic/{resource}/{id}")
    }

    /** @param body {"searchAttributes": [{"property": ..., "operator": "eq"|"like"|"ilike", "value": ...}]} */
    Response search(String resource, String body, Method method, ResponseSpecification responseSpec) {
        return request(spec(resource, null, body), responseSpec, method, "/generic/{resource}/search")
    }

    /** A resource name the generic API may not know: sent as given. */
    Response listByName(String resourceName, ResponseSpecification responseSpec) {
        return list(resourceName, responseSpec)
    }

    private static RequestSpecification spec(String resource, String id, String body) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam("resource", resource)
        if (id != null) {
            builder.addPathParam("id", id)
        }
        if (body != null) {
            builder.setBody(body)
        }
        return builder.build()
    }
}
