package org.pih.warehouse.api.client.inventory

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

@TestComponent
@InheritConstructors
class ProductAvailabilityApi extends AuthenticatedApi {

    Response list(String facilityId, ResponseSpecification responseSpec) {
        return list(facilityId, null, null, responseSpec)
    }

    Response list(String facilityId, Integer max, Integer offset, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .addPathParam("facilityId", facilityId)
        if (max != null) {
            builder.addQueryParam("max", max)
        }
        if (offset != null) {
            builder.addQueryParam("offset", offset)
        }
        return request(builder.build(), responseSpec, Method.GET, "/facilities/{facilityId}/availableItems")
    }
}
