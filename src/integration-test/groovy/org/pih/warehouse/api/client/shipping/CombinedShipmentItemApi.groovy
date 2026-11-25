package org.pih.warehouse.api.client.shipping

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

@TestComponent
@InheritConstructors
class CombinedShipmentItemApi extends AuthenticatedApi {

    Response addToShipment(String shipmentId, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", shipmentId)
                .setBody(body)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/combinedShipmentItems/addToShipment/{id}")
    }

    Response importTemplate(String shipmentId, String fileName, String content, String mimeType,
                            ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", shipmentId)
                .setContentType("multipart/form-data")
                .addMultiPart("importFile", fileName, content.bytes, mimeType)
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/combinedShipmentItems/importTemplate/{id}")
    }
}
