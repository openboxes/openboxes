package org.pih.warehouse.api.regression.platform.client.product

import org.pih.warehouse.api.client.product.*
import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The product source API (/api/productSuppliers...): one method per operation. A product source is a product as a
 * given supplier sells it.
 */
@TestComponent
@InheritConstructors
class ProductSupplierApi extends AuthenticatedApi {

    Response list(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/productSuppliers')
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/productSuppliers/{id}')
    }

    Response create(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST, '/productSuppliers')
    }

    /** PUT, or POST for clients that cannot send PUT. */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).setBody(body).build()
        return request(requestSpec, responseSpec, method, '/productSuppliers/{id}')
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.DELETE, '/productSuppliers/{id}')
    }

    /** The export: JSON rows by default, a CSV or an Excel file with format=csv or format=xls. */
    Response export(Map<String, ?> query, String accept, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().setAccept(accept)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/productSuppliers/export')
    }

    private static RequestSpecification idSpec(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }
}
