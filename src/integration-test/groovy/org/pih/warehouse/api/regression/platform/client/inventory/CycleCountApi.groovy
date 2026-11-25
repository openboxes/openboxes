package org.pih.warehouse.api.regression.platform.client.inventory

import org.pih.warehouse.api.client.inventory.*
import groovy.transform.InheritConstructors
import io.restassured.builder.MultiPartSpecBuilder
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

/**
 * The cycle count API (/api/facilities/{facility}/cycle-counts...): one method per operation.
 */
// Named explicitly: org.pih.warehouse.api.client has a client of the same class name, and both are
// component-scanned, so the default bean name would clash.
@TestComponent("regressionCycleCountApi")
@InheritConstructors
class CycleCountApi extends AuthenticatedApi {

    Response candidates(String facilityId, Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = facilityIdSpec(facilityId, query)
        if (query?.format == 'csv') {
            builder.setAccept('*/*')
        }
        return request(builder.build(), responseSpec, Method.GET, '/facilities/{facilityId}/cycle-counts/candidates')
    }

    Response pendingRequests(String facilityId, Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(facilityIdSpec(facilityId, query).build(), responseSpec, Method.GET,
                '/facilities/{facilityId}/cycle-counts/requests/pending')
    }

    Response createRequests(String facilityId, String body, ResponseSpecification responseSpec) {
        return request(facilityIdSpec(facilityId).setBody(body).build(), responseSpec, Method.POST,
                '/facilities/{facilityId}/cycle-counts/requests/batch')
    }

    Response updateRequests(String facilityId, String body, ResponseSpecification responseSpec) {
        return request(facilityIdSpec(facilityId).setBody(body).build(), responseSpec, Method.PATCH,
                '/facilities/{facilityId}/cycle-counts/requests/batch')
    }

    Response deleteRequests(String facilityId, List<String> ids, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = facilityIdSpec(facilityId)
        builder.addQueryParam('id', ids)
        return request(builder.build(), responseSpec, Method.DELETE,
                '/facilities/{facilityId}/cycle-counts/requests/batch')
    }

    /** @param format null for JSON, or "xls"/"pdf" for the count form download */
    Response start(String facilityId, String body, String format, ResponseSpecification responseSpec) {
        return request(facilitySpec(facilityId, format ? [format: format] : [:]).setBody(body).build(), responseSpec,
                Method.POST, '/facilities/{facility}/cycle-counts/start/batch')
    }

    Response startRecount(String facilityId, String body, String format, ResponseSpecification responseSpec) {
        return request(facilitySpec(facilityId, format ? [format: format] : [:]).setBody(body).build(), responseSpec,
                Method.POST, '/facilities/{facility}/cycle-counts/recount/start/batch')
    }

    Response list(String facilityId, Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(facilitySpec(facilityId, query).build(), responseSpec, Method.GET,
                '/facilities/{facility}/cycle-counts')
    }

    Response delete(String facilityId, String cycleCountId, ResponseSpecification responseSpec) {
        return request(cycleCountSpec(facilityId, cycleCountId).build(), responseSpec, Method.DELETE,
                '/facilities/{facility}/cycle-counts/{cycleCountId}')
    }

    Response submitCount(String facilityId, String cycleCountId, String body, ResponseSpecification responseSpec) {
        return request(cycleCountSpec(facilityId, cycleCountId).setBody(body).build(), responseSpec, Method.POST,
                '/facilities/{facility}/cycle-counts/{cycleCountId}/count')
    }

    Response submitRecount(String facilityId, String cycleCountId, String body, ResponseSpecification responseSpec) {
        return request(cycleCountSpec(facilityId, cycleCountId).setBody(body).build(), responseSpec, Method.POST,
                '/facilities/{facility}/cycle-counts/{cycleCountId}/recount')
    }

    Response refresh(String facilityId, String cycleCountId, Map<String, ?> query,
                     ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = cycleCountSpec(facilityId, cycleCountId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.POST,
                '/facilities/{facility}/cycle-counts/{cycleCountId}/refresh')
    }

