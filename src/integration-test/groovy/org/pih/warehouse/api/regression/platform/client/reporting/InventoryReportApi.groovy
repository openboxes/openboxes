package org.pih.warehouse.api.regression.platform.client.reporting

import groovy.transform.InheritConstructors
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.AuthenticatedApi

import static io.restassured.RestAssured.given

/**
 * The cycle count report's tables (/api/reports/inventory-audit-..., /api/reports/inventory-transactions-summary):
 * one method per operation. The report page sends facility, products (repeated), startDate and endDate (MM/dd/yyyy),
 * max and offset as query parameters; the export adds format=csv.
 */
@TestComponent
@InheritConstructors
class InventoryReportApi extends AuthenticatedApi {

    Response auditDetails(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/reports/inventory-audit-details')
    }

    Response auditDetailsPost(String body, ResponseSpecification responseSpec) {
        return request(withBody(body), responseSpec, Method.POST, '/reports/inventory-audit-details')
    }

    Response auditSummary(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/reports/inventory-audit-summary')
    }

    Response auditSummaryPost(String body, ResponseSpecification responseSpec) {
        return request(withBody(body), responseSpec, Method.POST, '/reports/inventory-audit-summary')
    }

    Response transactionsSummary(Map<String, ?> query, ResponseSpecification responseSpec) {
        return request(withQuery(query), responseSpec, Method.GET, '/reports/inventory-transactions-summary')
    }

    /** The expiration history report as its page asks for it (MM/DD/YYYY dates, paginationParams.*). */
    Response expirationHistory(Map<String, ?> query, ResponseSpecification responseSpec) {
        RequestSpecification requestSpec = withQuery(query)
        if (query?.format == 'csv') {
            requestSpec = new RequestSpecBuilder().addRequestSpecification(requestSpec).setAccept('*/*').build()
        }
        return request(requestSpec, responseSpec, Method.GET, '/inventories/expirationHistoryReport')
    }

    /**
     * The page's CSV export: the same operation with a .csv suffix on the path (GET /api/reports/...-summary.csv).
     * The spec validator does not model path suffixes, so this call bypasses the validating filters; the JSON calls
     * above are the validated ones.
     */
    Response csvExport(String report, Map<String, ?> query) {
        return given(baseRequestSpec).spec(withQuery(query)).when().get("/reports/${report}.csv".toString())
    }

    private static RequestSpecification withBody(String body) {
        return new RequestSpecBuilder().setBody(body).build()
    }

    /** A list value becomes a repeated parameter, as the page's query-string serializer sends it. */
    private static RequestSpecification withQuery(Map<String, ?> query) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
        query?.each { String name, value ->
            if (value instanceof Collection) {
                builder.addQueryParam(name, value as List)
            } else {
                builder.addQueryParam(name, value)
            }
        }
        return builder.build()
    }
}
