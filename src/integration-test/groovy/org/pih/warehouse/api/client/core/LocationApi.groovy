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
class LocationApi extends AuthenticatedApi {

    Response getAvailableItems(String locationId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", locationId)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/{id}/availableItems")
    }

    Response getAvailableItems(String locationId, Integer max, Integer offset, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .addPathParam("id", locationId)
        if (max != null) {
            builder.addQueryParam("max", max)
        }
        if (offset != null) {
            builder.addQueryParam("offset", offset)
        }
        return request(builder.build(), responseSpec, Method.GET, "/locations/{id}/availableItems")
    }

    Response exportAvailableItems(String locationId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", locationId)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/{id}/availableItems/export")
    }
}
