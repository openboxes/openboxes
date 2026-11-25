package org.pih.warehouse.api.regression.platform.spec.inventory

import spock.lang.PendingFeature


import org.pih.warehouse.api.spec.inventory.*
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import spock.lang.Unroll

import org.pih.warehouse.product.Product

/**
 * The cycle count reports (details: one row per counted item; summary: one row per product) after a count completes,
 * read with GET and with POST, and the facility-independent list of cycle counts.
 */
// OpenAPI validation stays REPORT-ONLY here: the cycle count answers it builds its data with render the
// item status and facility as objects on OBAF (see CycleCountApiSpec)
class CycleCountReportApiSpec extends CycleCountSpec {

    /** Completes a count of a new product (counted 3 of 4 on hand, accepted without a recount). */
    Map<String, String> completedCount() {
        Product product = stockedProduct(4)
        Map count = startCount(requestCount(product))
        recordCount(count.cycleCountItems[0].id as String, 3)
        regressionCycleCountApi.submitCount(facility.id, count.id as String, submitBody(false), ok().build())
        return [product: product.id, count: count.id as String]
    }

    @Unroll
    void 'the #kind report with #method lists the completed count of the product'() {
        given:
        Map<String, String> completed = completedCount()

        when:
        Response report = regressionCycleCountApi.report(kind, method,
                [facility: facility.id, products: completed.product, max: 10, offset: 0], ok().build())

        then:
        report.asString().contains(completed.product)
        report.jsonPath().getInt('max') == 10

        where:
        kind      | method
        'details' | Method.GET
        'details' | Method.POST
        'summary' | Method.GET
        'summary' | Method.POST
    }

    @PendingFeature(reason = 'a range ending today misses today; not newest first')
    @Unroll
    void 'the #kind report with #method counts the whole end day and lists the newest count first'() {
        given: 'two counts of one product completed today'
        Map<String, String> first = completedCount()
        Product product = Product.withNewSession { Product.get(first.product) }
        // The database keeps whole seconds, and submitting a count writes its baseline transaction one second before
        // its adjustment. Wait long enough that the second count's baseline cannot share a second with the first
        // count's adjustment (the second submit would fail with "A transaction already exists at time ...") and
        // that the two counts are ordered.
        Thread.sleep(2500)
        Map count = startCount(requestCount(product))
        recordCount(count.cycleCountItems[0].id as String, 3)
        regressionCycleCountApi.submitCount(facility.id, count.id as String, submitBody(false), ok().build())
        String today = new Date().format('MM/dd/yyyy')
        String yesterday = (new Date() - 1).format('MM/dd/yyyy')

        when:
        Response endToday = regressionCycleCountApi.report(kind, method,
                [facility: facility.id, products: first.product, startDate: today, endDate: today], ok().build())
        Response endYesterday = regressionCycleCountApi.report(kind, method,
                [facility: facility.id, products: first.product, endDate: yesterday], ok().build())

        then: 'both counts are in up to the end of today, newest first; none ended yesterday'
        endToday.jsonPath().getList('data.cycleCount.id').unique() == [count.id as String, first.count]
        endYesterday.jsonPath().getList('data') == []

        where:
        kind      | method
        'details' | Method.GET
        'details' | Method.POST
        'summary' | Method.GET
        'summary' | Method.POST
    }

    void 'the list of all cycle counts finds a count by its id'() {
        given:
        Map<String, String> completed = completedCount()

        expect:
        regressionCycleCountApi.listAll([id: completed.count], ok().build()).jsonPath().getList('data.id') == [completed.count]
    }
}
