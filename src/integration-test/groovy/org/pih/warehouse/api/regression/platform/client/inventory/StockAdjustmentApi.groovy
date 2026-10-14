package org.pih.warehouse.api.regression.platform.client.inventory

import org.pih.warehouse.api.client.inventory.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * Stock adjustments as the stock movement wizard's "adjust inventory" dialog sends them: a JSON array of lines,
 * POST /api/stockAdjustments?location.id=...
 */
@TestComponent
@InheritConstructors
class StockAdjustmentApi extends AuthenticatedApi {

    /** @param locationId the location.id query parameter, or null to use the session's location */
    Response create(String locationId, String body, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().setBody(body)
        if (locationId != null) {
            builder.addQueryParam('location.id', locationId)
        }
        return request(builder.build(), responseSpec, Method.POST, '/stockAdjustments')
    }
}
