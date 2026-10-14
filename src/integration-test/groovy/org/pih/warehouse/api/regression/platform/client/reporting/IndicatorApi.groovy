package org.pih.warehouse.api.regression.platform.client.reporting

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The cycle count report's indicator tiles (/api/reports/indicators/...): one method per operation.
 */
@TestComponent
@InheritConstructors
class IndicatorApi extends AuthenticatedApi {

    /** @param query facility (location id), startDate, endDate - the report page sends MM/dd/yyyy dates */
    Response productsInventoried(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/reports/indicators/productsInventoried')
    }

    Response inventoryAccuracy(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/reports/indicators/inventoryAccuracy')
    }

    Response inventoryShrinkage(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/reports/indicators/inventoryShrinkage')
    }

    private static RequestSpecification withQuery(Map<String, ?> query) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return builder.build()
    }
}
