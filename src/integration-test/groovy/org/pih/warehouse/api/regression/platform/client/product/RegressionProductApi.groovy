package org.pih.warehouse.api.regression.platform.client.product

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * Upstream's ProductApi (as on OBAF) plus 13 more operations. A sibling, not a
 * subclass (autowiring ProductApi by type must stay unambiguous for upstream's ProductApiWrapper).
 *
 * Port: `ProductApi productApi` -> `RegressionProductApi productApi` (field name may stay). availabilityInAllDepots
 * targets a route OBAF does not have (POST /api/products/inventoryItems/getAvailabilityInAllDepots): specs calling
 * it cannot pass on OBAF.
 */
@TestComponent
@InheritConstructors
class RegressionProductApi extends AuthenticatedApi {

    // ---- upstream ProductApi (OBAF 5e9711e84) ----

    Response list(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/products")
    }

    Response save(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().setBody(body).build()
        return request(requestSpec, responseSpec, Method.POST, "/products")
    }

    Response getDemand(String productId, ResponseSpecification responseSpec) {
        return productRead(productId, 'demand', responseSpec)
    }

    Response getDemandSummary(String productId, ResponseSpecification responseSpec) {
        return productRead(productId, 'demandSummary', responseSpec)
    }

    Response getProductSummary(String productId, ResponseSpecification responseSpec) {
        return productRead(productId, 'productSummary', responseSpec)
    }

    Response getProductAvailability(String productId, ResponseSpecification responseSpec) {
        return productRead(productId, 'productAvailability', responseSpec)
    }

    private Response productRead(String productId, String action, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam("productId", productId).build()
        return request(requestSpec, responseSpec, Method.GET, "/products/{productId}/${action}")
    }

    // ---- operations beyond upstream's ProductApi ----

    Response list(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addQueryParams(queryParams).build()
        return request(requestSpec, responseSpec, Method.GET, "/products")
    }

    /** The product list as a CSV download ("format=csv"). */
    Response listCsv(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setAccept("text/csv")
                .addQueryParams(queryParams + [format: "csv"])
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/products")
    }

    Response read(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam("id", id).build()
        return request(requestSpec, responseSpec, Method.GET, "/products/{id}")
    }

    /** Updates with PUT, or with POST (the same operation, for clients that cannot send PUT). */
    Response update(String id, String body, Method method, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam("id", id).setBody(body).build()
        return request(requestSpec, responseSpec, method, "/products/{id}")
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addPathParam("id", id).build()
        return request(requestSpec, responseSpec, Method.DELETE, "/products/{id}")
    }

    Response search(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().addQueryParams(queryParams).build()
        return request(requestSpec, responseSpec, Method.GET, "/products/search")
    }

    /** One of the per-product reads under /api/products/{id}/{action} (substitutions, associatedProducts, ...). */
    Response productAction(String productId, String action, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam("productId", productId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, "/products/{productId}/${action}")
    }

    /** The available stock of the products (query product.id or id, repeated) at the location (location.id). */
    Response availableItems(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, "/products/availableItems")
    }

    Response getInventoryItem(String productId, String lotNumber, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("productId", productId)
                .addPathParam("lotNumber", lotNumber)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/products/{productId}/inventoryItems/{lotNumber}")
    }

    Response latestInventoryCountDate(List<String> productIds, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (productIds) {
            builder.addQueryParam("productIds", productIds)
        }
        return request(builder.build(), responseSpec, Method.GET, "/products/getLatestInventoryCountDate")
    }

    Response lotNumbersWithExpirationDate(List<String> productIds, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (productIds) {
            builder.addQueryParam("productIds", productIds)
        }
        return request(builder.build(), responseSpec, Method.GET, "/products/inventoryItems/lotNumbersWithExpirationDate")
    }

    /** No OBAF route: a spec calling it cannot pass on OBAF. */
    Response availabilityInAllDepots(String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().setBody(body).build()
        return request(requestSpec, responseSpec, Method.POST, "/products/inventoryItems/getAvailabilityInAllDepots")
    }

    /** Imports products from a CSV sent as the request body (Content-Type text/csv). */
    Response importCsv(String csv, String contentType, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder().setContentType(contentType).setBody(csv)
                // OBAF: ProductApiController.importCsv compares the content type exactly ("text/csv"); RestAssured
                // appends "; charset=ISO-8859-1" unless told not to.
                .setConfig(RestAssuredConfig.config().encoderConfig(EncoderConfig.encoderConfig()
                        .appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/products/import")
    }
}
