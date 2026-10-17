package org.pih.warehouse.api.regression.platform.spec.reporting

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.builder.RegressionProductTestBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll

import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit

import org.pih.warehouse.api.regression.platform.client.reporting.IndicatorApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.CycleCount
import org.pih.warehouse.inventory.CycleCountItem
import org.pih.warehouse.inventory.CycleCountItemStatus
import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.CycleCountRequestStatus
import org.pih.warehouse.inventory.CycleCountRequestType
import org.pih.warehouse.inventory.CycleCountStatus
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.product.Product

/**
 * The cycle count report's indicator tiles (/api/reports/indicators/...) as the report page asks for them: the
 * current facility and an MM/dd/yyyy date range.
 *
 * The main facility collects data from every spec, so the tests compare a tile before and after their own changes
 * instead of expecting absolute numbers.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class IndicatorApiSpec extends RegressionApiSpec {

    static final List<String> TILES = ['productsInventoried', 'inventoryAccuracy', 'inventoryShrinkage']

    @Autowired
    IndicatorApi indicatorApi

    @Autowired
    AuthService authService

    @PendingFeature(reason = 'a range ending today misses today')
    void 'products inventoried counts the distinct products counted at the facility in the range'() {
        given:
        Map<String, String> today = dates(-1, 0)
        Map<String, String> lastMonth = dates(-40, -30)
        int before = productsInventoried(today)
        int beforeLastMonth = productsInventoried(lastMonth)
        Product first = newProduct()
        Product second = newProduct()

        when: 'both are counted, the first one twice'
        stock(first, 5, Instant.now().minusSeconds(300))
        stock(first, 7, Instant.now().minusSeconds(120))
        stock(second, 3, Instant.now().minusSeconds(120))

        then:
        productsInventoried(today) == before + 2

        and: 'a range that ended before the counts does not include them'
        productsInventoried(lastMonth) == beforeLastMonth
    }

    void 'the products inventoried tile has the shape the report page reads'() {
        expect:
        indicatorApi.productsInventoried([facility: facility.id] + dates(-1, 0), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.name', Matchers.equalTo('productsInventoried'))
                .expectBody('data.type', Matchers.equalTo('SINGLE'))
                .expectBody('data.value', Matchers.greaterThanOrEqualTo(0))
                .build())
    }

    @PendingFeature(reason = 'a range ending today misses today')
    void 'inventory shrinkage counts the products adjusted down in the range and the value lost'() {
        given: 'a product worth 2.50 a unit, counted at 10 before the range'
        Product shrunk = newProduct(2.5)
        stock(shrunk, 10, Instant.now().minus(3, ChronoUnit.DAYS))
        Map<String, String> today = dates(-1, 0)
        Map before = shrinkage(today)

        when: 'it is counted at 4 in the range: 6 units lost'
        stock(shrunk, 4, Instant.now().minusSeconds(120))
        Map after = shrinkage(today)

        then:
        after.name == 'inventoryShrinkage'
        after.type == 'DOUBLE'
        after.firstValue == before.firstValue + 1
        (after.secondValue as BigDecimal) - (before.secondValue as BigDecimal) == 15.0
    }

    void 'a product counted up in the range is not shrinkage'() {
        given:
        Product grown = newProduct(2.5)
        stock(grown, 4, Instant.now().minus(3, ChronoUnit.DAYS))
        Map<String, String> today = dates(-1, 0)
        Map before = shrinkage(today)

        when:
        stock(grown, 10, Instant.now().minusSeconds(120))

        then:
        shrinkage(today) == before
    }

    @PendingFeature(reason = 'a range ending today misses today')
    void 'inventory accuracy is the share of completed counts without a variance'() {
        given:
        Map<String, String> today = dates(-1, 0)
        Map before = accuracy(today)

        when: 'two completed counts in the range: one matches the stock, one is off by two'
        completedCount(newProduct(), 10, 10)
        completedCount(newProduct(), 10, 8)
        Map after = accuracy(today)

        then:
        after.name == 'inventoryAccuracy'
        after.type == 'DOUBLE'
        after.secondValue == before.secondValue + 2

        and: 'the percentage is accurate counts over all counts'
        accurateCounts(after) == accurateCounts(before) + 1
    }

    @Unroll
    void 'the #tile tile for one day from a time of day to the end of that day is not reversed'() {
        given: 'today from 06:00 on (a date and time) to today (a day)'
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(new Date())
        calendar.set(Calendar.HOUR_OF_DAY, 6)
        calendar.set(Calendar.MINUTE, 0)
        String start = new SimpleDateFormat('MM/dd/yyyy HH:mm XXX').format(calendar.getTime())
        String end = new SimpleDateFormat('MM/dd/yyyy').format(new Date())

        expect:
        indicatorApi."$tile"([facility: facility.id, startDate: start, endDate: end], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.name', Matchers.equalTo(tile))
                .build())

        where:
        tile << TILES
    }

    @Unroll
    void 'the #tile tile without dates covers all time'() {
        expect:
        indicatorApi."$tile"([facility: facility.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.name', Matchers.equalTo(tile))
                .build())

        where:
        tile << TILES
    }

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
     * A date range as the report page sends it: MM/dd/yyyy, days relative to today. The tests end the range today,
     * as the page does by default: the end day counts in full.
     */
    private static Map<String, String> dates(int startDays, int endDays) {
        SimpleDateFormat format = new SimpleDateFormat('MM/dd/yyyy')
        return [
                startDate: format.format(Date.from(Instant.now().plus(startDays, ChronoUnit.DAYS))),
                endDate  : format.format(Date.from(Instant.now().plus(endDays, ChronoUnit.DAYS))),
        ]
    }

    private int productsInventoried(Map<String, String> range) {
        return indicatorApi.productsInventoried([facility: facility.id] + range, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getInt('data.value')
    }

    private Map shrinkage(Map<String, String> range) {
        return indicatorApi.inventoryShrinkage([facility: facility.id] + range, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getMap('data')
    }

    private Map accuracy(Map<String, String> range) {
        return indicatorApi.inventoryAccuracy([facility: facility.id] + range, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getMap('data')
    }

    /** The number of accurate counts behind an accuracy tile (percentage x total, the percentage has 2 decimals). */
    private static int accurateCounts(Map tile) {
        return ((tile.firstValue as BigDecimal) * (tile.secondValue as int) / 100)
                .setScale(0, RoundingMode.HALF_UP).intValue()
    }

    private Product newProduct(BigDecimal price = 1) {
        return Product.withNewTransaction {
            new RegressionProductTestBuilder().name(randomUtil.randomStringFieldValue('Indicator product'))
                    .category(rootCategory).pricePerUnit(price).build(true)
        }
    }

    /** Counts the product at the main facility (default bin, no lot) at the given time. */
    private void stock(Product counted, int quantity, Instant when) {
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(counted)
                .inventory(facility.inventory)
                .transactionDate(when)
                .row(null, null, null, quantity)
                .build())
    }

    /**
     * A completed cycle count of one product at the main facility, as the cycle count pages leave it: the request
     * completed and the count's transaction recorded a minute ago.
     */
    private void completedCount(Product product, int quantityOnHand, int quantityCounted) {
        // The cycle count and its request record their creator from the current user (loaded in a session)
        User user = User.withNewTransaction {
            User found = User.findByUsername(username)
            authService.currentUser = found
            return found
        }
        try {
            saveCompletedCount(product, user, quantityOnHand, quantityCounted)
        } finally {
            authService.currentUser = null
        }
    }

    private void saveCompletedCount(Product product, User user, int quantityOnHand, int quantityCounted) {
        CycleCount.withNewTransaction {
            Product counted = Product.load(product.id)
            Location countedAt = Location.load(facility.id)
            InventoryItem item = new InventoryItem(product: counted, lotNumber: null).save(failOnError: true)
            CycleCount count = new CycleCount(facility: countedAt, status: CycleCountStatus.COMPLETED,
                    dateLastRefreshed: new Date())
            count.addToCycleCountItems(new CycleCountItem(facility: countedAt, inventoryItem: item, product: counted,
                    countIndex: 0, status: CycleCountItemStatus.APPROVED, quantityOnHand: quantityOnHand,
                    quantityCounted: quantityCounted, custom: false, dateCounted: new Date(), createdBy: user,
                    updatedBy: user))
            count.save(failOnError: true)
            new CycleCountRequest(facility: countedAt, product: counted, status: CycleCountRequestStatus.COMPLETED,
                    cycleCount: count, requestType: CycleCountRequestType.MANUAL_REQUEST, blindCount: false,
                    inventoryItemsCount: 1).save(failOnError: true)
            new Transaction(inventory: countedAt.inventory, cycleCount: count,
                    transactionType: TransactionType.load(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID),
                    transactionDate: Date.from(Instant.now().minusSeconds(60))).save(failOnError: true, flush: true)
        }
    }
}
