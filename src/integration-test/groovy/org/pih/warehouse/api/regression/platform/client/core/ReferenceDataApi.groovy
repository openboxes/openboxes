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
 * Small read-only lists the pages load: reason codes, support links, the instance reset command, the demo data
 * summary. One method per operation.
 */
@TestComponent
@InheritConstructors
class ReferenceDataApi extends AuthenticatedApi {

    /** @param query activityCode (repeated) chooses the list */
    Response reasonCodes(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value instanceof Collection ? value as List : value) }
        return request(builder.build(), responseSpec, Method.GET, '/reasonCodes')
    }

    Response reasonCode(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.GET, '/reasonCodes/{id}')
    }

    Response supportLinks(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/supportLinks')
    }

    Response resettingInstanceCommand(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/resettingInstance/command')
    }

    Response listOfDemoData(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/loadData/listOfDemoData')
    }

    /** The request types the stock request wizard offers (GET /api/getRequestTypes). */
    Response requestTypes(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/getRequestTypes')
    }

    Response helpScoutConfiguration(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/helpscout/configuration')
    }

    /** @param entityType an EntityTypeCode name, or null for every attribute */
    Response attributes(String entityType, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (entityType != null) {
            builder.addQueryParam('entityType', entityType)
        }
        return request(builder.build(), responseSpec, Method.GET, '/attributes')
    }

    private static RequestSpecification withId(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }
}
