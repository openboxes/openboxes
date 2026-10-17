package org.pih.warehouse.api.regression.platform.client.product

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/** Upstream's CategoryApi plus updateWithPost (a sibling; see RegressionProductApi). */
@TestComponent
@InheritConstructors
class RegressionCategoryApi extends AuthenticatedApi {

    Response list(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/categories")
    }

    Response get(String categoryId, ResponseSpecification responseSpec) {
        return request(byId(categoryId, null), responseSpec, Method.GET, "/categories/{categoryId}")
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, "/categories")
    }

    Response update(String categoryId, String body, ResponseSpecification responseSpec) {
        return request(byId(categoryId, body), responseSpec, Method.PUT, "/categories/{categoryId}")
    }

    Response delete(String categoryId, ResponseSpecification responseSpec) {
        return request(byId(categoryId, null), responseSpec, Method.DELETE, "/categories/{categoryId}")
    }

    /** The same update as {@link #update}, sent with POST for clients that cannot send PUT. */
    Response updateWithPost(String categoryId, String body, ResponseSpecification responseSpec) {
        return request(byId(categoryId, body), responseSpec, Method.POST, "/categories/{categoryId}")
    }

    private static RequestSpecification byId(String categoryId, String body) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam("categoryId", categoryId)
        if (body != null) {
            builder.setBody(body)
        }
        return builder.build()
    }
}
