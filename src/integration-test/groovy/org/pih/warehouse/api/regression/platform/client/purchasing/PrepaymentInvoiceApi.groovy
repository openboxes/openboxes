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
 * Editing the items of an invoice whose orders were prepaid (/api/prepaymentInvoices/{id}/invoiceItems,
 * /api/prepaymentInvoiceItems/{id}): one method per operation.
 */
@TestComponent
@InheritConstructors
class PrepaymentInvoiceApi extends AuthenticatedApi {

    Response updateItems(String invoiceId, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', invoiceId).setBody(body).build()
        return request(requestSpec, responseSpec, Method.POST, '/prepaymentInvoices/{id}/invoiceItems')
    }

    Response updateItem(String invoiceItemId, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', invoiceItemId).setBody(body).build()
        return request(requestSpec, responseSpec, Method.POST, '/prepaymentInvoiceItems/{id}')
    }

    Response deleteItem(String invoiceItemId, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', invoiceItemId).build()
        return request(requestSpec, responseSpec, Method.DELETE, '/prepaymentInvoiceItems/{id}')
    }
}
