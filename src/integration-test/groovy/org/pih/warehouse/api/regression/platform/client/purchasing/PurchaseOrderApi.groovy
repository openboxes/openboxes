package org.pih.warehouse.api.regression.platform.client.purchasing

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The purchase order API (/api/purchaseOrders...): one method per operation.
 */
@TestComponent
@InheritConstructors
class PurchaseOrderApi extends AuthenticatedApi {

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        if (query?.format == 'csv') {
            builder.setAccept('text/csv')
        }
        return request(builder.build(), responseSpec, Method.GET, '/purchaseOrders')
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/purchaseOrders/{id}')
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.DELETE, '/purchaseOrders/{id}')
    }

    Response rollback(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.POST, '/purchaseOrders/{id}/rollback')
    }

    Response statusOptions(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/orderSummaryStatus')
    }

    private static RequestSpecification idSpec(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }
}
