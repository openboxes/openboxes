package org.pih.warehouse.api.regression.platform.spec.reporting

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.builder.RegressionProductTestBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat
import java.time.Instant

import org.pih.warehouse.api.regression.platform.client.reporting.InventoryReportApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

/**
 * The expiration history report as its page asks for it: MM/DD/YYYY dates, paginationParams.offset/max and a search
 * term, for the current location. Each test writes off 5 of a lot of its own product (price 2.50) as expired a
 * minute ago, so a range ending today must include it.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ExpirationHistoryReportApiSpec extends RegressionApiSpec {

    @Autowired
    InventoryReportApi inventoryReportApi

    String productCode

    String transactionId

    private static String day(Date date) {
        return new SimpleDateFormat('MM/dd/yyyy').format(date)
    }

    private static Date daysAgo(int days) {
        return Date.from(Instant.now().minusSeconds(days * 86400L))
    }

    @Override
    void setupData() {
        Product expiring = Product.withNewTransaction {
            new RegressionProductTestBuilder().name(randomUtil.randomStringFieldValue('Expired product')).category(rootCategory)
                    .pricePerUnit(2.5).build(true)
        }
        productCode = expiring.productCode
        transactionId = Transaction.withNewTransaction {
            Location location = Location.get(facility.id)
            InventoryItem item = new InventoryItem(product: Product.get(expiring.id), lotNumber: 'EXP-1',
                    expirationDate: daysAgo(2)).save(failOnError: true)
            Transaction transaction = new Transaction(inventory: location.inventory,
                    transactionType: TransactionType.load(Constants.EXPIRATION_TRANSACTION_TYPE_ID),
                    transactionDate: Date.from(Instant.now().minusSeconds(60)))
            transaction.addToTransactionEntries(new TransactionEntry(inventoryItem: item, quantity: 5))
            transaction.save(failOnError: true, flush: true).id
        }
    }

    @PendingFeature(reason = 'expiration history: end day excluded, totals of the page only, CSV as text/html, missing/reversed dates not refused')
    void 'a write-off of today is in a range ending today, with its quantity and value'() {
        expect:
        inventoryReportApi.expirationHistory([startDate: day(daysAgo(7)), endDate: day(new Date()), searchTerm: productCode,
                                              'paginationParams.offset': '0', 'paginationParams.max': '10'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.transactionId', Matchers.equalTo([transactionId]))
                .expectBody('data[0].productCode', Matchers.equalTo(productCode))
                .expectBody('data[0].lotNumber', Matchers.equalTo('EXP-1'))
                .expectBody('data[0].quantityLostToExpiry', Matchers.equalTo(5))
                .expectBody('totalCount', Matchers.equalTo(1))
                .expectBody('totalQuantityLostToExpiry', Matchers.equalTo(5))
                .expectBody('totalValueLostToExpiry', Matchers.comparesEqualTo(12.5f))
                .build())
    }

    @PendingFeature(reason = 'expiration history: end day excluded, totals of the page only, CSV as text/html, missing/reversed dates not refused')
    void 'the totals cover every matching write-off, not only the page'() {
        given: 'a second write-off of 3 of the same product'
        Transaction.withNewTransaction {
            Location location = Location.get(facility.id)
            InventoryItem item = InventoryItem.findByProductAndLotNumber(Product.findByProductCode(productCode), 'EXP-1')
            Transaction transaction = new Transaction(inventory: location.inventory,
                    transactionType: TransactionType.load(Constants.EXPIRATION_TRANSACTION_TYPE_ID),
                    transactionDate: Date.from(Instant.now().minusSeconds(120)))
            transaction.addToTransactionEntries(new TransactionEntry(inventoryItem: item, quantity: 3))
            transaction.save(failOnError: true, flush: true)
        }

        expect: 'a page of one row, totals of both'
        inventoryReportApi.expirationHistory([startDate: day(daysAgo(7)), endDate: day(new Date()), searchTerm: productCode,
                                              'paginationParams.offset': '0', 'paginationParams.max': '1'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.hasSize(1))
                .expectBody('totalCount', Matchers.equalTo(2))
                .expectBody('totalQuantityLostToExpiry', Matchers.equalTo(8))
                .expectBody('totalValueLostToExpiry', Matchers.comparesEqualTo(20.0f))
                .build())
    }

    void 'a range that ends before the write-off does not have it'() {
        expect:
        inventoryReportApi.expirationHistory([startDate: day(daysAgo(7)), endDate: day(daysAgo(1)), searchTerm: productCode],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data', Matchers.empty())
                        .expectBody('totalQuantityLostToExpiry', Matchers.equalTo(0))
                        .build())
    }

    @PendingFeature(reason = 'expiration history: end day excluded, totals of the page only, CSV as text/html, missing/reversed dates not refused')
    void 'the CSV export has the write-off'() {
        when:
        Response response = inventoryReportApi.expirationHistory([format: 'csv', startDate: day(daysAgo(7)),
                                                                  endDate: day(new Date()), searchTerm: productCode],
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        response.contentType.startsWith('text/csv')
        response.asString().readLines().find { it.contains(productCode) }?.contains('EXP-1')
    }

    @PendingFeature(reason = 'expiration history: end day excluded, totals of the page only, CSV as text/html, missing/reversed dates not refused')
    void 'missing dates or a range that ends before it starts: 400'() {
        expect:
        inventoryReportApi.expirationHistory(query, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())

        where:
        query << [
                [:],
                [startDate: day(daysAgo(7))],
                [endDate: day(new Date())],
                [startDate: day(new Date()), endDate: day(daysAgo(7))],
        ]
    }
}
