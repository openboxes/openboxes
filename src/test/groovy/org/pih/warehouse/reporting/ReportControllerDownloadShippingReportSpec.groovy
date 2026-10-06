package org.pih.warehouse.reporting

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.shipping.Shipment

class ReportControllerDownloadShippingReportSpec extends Specification implements ControllerUnitTest<ReportController>, DataTest {

    void setupSpec() {
        mockDomains(Shipment)
    }

    void setup() {
        request.contextPath = "/openboxes"
    }

    @Unroll
    void "downloadShippingReport rejects sneaky params.url values: #scenario"() {
        given:
        params.format = "pdf"
        params.url = url
        params['shipment.id'] = "some-shipment"

        when:
        controller.downloadShippingReport()

        then:
        response.status == 400

        where:
        url                              | scenario
        "@evil.com/"                     | "host injection via @"
        "://evil.com/"                   | "protocol injection"
        "\\\\evil.com\\share"            | "UNC path"
        "/etc/passwd"                    | "path outside application context"
        "/admin/config"                  | "path on same host but outside context"
        "/openboxes/../admin/config"     | "dot-segment traversal out of context"
        "/openboxes/%2e%2e/admin/config" | "percent-encoded dot-segment traversal"
        "/openboxes/%2E%2E/admin/config" | "uppercase percent-encoded dot-segment traversal"
        ""                               | "empty string"
        null                             | "null"
    }

    @Unroll
    void "downloadShippingReport accepts params.url values that resolve within the context: #scenario"() {
        given:
        params.format = "pdf"
        params.url = url
        params['shipment.id'] = "some-shipment"
        controller.reportService = [generatePdf: { String u, OutputStream o -> }]

        when:
        controller.downloadShippingReport()

        then:
        response.status != 400

        where:
        url                                                  | scenario
        "/openboxes/report/showPaginatedPackingListReport"   | "straightforward path"
        "/openboxes/report/../report/show"                   | "relative segment that resolves inside context"
        "/openboxes/report/%2e%2e/report/show"               | "percent-encoded relative segment inside context"
    }
}
