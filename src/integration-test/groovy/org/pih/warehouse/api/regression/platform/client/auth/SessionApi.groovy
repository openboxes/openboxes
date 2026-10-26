package org.pih.warehouse.api.regression.platform.client.auth

import org.pih.warehouse.api.client.auth.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The logged-in user's session: current location and language, the app context and menu, and logout.
 */
@TestComponent
@InheritConstructors
class SessionApi extends AuthenticatedApi {

    Response chooseLocation(String locationId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", locationId)
                .build()
        return request(requestSpec, responseSpec, Method.PUT, "/chooseLocation/{id}")
    }

    Response chooseLocale(String localeCode, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", localeCode)
                .build()
        return request(requestSpec, responseSpec, Method.PUT, "/chooseLocale/{id}")
    }

    Response getAppContext(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/getAppContext")
    }

    Response getMenuConfig(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/getMenuConfig")
    }

    Response logout(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.POST, "/logout")
    }
}
