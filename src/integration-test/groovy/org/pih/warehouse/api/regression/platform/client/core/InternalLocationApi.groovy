package org.pih.warehouse.api.regression.platform.client.core

import org.pih.warehouse.api.client.core.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The locations inside a facility (/api/binLocations..., /api/internalLocations...): one method per operation.
 * Query parameters use the names the pages send (location.id, parentLocation.id, shipmentNumber, searchTerm...).
 */
@TestComponent
@InheritConstructors
class InternalLocationApi extends AuthenticatedApi {

    Response binLocations(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/binLocations')
    }

    Response binLocation(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.GET, '/binLocations/{id}')
    }

    Response internalLocations(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/internalLocations')
    }

    Response receivingLocations(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/internalLocations/receiving')
    }

    Response search(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/internalLocations/search')
    }

    Response internalLocation(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.GET, '/internalLocations/{id}')
    }

    private static RequestSpecification withId(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }

    private static RequestSpecification withQuery(Map<String, ?> query) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value instanceof Collection ? value as List : value) }
        return builder.build()
    }
}
