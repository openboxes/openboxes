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
 * Combined shipments: an inbound shipment from a vendor filled with lines of several of its purchase orders
 * (/api/combineShipments/{id}, /api/combinedShipmentItems/...). One method per operation.
 */
@TestComponent
@InheritConstructors
class CombinedShipmentApi extends AuthenticatedApi {

    /** The purchase order the combined shipment page starts from. */
    Response order(String orderId, ResponseSpecification responseSpec) {
        return request(withId(orderId), responseSpec, Method.GET, '/combineShipments/{id}')
    }

    /** @param query vendor, destination (location ids), name (search terms) */
    Response productsInOrders(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/combinedShipmentItems/getProductsInOrders')
    }

    Response findOrderItems(String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().setBody(body).build(), responseSpec, Method.POST,
                '/combinedShipmentItems/findOrderItems')
    }

    Response addToShipment(String shipmentId, String body, ResponseSpecification responseSpec) {
        return request(new RequestSpecBuilder().addPathParam('id', shipmentId).setBody(body).build(), responseSpec,
                Method.POST, '/combinedShipmentItems/addToShipment/{id}')
    }

    /** @param query vendor and destination, or blank=true for an empty template */
    Response exportTemplate(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/combinedShipmentItems/exportTemplate')
    }

    /** Uploads the CSV as the importFile part (or under another part name / content type, to test refusals). */
    Response importTemplate(String shipmentId, String csv, ResponseSpecification responseSpec,
                            String partName = 'importFile', String contentType = 'text/csv') {
        return request(new RequestSpecBuilder()
                .addPathParam('id', shipmentId)
                .setContentType('multipart/form-data')
                .addMultiPart(partName, 'template.csv', csv.getBytes('UTF-8'), contentType)
                .build(), responseSpec, Method.POST, '/combinedShipmentItems/importTemplate/{id}')
    }

    private static RequestSpecification withId(String id) {
        return new RequestSpecBuilder().addPathParam('id', id).build()
    }

    private static RequestSpecification withQuery(Map<String, ?> query) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return builder.build()
    }

    /** The order picker of the combined shipment dialog: GET /api/orderNumberOptions?vendor=...&destination=... */
    Response orderNumberOptions(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/orderNumberOptions')
    }
}
