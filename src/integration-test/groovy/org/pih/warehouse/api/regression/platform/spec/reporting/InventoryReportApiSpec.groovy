package org.pih.warehouse.api.regression.platform.spec.reporting

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.builder.RegressionProductTestBuilder
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit

import org.pih.warehouse.api.regression.platform.client.reporting.InventoryReportApi
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product

/**
 * The cycle count report's tables as the report page asks for them: the current facility, the chosen products
 * (repeated parameter), an MM/dd/yyyy date range and paging; the export asks for the .csv variant.
 *
 * Every test works on a product of its own, counted at 10 three days ago and at 4 a few minutes ago (one
 * adjustment of -6 in today's range), so the rows are exact even though the main facility is shared.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class InventoryReportApiSpec extends RegressionApiSpec {

    @Autowired
    InventoryReportApi inventoryReportApi

    Product counted

    @Override
    void setupData() {
        counted = Product.withNewTransaction {
            new RegressionProductTestBuilder().name(randomUtil.randomStringFieldValue('Report product')).category(rootCategory)
                    .pricePerUnit(2.5).build(true)
        }
        stock(counted, 10, Instant.now().minus(3, ChronoUnit.DAYS))
        stock(counted, 4, Instant.now().minusSeconds(120))
    }

    // ---------------------------------------------------------------- inventory audit details

    @PendingFeature(reason = 'inventory audit details answer 500')
    void 'audit details list the adjustments of the chosen products in the range'() {
        expect:
        inventoryReportApi.auditDetails(query(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.size()', Matchers.equalTo(1))
                .expectBody('data[0].inventoryItem.product.id', Matchers.equalTo(counted.id))
                .expectBody('data[0].quantityAdjusted', Matchers.equalTo(-6))
                .expectBody('data[0].amountAdjusted', Matchers.equalTo(-15.0f))
                .expectBody('totalCount', Matchers.equalTo(1))
                .build())
    }

    @PendingFeature(reason = 'inventory audit details answer 500')
    void 'audit details without dates list every adjustment of the product'() {
        expect: 'the first count (+10 from nothing) and the second (-6)'
        inventoryReportApi.auditDetails([facility: facility.id, products: [counted.id], max: '10', offset: '0'],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.quantityAdjusted', Matchers.containsInAnyOrder(10, -6))
                        .build())
    }

    @PendingFeature(reason = 'inventory audit details answer 500')
    void 'audit details are paged in a stable order: newest first'() {
        given:
        Map<String, ?> all = [facility: facility.id, products: [counted.id]]

        when:
        Response first = inventoryReportApi.auditDetails(all + [max: '1', offset: '0'], responseSpecUtil.OK_RESPONSE_SPEC)
        Response second = inventoryReportApi.auditDetails(all + [max: '1', offset: '1'], responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        first.jsonPath().getInt('count') == 1
        first.jsonPath().getInt('totalCount') == 2
        first.jsonPath().getList('data.quantityAdjusted') == [-6]
        second.jsonPath().getList('data.quantityAdjusted') == [10]
    }

    @PendingFeature(reason = 'inventory audit details answer 500')
    void 'an adjustment is one row even when the product has several facility-level inventory levels'() {
        given: 'two inventory levels for the product at the facility, neither for a bin'
        InventoryLevel.withNewTransaction {
            ['B', 'C'].each { String abcClass ->
                new InventoryLevel(inventory: Location.get(facility.id).inventory, product: Product.load(counted.id),
                        abcClass: abcClass).save(validate: false, failOnError: true, flush: true)
            }
        }

        expect: 'one detail row, and the summary adds the adjustment once'
        inventoryReportApi.auditDetails(query(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(1))
                .expectBody('data.quantityAdjusted', Matchers.equalTo([-6]))
                .build())
        inventoryReportApi.auditSummary(query(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data[0].quantityAdjusted', Matchers.equalTo(-6))
                .expectBody('data[0].numberOfAdjustments', Matchers.equalTo(1))
                .build())
    }

    @PendingFeature(reason = 'inventory audit details answer 500')
    void 'audit details can be asked for with a JSON body'() {
        expect:
        inventoryReportApi.auditDetailsPost(JsonOutput.toJson(body()), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.inventoryItem.product.id', Matchers.everyItem(Matchers.equalTo(counted.id)))
                .expectBody('data.quantityAdjusted', Matchers.equalTo([-6]))
                .build())
    }

    // ---------------------------------------------------------------- inventory audit summary

    @PendingFeature(reason = 'the audit summary counts at the session location; amounts ignore the price')
    void 'the audit summary has one row per product with its adjustments, counts and value'() {
        expect:
        inventoryReportApi.auditSummary(query(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(1))
                .expectBody('data[0].product.id', Matchers.equalTo(counted.id))
                .expectBody('data[0].numberOfAdjustments', Matchers.equalTo(1))
                .expectBody('data[0].numberOfCounts', Matchers.equalTo(1))
                .expectBody('data[0].quantityAdjusted', Matchers.equalTo(-6))
                .expectBody('data[0].valueAdjusted', Matchers.equalTo(-15.0f))
                .expectBody('data[0].quantityOnHand', Matchers.equalTo(4))
                .expectBody('data[0].valueOnHand', Matchers.equalTo(10.0f))
                .build())
    }

    @PendingFeature(reason = 'the audit summary finds nothing without dates')
    void 'the audit summary without dates covers all time'() {
        expect: 'both counts and the net adjustment (+10 - 6)'
        inventoryReportApi.auditSummary([facility: facility.id, products: [counted.id]], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data[0].numberOfAdjustments', Matchers.equalTo(2))
                .expectBody('data[0].numberOfCounts', Matchers.equalTo(2))
                .expectBody('data[0].quantityAdjusted', Matchers.equalTo(4))
                .build())
    }

    @PendingFeature(reason = 'the audit summary counts at the session location')
    void 'the audit summary can be asked for with a JSON body'() {
        expect:
        inventoryReportApi.auditSummaryPost(JsonOutput.toJson(body()), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.product.id', Matchers.equalTo([counted.id]))
                .build())
    }

    @PendingFeature(reason = 'the audit summary counts at the session location')
    void 'the audit summary export is a CSV of the same rows'() {
        when:
        Response csv = inventoryReportApi.csvExport('inventory-audit-summary', query())
        List<String> lines = csv.body().asString().readLines()

        then:
        csv.statusCode() == HttpStatus.SC_OK
        csv.contentType().startsWith('text/csv')
        csv.header('Content-disposition') == 'attachment; filename="inventory-audit-summary.csv"'
        lines.size() == 2
        lines[0].contains('number of adjustments')
        lines[1].contains(counted.name)
    }

    // ---------------------------------------------------------------- inventory transactions summary

    @PendingFeature(reason = 'the transactions summary misses the counts of the range')
    void 'the transactions summary has the stock before and after each count in the range'() {
        expect:
        inventoryReportApi.transactionsSummary(query(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.equalTo(1))
                .expectBody('data[0].product.id', Matchers.equalTo(counted.id))
                .expectBody('data[0].quantityBefore', Matchers.equalTo(10))
                .expectBody('data[0].quantityAfter', Matchers.equalTo(4))
                .expectBody('data[0].quantityDifference', Matchers.equalTo(-6))
                .expectBody('data[0].varianceTypeCode', Matchers.equalTo('LESS'))
                .build())
    }

    @PendingFeature(reason = 'the transactions summary CSV is named .csv.csv')
    void 'the transactions summary export is a CSV with one file name'() {
        when:
        Response csv = inventoryReportApi.csvExport('inventory-transactions-summary', query())

        then:
        csv.statusCode() == HttpStatus.SC_OK
        csv.contentType().startsWith('text/csv')
        csv.header('Content-disposition') ==~ /attachment; filename="inventory-transaction-summary-.+\.csv"/
        !csv.header('Content-disposition').contains('.csv.csv')
        csv.body().asString().readLines().size() == 2
    }

    @PendingFeature(reason = 'without both dates the transactions summary answers an empty 200')
    void 'the transactions summary needs both ends of the range'() {
        expect:
        inventoryReportApi.transactionsSummary([facility: facility.id, products: [counted.id]],
                error(HttpStatus.SC_BAD_REQUEST))
        inventoryReportApi.transactionsSummary([facility: facility.id, startDate: query().startDate],
                error(HttpStatus.SC_BAD_REQUEST))
    }

    // ---------------------------------------------------------------- invalid parameters

    // ---------------------------------------------------------------- helpers

    /** An error response: the status and the common error body. */
    static ResponseSpecification error(int status) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.notNullValue())
                .build()
    }

    /**
     * The page's query for this test's product: yesterday to today (the page's default range ends today; the end day
     * counts in full) and the first page.
     */
    private Map<String, ?> query() {
        SimpleDateFormat format = new SimpleDateFormat('MM/dd/yyyy')
        return [
                facility : facility.id,
                products : [counted.id],
                startDate: format.format(Date.from(Instant.now().minus(1, ChronoUnit.DAYS))),
                endDate  : format.format(new Date()),
                max      : '10',
                offset   : '0',
        ]
    }

    /** The same as a JSON body; the products as references. */
    private Map<String, ?> body() {
        Map<String, ?> query = query()
        return query.findAll { it.key != 'products' } + [products: [[id: counted.id]]]
    }

    private void stock(Product product, int quantity, Instant when) {
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(product)
                .inventory(facility.inventory)
                .transactionDate(when)
                .row(null, null, null, quantity)
                .build())
    }
}
