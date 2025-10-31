package org.pih.warehouse.api.client.auth

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
class AuthApi extends UnauthenticatedApi {

    /**
     * Authenticates into the app with the provided user.
     */
    Response login(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.POST, "/login")
    }
}
