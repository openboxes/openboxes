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
class UserRoleApi extends AuthenticatedApi {

    Response getUserRoles(String userId, ResponseSpecification responseSpec) {
        return getUserRoles(userId, null, responseSpec)
    }

    Response getUserRoles(String userId, String facilityId, ResponseSpecification responseSpec) {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder()
                .addPathParam("userId", userId)
        if (facilityId) {
            requestSpecBuilder.addQueryParam("facilityId", facilityId)
        }
        RequestSpecification requestSpec = requestSpecBuilder.build()
        return request(requestSpec, responseSpec, Method.GET, "/users/{userId}/roles")
    }
}