    Response createItem(String facilityId, String cycleCountId, String body, ResponseSpecification responseSpec) {
        return request(cycleCountSpec(facilityId, cycleCountId).setBody(body).build(), responseSpec, Method.POST,
                '/facilities/{facility}/cycle-counts/{cycleCountId}/items')
    }

    /** Creates items with POST, or updates them with PATCH, through the batch path that names the count. */
    Response itemBatchOfCount(String facilityId, String cycleCountId, String body, Method method,
                              ResponseSpecification responseSpec) {
        return request(cycleCountSpec(facilityId, cycleCountId).setBody(body).build(), responseSpec, method,
                '/facilities/{facility}/cycle-counts/{cycleCountId}/items/batch')
    }

    /** Creates items with POST, or updates them with PATCH, through the batch path without the count. */
    Response itemBatch(String facilityId, String body, Method method, ResponseSpecification responseSpec) {
        return request(facilitySpec(facilityId).setBody(body).build(), responseSpec, method,
                '/facilities/{facility}/cycle-counts/items/batch')
    }

    Response updateItem(String facilityId, String itemId, String body, ResponseSpecification responseSpec) {
        return request(itemSpec(facilityId, itemId).setBody(body).build(), responseSpec, Method.PATCH,
                '/facilities/{facility}/cycle-counts/items/{cycleCountItemId}')
    }

    Response deleteItem(String facilityId, String itemId, ResponseSpecification responseSpec) {
        return request(itemSpec(facilityId, itemId).build(), responseSpec, Method.DELETE,
                '/facilities/{facility}/cycle-counts/items/{cycleCountItemId}')
    }

    /** Uploads a filled-in count (or recount) form as the multipart field "importFile". */
    Response upload(String facilityId, boolean recount, String fileName, byte[] content,
                    ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = facilitySpec(facilityId)
                .setContentType('multipart/form-data')
                .addMultiPart(new MultiPartSpecBuilder(content)
                        .controlName('importFile')
                        .fileName(fileName)
                        .mimeType('application/vnd.ms-excel')
                        .build())
                .build()
        return request(requestSpec, responseSpec, Method.POST,
                "/facilities/{facility}/cycle-counts/items/upload/${recount ? 'recount' : 'count'}")
    }

    /** All cycle counts, or those with the given ids (the facility-independent list). */
    Response listAll(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return request(builder.build(), responseSpec, Method.GET, '/cycleCounts')
    }

    /**
     * The cycle count report: "details" (one row per counted item) or "summary" (one row per product), filtered by
     * facility, products and date range, read with GET (query) or POST (form fields, as the report page sends them).
     */
    Response report(String kind, Method method, Map<String, ?> filters, ResponseSpecification responseSpec) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        if (method == Method.GET) {
            filters?.each { String name, value -> builder.addQueryParam(name, value) }
        } else {
            builder.setContentType('application/x-www-form-urlencoded')
            filters?.each { String name, value -> builder.addFormParam(name, value) }
        }
        return request(builder.build(), responseSpec, method, "/reports/cycle-count-${kind}")
    }

    private static RequestSpecBuilder facilityIdSpec(String facilityId, Map<String, ?> query = [:]) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('facilityId', facilityId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return builder
    }

    private static RequestSpecBuilder facilitySpec(String facilityId, Map<String, ?> query = [:]) {
        RequestSpecBuilder builder = new RequestSpecBuilder().addPathParam('facility', facilityId)
        query?.each { String name, value -> builder.addQueryParam(name, value) }
        return builder
    }

    private static RequestSpecBuilder cycleCountSpec(String facilityId, String cycleCountId) {
        return facilitySpec(facilityId).addPathParam('cycleCountId', cycleCountId)
    }

    private static RequestSpecBuilder itemSpec(String facilityId, String itemId) {
        return facilitySpec(facilityId).addPathParam('cycleCountItemId', itemId)
    }
}
