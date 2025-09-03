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
class ProductApi extends AuthenticatedApi {

    Response list(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/products")
    }

    Response save(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.POST, "/products")
    }

    Response getDemand(String productId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("productId", productId)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/products/{productId}/demand")
    }

    Response getDemandSummary(String productId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("productId", productId)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/products/{productId}/demandSummary")
    }

    Response getProductSummary(String productId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("productId", productId)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/products/{productId}/productSummary")
    }

    Response getProductAvailability(String productId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("productId", productId)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/products/{productId}/productAvailability")
    }
}
