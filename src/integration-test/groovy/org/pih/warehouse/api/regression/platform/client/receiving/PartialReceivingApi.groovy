package org.pih.warehouse.api.regression.platform.client.receiving

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The partial receiving API (/api/partialReceiving...), the receiving workflow before receipts/v2: one method per
 * operation. The id of a partial receipt is the id of its shipment.
 */
@TestComponent
@InheritConstructors
class PartialReceivingApi extends AuthenticatedApi {

    private Response call(Method method, String path, String id, String body, Map<String, ?> query,
                          ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (id != null) {
            builder.addPathParam('id', id)
        }
        if (body != null) {
            builder.setBody(body)
        }
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, method, path)
    }

    Response list(ResponseSpecification responseSpec) {
        return call(Method.GET, '/partialReceiving', null, null, null, responseSpec)
    }

    Response read(String shipmentId, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.GET, '/partialReceiving/{id}', shipmentId, null, query, responseSpec)
    }

    Response update(String shipmentId, String body, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.POST, '/partialReceiving/{id}', shipmentId, body, query, responseSpec)
    }

    Response updateWithPut(String shipmentId, String body, Map<String, ?> query, ResponseSpecification responseSpec) {
        return call(Method.PUT, '/partialReceivings/{id}', shipmentId, body, query, responseSpec)
    }

    Response exportCsv(String shipmentId, String body, ResponseSpecification responseSpec) {
        return call(Method.POST, '/partialReceiving/exportCsv/{id}', shipmentId, body, null, responseSpec)
    }

    /** Uploads the CSV as the importFile part (or under another part name, to leave importFile out). */
    Response importCsv(String shipmentId, String csv, ResponseSpecification responseSpec, String partName = 'importFile') {
        return request(new RequestSpecBuilder()
                .addPathParam('id', shipmentId)
                .setContentType('multipart/form-data')
                .addMultiPart(partName, 'receipt.csv', csv.getBytes('UTF-8'), 'text/csv')
                .build(), responseSpec, Method.POST, '/partialReceiving/importCsv/{id}')
    }
}
