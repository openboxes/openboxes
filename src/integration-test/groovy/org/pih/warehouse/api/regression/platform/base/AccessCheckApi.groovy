package org.pih.warehouse.api.regression.platform.base

import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification

import org.pih.warehouse.api.client.base.Api

/**
 * Calls any API operation by its documented path template, filling every path parameter with a placeholder.
 * For access checks: the security interceptors answer before the controller runs, so the placeholder never matters.
 * Redirects are not followed, so OBAF's interceptor redirects are visible.
 */
class AccessCheckApi extends Api {

    static final String PLACEHOLDER = 'access-check'

    private final RequestSpecification baseRequestSpec

    AccessCheckApi(RequestSpecification baseRequestSpec) {
        this.baseRequestSpec = baseRequestSpec
    }

    @Override
    RequestSpecification getBaseRequestSpec() {
        return baseRequestSpec
    }

    /** @param path a spec path such as "/api/products/{id}" */
    Response call(String method, String path, ResponseSpecification responseSpec) {
        String template = path.replaceFirst(/^\/api/, '')
        Map<String, String> params = (template =~ /\{([^}]+)\}/).collect { it[1] }.collectEntries { [(it): PLACEHOLDER] }
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParams(params).setUrlEncodingEnabled(true).build()
        requestSpec.redirects().follow(false)
        return request(requestSpec, responseSpec, Method.valueOf(method), template)
    }
}
