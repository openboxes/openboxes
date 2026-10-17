package org.pih.warehouse.api.regression.platform.client.stocklist

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The stock list API (/api/stocklists...): one method per operation. A stock list is a requisition template.
 */
@TestComponent
@InheritConstructors
class StocklistApi extends AuthenticatedApi {

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        if (query?.format == 'csv') {
            builder.setAccept('text/csv')
        }
        return request(builder.build(), responseSpec, Method.GET, '/stocklists')
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/stocklists/{id}')
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/stocklists')
    }

    /** PUT, or POST for clients that cannot send PUT. */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).setBody(body).build()
        return request(requestSpec, responseSpec, method, '/stocklists/{id}')
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.DELETE, '/stocklists/{id}')
    }

    Response sendMail(String id, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).setBody(body).build()
        return request(requestSpec, responseSpec, Method.POST, '/stocklists/sendMail/{id}')
    }

    /** clear, clone, publish or unpublish. */
    Response action(String id, String action, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.POST, "/stocklists/{id}/${action}")
    }

    Response export(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).setAccept('text/csv').build()
        return request(requestSpec, responseSpec, Method.GET, '/stocklists/{id}/export')
    }

    private static RequestSpecification idSpec(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }
}
