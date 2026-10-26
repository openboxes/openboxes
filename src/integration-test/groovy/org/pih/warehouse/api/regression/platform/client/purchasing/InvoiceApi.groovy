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
 * The invoice API (/api/invoices..., /api/invoiceItems/{id}/validation, /api/invoiceStatuses, /api/invoiceTypeCodes):
 * one method per operation.
 */
@TestComponent
@InheritConstructors
class InvoiceApi extends AuthenticatedApi {

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        if (query?.format == 'csv') {
            builder.setAccept('text/csv')
        }
        return request(builder.build(), responseSpec, Method.GET, '/invoices')
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/invoices')
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/invoices/{id}')
    }

    /** Updates with PUT, or with POST (the web app's way). */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        return request(idSpec(id, body), responseSpec, method, '/invoices/{id}')
    }

    Response items(String id, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('id', id)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/invoices/{id}/items')
    }

    Response updateItems(String id, String body, ResponseSpecification responseSpec) {
        return request(idSpec(id, body), responseSpec, Method.POST, '/invoices/{id}/items')
    }

    Response itemCandidates(String id, String body, ResponseSpecification responseSpec) {
        return request(idSpec(id, body), responseSpec, Method.POST, '/invoices/{id}/invoiceItemCandidates')
    }

    Response orderNumbers(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/invoices/{id}/orders')
    }

    Response shipmentNumbers(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/invoices/{id}/shipments')
    }

    /** Removes an invoice item: the id in the path is the invoice item's. */
    Response removeItem(String invoiceItemId, ResponseSpecification responseSpec) {
        return request(idSpec(invoiceItemId), responseSpec, Method.DELETE, '/invoices/{id}/removeItem')
    }

    Response submit(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.POST, '/invoices/{id}/submit')
    }

    Response post(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.POST, '/invoices/{id}/post')
    }

    Response prepaymentItems(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/invoices/{id}/prepaymentItems')
    }

    Response validateItem(String invoiceItemId, String body, ResponseSpecification responseSpec) {
        return request(idSpec(invoiceItemId, body), responseSpec, Method.POST, '/invoiceItems/{id}/validation')
    }

    Response statuses(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/invoiceStatuses')
    }

    Response typeCodes(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/invoiceTypeCodes')
    }

    private static RequestSpecification idSpec(String id, String body = null) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('id', id)
        if (body != null) {
            builder.setBody(body)
        }
        return builder.build()
    }
}
