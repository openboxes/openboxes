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
 * The stock list item API (/api/stocklistItems...): the lines of stock lists, by product.
 */
@TestComponent
@InheritConstructors
class StocklistItemApi extends AuthenticatedApi {

    /** The stock list lines of a product; productId null leaves the parameter out. */
    Response list(String productId, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (productId != null) {
            builder.addQueryParam('product.id', productId)
        }
        return request(builder.build(), responseSpec, Method.GET, '/stocklistItems')
    }

    Response read(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.GET, '/stocklistItems/{id}')
    }

    /** Adds a line for the product (query parameter product.id) to the stock list named in the body. */
    Response create(String productId, String body, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().setBody(body)
        if (productId != null) {
            builder.addQueryParam('product.id', productId)
        }
        return request(builder.build(), responseSpec, Method.POST, '/stocklistItems')
    }

    /** PUT, or POST (what the stock list management page sends). */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam('id', id).setBody(body).build()
        return request(requestSpec, responseSpec, method, '/stocklistItems/{id}')
    }

    Response remove(String id, ResponseSpecification responseSpec) {
        return request(idSpec(id), responseSpec, Method.DELETE, '/stocklistItems/{id}')
    }

    Response availableStocklists(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, '/stocklistItems/availableStocklists')
    }

    private static RequestSpecification idSpec(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }
}
