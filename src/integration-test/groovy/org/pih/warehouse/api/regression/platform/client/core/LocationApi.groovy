package org.pih.warehouse.api.regression.platform.client.core

import org.pih.warehouse.api.client.core.*
import groovy.transform.InheritConstructors
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

@TestComponent
@InheritConstructors
class LocationApi extends AuthenticatedApi {

    Response list(Map<String, ?> queryParams, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParams(queryParams)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations")
    }

    Response read(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/{id}")
    }

    /** The CSV file to fill in for importCsv (a download, not JSON). */
    Response downloadTemplate(ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setAccept("text/csv")
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/template")
    }

    /** Uploads a locations CSV as the multipart field "importFile". */
    Response importCsv(String fileName, String mimeType, byte[] content, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setContentType("multipart/form-data")
                .addMultiPart(new MultiPartSpecBuilder(content)
                        .controlName("importFile")
                        .fileName(fileName)
                        .mimeType(mimeType)
                        .build())
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/locations/importCsv")
    }

    /** @param query useDefaultActivities, assignCurrentLocationGroup ("true"/"false") */
    Response create(String body, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().setBody(body)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.POST, "/locations")
    }

    /** @param method PUT or POST: both update */
    Response update(String id, String body, Map<String, ?> query, Method method, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam("id", id).setBody(body)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, method, "/locations/{id}")
    }

    Response delete(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .build()
        return request(requestSpec, responseSpec, Method.DELETE, "/locations/{id}")
    }

    /** @param activityCodes zero or more activity codes (repeated activityCode parameter) */
    Response locationTypes(List<String> activityCodes, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (activityCodes) {
            builder.addQueryParam("activityCode", activityCodes)
        }
        return request(builder.build(), responseSpec, Method.GET, "/locations/locationTypes")
    }

    Response supportedActivities(ResponseSpecification responseSpec) {
        return request(null, responseSpec, Method.GET, "/locations/supportedActivities")
    }

    /** Quantity on hand per product at the location (GET /api/locations/{id}/productSummary). */
    Response productSummary(String id, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/{id}/productSummary")
    }

    /** The Excel file to fill in for importBinLocations (a download, not JSON). */
    Response downloadBinLocationTemplate(ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setAccept("*/*")
                .build()
        return request(requestSpec, responseSpec, Method.GET, "/locations/binLocations/template")
    }

    /** Uploads a bin location spreadsheet as the multipart field "fileContents". */
    Response importBinLocations(String id, String fileName, byte[] content, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addPathParam("id", id)
                .setContentType("multipart/form-data")
                .addMultiPart(new MultiPartSpecBuilder(content)
                        .controlName("fileContents")
                        .fileName(fileName)
                        .mimeType("application/vnd.ms-excel")
                        .build())
                .build()
        return request(requestSpec, responseSpec, Method.POST, "/locations/{id}/binLocations/import")
    }
}
