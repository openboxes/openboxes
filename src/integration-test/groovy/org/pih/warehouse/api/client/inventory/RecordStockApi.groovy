package org.pih.warehouse.api.client.inventory

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
class RecordStockApi extends AuthenticatedApi {

    Response saveRecordStock(String facilityId, String body, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("facilityId", facilityId)
                .setBody(body)
                .build()

        return request(requestSpec, responseSpec, Method.POST, "/facilities/{facilityId}/inventory/record-stock/save")
    }
}
