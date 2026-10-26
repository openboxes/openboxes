package org.pih.warehouse.api.regression.platform.client.receiving

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The receiving API (/api/receipts/v2/...): one method per operation.
 */
@TestComponent
@InheritConstructors
class ReceiptApi extends AuthenticatedApi {

    private Response call(Method method, String path, Map<String, String> pathParams, String body,
                          Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        pathParams.each { String name, String value -> builder.addPathParam(name, value) }
        if (body != null) {
            builder.setBody(body)
        }
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, method, path)
    }

    Response start(String shipmentId, ResponseSpecification responseSpec) {
        return call(Method.POST, '/receipts/v2/shipment/{shipmentId}/start', [shipmentId: shipmentId], null, null, responseSpec)
    }

    Response sync(String shipmentId, ResponseSpecification responseSpec) {
        return call(Method.POST, '/receipts/v2/shipment/{shipmentId}/sync', [shipmentId: shipmentId], null, null, responseSpec)
    }

    Response listShipmentReceipts(String shipmentId, ResponseSpecification responseSpec) {
        return call(Method.GET, '/receipts/v2/shipment/{shipmentId}', [shipmentId: shipmentId], null, null, responseSpec)
    }

    Response summary(String shipmentId, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/receipts/v2/shipment/{shipmentId}/summary', [shipmentId: shipmentId], null, query, responseSpec)
    }

    Response updateItemsBatch(String receiptId, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/receipts/v2/{receiptId}/items/batch', [receiptId: receiptId], body, null, responseSpec)
    }

    Response complete(String receiptId, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/receipts/v2/{receiptId}/complete', [receiptId: receiptId], body, null, responseSpec)
    }

    Response editReceivingInfo(String receiptId, String shipmentItemId, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/receipts/v2/{receiptId}/shipmentItems/{shipmentItemId}/receiptItems',
                [receiptId: receiptId, shipmentItemId: shipmentItemId], body, null, responseSpec)
    }

    Response createComment(String receiptItemId, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/receipts/v2/receiptItems/{receiptItemId}/comments', [receiptItemId: receiptItemId],
                body, null, responseSpec)
    }

    Response updateComment(String receiptItemId, String body, ResponseSpecification responseSpec) {
        return call(Method.PUT, '/receipts/v2/receiptItems/{receiptItemId}/comments', [receiptItemId: receiptItemId],
                body, null, responseSpec)
    }

    Response deleteComment(String receiptItemId, ResponseSpecification responseSpec) {
        return call(Method.DELETE, '/receipts/v2/receiptItems/{receiptItemId}/comments', [receiptItemId: receiptItemId],
                null, null, responseSpec)
    }
}
