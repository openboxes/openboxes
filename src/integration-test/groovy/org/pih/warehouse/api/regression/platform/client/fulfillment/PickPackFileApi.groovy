package org.pih.warehouse.api.regression.platform.client.fulfillment

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The pick and pack spreadsheets of a stock movement (/api/stockMovements/...PickList.../...PackList...): the
 * templates and exports as CSV or Excel, and the CSV imports (a multipart upload of the file as importFile).
 */
@TestComponent
@InheritConstructors
class PickPackFileApi extends AuthenticatedApi {

    private Response download(String path, String id, String format, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('id', id)
        if (format != null) {
            builder.addQueryParam('format', format)
        }
        return request(builder.build(), responseSpec, Method.GET, path)
    }

    /** Uploads the CSV as the importFile part (or under another part name, to leave importFile out). */
    private Response upload(String path, String id, String csv, ResponseSpecification responseSpec,
                            String partName = 'importFile') {
        return request(new RequestSpecBuilder()
                .addPathParam('id', id)
                .setContentType('multipart/form-data')
                .addMultiPart(partName, 'import.csv', csv.getBytes('UTF-8'), 'text/csv')
                .build(), responseSpec, Method.POST, path)
    }

    Response exportPicklistItems(String id, String format, ResponseSpecification responseSpec) {
        return download('/stockMovements/exportPickListItems/{id}', id, format, responseSpec)
    }

    Response exportPicklistTemplate(String id, String format, ResponseSpecification responseSpec) {
        return download('/stockMovements/picklistTemplate/{id}', id, format, responseSpec)
    }

    Response importPicklistItems(String id, String csv, ResponseSpecification responseSpec,
                                 String partName = 'importFile') {
        return upload('/stockMovements/importPickListItems/{id}', id, csv, responseSpec, partName)
    }

    Response exportPackTemplate(String id, String format, ResponseSpecification responseSpec) {
        return download('/stockMovements/packlistTemplate/{id}', id, format, responseSpec)
    }

    Response importPackListItems(String id, String csv, ResponseSpecification responseSpec,
                                 String partName = 'importFile') {
        return upload('/stockMovements/importPackListItems/{id}', id, csv, responseSpec, partName)
    }
}
