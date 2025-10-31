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
class ProductClassificationApi extends AuthenticatedApi {

    Response list(String facilityId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("facilityId", facilityId)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/facilities/{facilityId}/products/classifications")
    }
}
