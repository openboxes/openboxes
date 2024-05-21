package org.pih.warehouse.component.api.product

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.pih.warehouse.component.api.base.Api

@InheritConstructors
class CategoryApi extends Api {

    Response list(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/categories")
    }

    Response get(String categoryId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("categoryId", categoryId)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/categories/{categoryId}")
    }

    Response create(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.POST, "/categories")
    }

    Response update(String categoryId, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("categoryId", categoryId)
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.PUT, "/categories/{categoryId}")
    }

    Response delete(String categoryId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("categoryId", categoryId)
                .build()

        return request(requestSpec, responseSpec, Method.DELETE, "/categories/{categoryId}")
    }
}
