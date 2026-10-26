package org.pih.warehouse.api.regression.platform.client.fulfillment

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The fulfillment API (/api/fulfillments...): creating a shipped outbound from an imported packing list.
 */
@TestComponent
@InheritConstructors
class FulfillmentApi extends AuthenticatedApi {

    Response save(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/fulfillments')
    }

    Response validate(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/fulfillments/validate')
    }
}
