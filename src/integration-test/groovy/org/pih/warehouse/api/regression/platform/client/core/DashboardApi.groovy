package org.pih.warehouse.api.regression.platform.client.core

import org.pih.warehouse.api.client.core.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The dashboard: its configuration and the indicators its widgets show (each widget names its endpoint in the
 * dashboard configuration, e.g. "/api/dashboard/inventoryValue").
 */
@TestComponent
@InheritConstructors
class DashboardApi extends AuthenticatedApi {

    /** @param name the indicator, e.g. "inventoryValue" (GET /api/dashboard/{name}) */
    Response indicator(String name, Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParams(queryParams)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/dashboard/${name}")
    }

    Response config(String dashboardId, ResponseSpecification responseSpec) {
        return request(idSpec(dashboardId), responseSpec, Method.GET, "/dashboard/{id}/config")
    }

    Response subdashboardKeys(String dashboardId, ResponseSpecification responseSpec) {
        return request(idSpec(dashboardId), responseSpec, Method.GET, "/dashboard/{id}/subdashboardKeys")
    }

    Response updateConfig(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/dashboard/config")
    }

    private static RequestSpecification idSpec(String id) {
        return new RequestSpecBuilder().addPathParam("id", id).build()
    }
}
