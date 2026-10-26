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
 * The option lists behind the product list page's filters: each answers {data: [{id, label, ...}]}.
 */
@TestComponent
@InheritConstructors
class SelectOptionsApi extends AuthenticatedApi {

    /** @param path one of "/catalogOptions", "/categoryOptions", "/glAccountOptions", ... */
    Response options(String path, Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParams(queryParams)
                .build()
        return request(requestSpec, responseSpec, Method.GET, path)
    }
}
