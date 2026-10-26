package org.pih.warehouse.api.regression.platform.client.transfers

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The stock transfer API (/api/stockTransfers..., /api/stockTransferItems/{id}/): one method per operation. A stock
 * transfer is a transfer order between bins of a facility, or a return order (outbound to another location, or
 * inbound from one).
 */
@TestComponent
@InheritConstructors
class StockTransferApi extends AuthenticatedApi {

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/stockTransfers')
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/stockTransfers')
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.GET, '/stockTransfers/{id}')
    }

    Response update(String id, Method method, String body, ResponseSpecification responseSpec) {
        return request(withId(id, body), responseSpec, method, '/stockTransfers/{id}')
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.DELETE, '/stockTransfers/{id}')
    }

    Response statusOptions(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/stockTransfers/statusOptions')
    }

    /** What can be transferred at a location (the current one unless location.id is given). */
    Response candidates(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/stockTransfers/candidates')
    }

    /** What can be returned from a location, filtered by product, bin, lot or expiration date. */
    Response returnCandidates(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST,
                '/stockTransfers/candidates')
    }

    Response removeItem(String orderItemId, ResponseSpecification responseSpec) {
        return request(withId(orderItemId), responseSpec, Method.DELETE, '/stockTransferItems/{id}/')
    }

    Response removeAllItems(String id, ResponseSpecification responseSpec) {
        return request(withId(id), responseSpec, Method.DELETE, '/stockTransfers/{id}/removeAllItems')
    }

    Response sendShipment(String id, ResponseSpecification responseSpec) {
        return request(withId(id, '{}'), responseSpec, Method.POST, '/stockTransfers/{id}/sendShipment')
    }

    Response rollback(String id, ResponseSpecification responseSpec) {
        return request(withId(id, '{}'), responseSpec, Method.POST, '/stockTransfers/{id}/rollback')
    }

    private static RequestSpecification withId(String id, String body = null) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('id', id)
        if (body != null) {
            builder.setBody(body)
        }
        return builder.build()
    }
}
