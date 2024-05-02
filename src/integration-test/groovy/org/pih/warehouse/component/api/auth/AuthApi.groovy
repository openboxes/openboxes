package org.pih.warehouse.component.api.auth

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

import org.pih.warehouse.component.api.base.Api

@InheritConstructors
class AuthApi extends Api {

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
