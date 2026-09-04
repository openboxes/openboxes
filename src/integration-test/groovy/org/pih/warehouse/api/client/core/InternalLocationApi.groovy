package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

@TestComponent
@InheritConstructors
class InternalLocationApi extends AuthenticatedApi {

    Response search(Map<String, Object> queryParams, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        queryParams.each { String key, Object value ->
            if (value instanceof Collection) {
                builder.addQueryParam(key, value as Collection)
            } else {
                builder.addQueryParam(key, value)
            }
        }
        RequestSpecification requestSpec = builder.build()
        return request(requestSpec, responseSpec, Method.GET, "/internalLocations/search")
    }
}
