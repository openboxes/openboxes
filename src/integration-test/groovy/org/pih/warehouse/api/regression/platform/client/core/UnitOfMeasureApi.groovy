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

// Named explicitly: org.pih.warehouse.api.client has a client of the same class name, and both are
// component-scanned, so the default bean name would clash.
@TestComponent("regressionUnitOfMeasureApi")
@InheritConstructors
class UnitOfMeasureApi extends AuthenticatedApi {

    Response list(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/unitOfMeasures")
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, "/unitOfMeasures/{id}")
    }

    Response create(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/unitOfMeasures")
    }

    /** Updates with PUT, or with POST (the same operation, for clients that cannot send PUT). */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, method, "/unitOfMeasures/{id}")
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.DELETE, "/unitOfMeasures/{id}")
    }

    Response currencies(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/unitOfMeasure/currencies")
    }

    Response options(String type, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("type", type)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/unitOfMeasures/options")
    }

    private static RequestSpecification idSpec(String id) {
        return new RequestSpecBuilder().addPathParam("id", id).build()
    }
}
