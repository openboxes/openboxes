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
 * The product source settings the product source form saves after the details: preferences
 * (/api/productSupplierPreferences...), attributes (/api/productSupplierAttributes/batch) and the package and prices
 * (/api/productPackages).
 */
@TestComponent
@InheritConstructors
class ProductSupplierPreferenceApi extends AuthenticatedApi {

    Response create(String body, ResponseSpecification responseSpec) {
        return request(bodySpec(body), responseSpec, Method.POST, '/productSupplierPreferences')
    }

    /** PUT, or POST for clients that cannot send PUT. */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).setBody(body).build()
        return request(requestSpec, responseSpec, method, '/productSupplierPreferences/{id}')
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).build()
        return request(requestSpec, responseSpec, Method.DELETE, '/productSupplierPreferences/{id}')
    }

    Response saveBatch(String body, ResponseSpecification responseSpec) {
        return request(bodySpec(body), responseSpec, Method.POST, '/productSupplierPreferences/batch')
    }

    Response updateAttributes(String body, ResponseSpecification responseSpec) {
        return request(bodySpec(body), responseSpec, Method.POST, '/productSupplierAttributes/batch')
    }

    Response savePackage(String body, ResponseSpecification responseSpec) {
        return request(bodySpec(body), responseSpec, Method.POST, '/productPackages')
    }

    private static RequestSpecification bodySpec(String body) {
        return new RequestSpecBuilder().setBody(body).build()
    }
}
