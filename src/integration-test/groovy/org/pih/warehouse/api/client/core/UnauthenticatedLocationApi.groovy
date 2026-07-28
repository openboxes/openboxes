package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.UnauthenticatedApi

@TestComponent
@InheritConstructors
class UnauthenticatedLocationApi extends UnauthenticatedApi {

    Response getAvailableItems(String locationId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", locationId)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/{id}/availableItems")
    }
}
