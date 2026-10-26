package org.pih.warehouse.api.regression.platform.client.product

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/** Upstream's ProductClassificationApi plus listAll (a sibling; see RegressionProductApi). */
@TestComponent
@InheritConstructors
class RegressionProductClassificationApi extends AuthenticatedApi {

    Response list(String facilityId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam("facilityId", facilityId).build()
        return request(requestSpec, responseSpec, Method.GET, "/facilities/{facilityId}/products/classifications")
    }

    /** The same list with the facility as a query parameter (facilityId). */
    Response listAll(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, "/productClassifications")
    }
}
