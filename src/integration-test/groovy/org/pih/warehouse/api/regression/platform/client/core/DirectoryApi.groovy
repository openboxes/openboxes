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
 * People, users and organizations as the list pages' filters look them up, and the header search.
 */
@TestComponent
@InheritConstructors
class DirectoryApi extends AuthenticatedApi {

    Response persons(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        return request(query(queryParams), responseSpec, Method.GET, "/persons")
    }

    Response users(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        return request(query(queryParams), responseSpec, Method.GET, "/users")
    }

    Response organizations(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        return request(query(queryParams), responseSpec, Method.GET, "/organizations")
    }

    Response organization(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/organizations/{id}")
    }

    /** The header search (GET /api/globalSearch): products matching every term. */
    Response globalSearch(String term, ResponseSpecification responseSpec) {
        return request(query([term: term]), responseSpec, Method.GET, "/globalSearch")
    }

    private static RequestSpecification query(Map<String, ?> queryParams) {
        return new RequestSpecBuilder().addQueryParams(queryParams).build()
    }
}
