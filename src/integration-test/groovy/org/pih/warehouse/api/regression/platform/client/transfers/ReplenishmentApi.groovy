package org.pih.warehouse.api.regression.platform.client.transfers

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The bin replenishment API (/api/requirements, /api/replenishments...): one method per operation. A replenishment
 * is a transfer order within a facility, from bins with stock to bins below their minimum; the picklist operations
 * take the id of one of its order items.
 */
@TestComponent
@InheritConstructors
class ReplenishmentApi extends AuthenticatedApi {

    Response requirements(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/requirements')
    }

    /** The replenishment page creates replenishments here; POST /api/replenishments does the same. */
    Response createFromRequirements(String body, ResponseSpecification responseSpec) {
        return request(withBody(body), responseSpec, Method.POST, '/requirements')
    }

    Response list(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/replenishments')
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return request(withBody(body), responseSpec, Method.POST, '/replenishments')
    }

    Response statusOptions(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/replenishments/statusOptions')
    }

    /** @param trailingSlash the web app calls /api/replenishments/{id}/ as well as /api/replenishments/{id} */
    Response read(String id, boolean trailingSlash, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.GET, path(trailingSlash))
    }

    Response update(String id, Method method, boolean trailingSlash, String body, ResponseSpecification responseSpec) {
        return request(withId(id, body), responseSpec, method, path(trailingSlash))
    }

    Response removeItem(String orderItemId, ResponseSpecification responseSpec) {
        return request(withId(orderItemId), responseSpec, Method.DELETE, '/replenishments/{id}/removeItem')
    }

    Response getPicklist(String orderItemId, ResponseSpecification responseSpec) {
        return request(withId(orderItemId), responseSpec, Method.GET, '/replenishments/{id}/picklists')
    }

    /** Recreates the picklist of every item of the replenishment (takes the replenishment id). */
    Response createPicklist(String replenishmentId, ResponseSpecification responseSpec) {
        return request(withId(replenishmentId), responseSpec, Method.POST, '/replenishments/{id}/picklists')
    }

    Response updatePicklist(String orderItemId, String body, ResponseSpecification responseSpec) {
        return request(withId(orderItemId, body), responseSpec, Method.PUT, '/replenishments/{id}/picklists')
    }

    Response deletePicklist(String orderItemId, ResponseSpecification responseSpec) {
        return request(withId(orderItemId), responseSpec, Method.DELETE, '/replenishments/{id}/picklists')
    }

    /** Picks one item again from the suggested bins. */
    Response createPicklistItem(String orderItemId, ResponseSpecification responseSpec) {
        return request(withId(orderItemId, '{}'), responseSpec, Method.POST, '/replenishments/{id}/picklistItem')
    }

    private static String path(boolean trailingSlash) {
        return trailingSlash ? '/replenishments/{id}/' : '/replenishments/{id}'
    }

    private static RequestSpecification withId(String id, String body = null) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('id', id)
        if (body != null) {
            builder.setBody(body)
        }
        return builder.build()
    }

    private static RequestSpecification withBody(String body) {
        return new RequestSpecBuilder().setBody(body).build()
    }

    private static RequestSpecification withQuery(Map<String, ?> query) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return builder.build()
    }
}
