package org.pih.warehouse.api.client.core

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

@TestComponent
@InheritConstructors
class LocalizationApi extends AuthenticatedApi {

    Response list(String languageCode, String prefix, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("languageCode", languageCode)
                .addQueryParam("prefix", prefix)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/localizations")
    }

    Response get(String messageCode, String languageCode, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", messageCode)
                .addQueryParam("lang", languageCode)
                .build()

        return request(requestSpec, responseSpec, Method.GET, "/localizations/{id}")
    }
}
