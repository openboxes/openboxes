package org.pih.warehouse.api.client.product

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
class ProductSupplierPreferenceApi extends AuthenticatedApi {

    Response create(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/productSupplierPreferences")
    }

    Response createOrUpdateBatch(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/productSupplierPreferences/batch")
    }

    Response update(String id, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.PUT, "/productSupplierPreferences/{id}")
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .build()
        return request(requestSpec, responseSpec, Method.DELETE, "/productSupplierPreferences/{id}")
    }
}
