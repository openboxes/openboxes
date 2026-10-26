package org.pih.warehouse.api.regression.platform.client.receiving

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The putaway API (/api/putaways..., /api/putawayItems/{id}): one method per operation. A putaway is an order;
 * a putaway item is one of its order items.
 */
@TestComponent
@InheritConstructors
class PutawayApi extends AuthenticatedApi {

    Response candidates(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/putaways')
    }

    Response save(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/putaways')
    }

    Response read(String orderId, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('id', orderId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/putaways/{id}')
    }

    Response removeItem(String orderItemId, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().addPathParam('id', orderItemId).build(), responseSpec, Method.DELETE,
                '/putawayItems/{id}')
    }
}
