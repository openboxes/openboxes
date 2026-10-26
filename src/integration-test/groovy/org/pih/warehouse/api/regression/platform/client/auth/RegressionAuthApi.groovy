package org.pih.warehouse.api.regression.platform.client.auth

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.UnauthenticatedApi

/**
 * Sibling of upstream's AuthApi with more session operations (status, logout, chooseLocation, chooseLocale) plus
 * login.
 *
 * Port: `AuthApi authApi` -> `RegressionAuthApi regressionAuthApi` in specs that call the added methods. A sibling rather than a
 * subclass, so that autowiring AuthApi by type stays unambiguous for upstream's own wrappers.
 */
@TestComponent
@InheritConstructors
class RegressionAuthApi extends UnauthenticatedApi {

    Response login(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/login")
    }

    /** Whether the application and its database are up (no login needed). */
    Response status(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/status")
    }

    /** Ends the session the request authenticates with. */
    Response logout(RequestSpecification authentication, ResponseSpecification responseSpec) {
        return request(authentication, responseSpec, Method.POST, "/logout")
    }

    /** Makes the location the current one. */
    Response chooseLocation(RequestSpecification authentication, String locationId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addRequestSpecification(authentication)
                .addPathParam("id", locationId)
                .build()
        return request(requestSpec, responseSpec, Method.PUT, "/chooseLocation/{id}")
    }

    /** Makes the locale the current one. */
    Response chooseLocale(RequestSpecification authentication, String localeCode, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addRequestSpecification(authentication)
                .addPathParam("id", localeCode)
                .build()
        return request(requestSpec, responseSpec, Method.PUT, "/chooseLocale/{id}")
    }
}
