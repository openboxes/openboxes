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
class UnitOfMeasureApi extends AuthenticatedApi {

    Response currencies(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/unitOfMeasure/currencies")
    }

    Response uomOptions(String type, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("type", type)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/unitOfMeasures/options")
    }
}
