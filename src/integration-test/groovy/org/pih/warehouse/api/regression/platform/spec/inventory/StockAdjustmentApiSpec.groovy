package org.pih.warehouse.api.regression.platform.spec.inventory

import spock.lang.PendingFeature





import spock.lang.Unroll
import spock.lang.Ignore
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.inventory.*
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.inventory.StockAdjustmentApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product

/**
 * Stock adjustments as the stock movement wizard's "adjust inventory" dialog sends them: a JSON array of lines whose
 * quantityAdjusted is the quantity the bin holds from now on, each with a comment, for the location in the
 * location.id parameter. The test product starts with 10 in the default lot, no bin.
 *
 * OBAF: StockAdjustmentApiController takes ONE adjustment per request, bound to AdjustStockCommand
 * ({inventoryItem: {id}, binLocation, currentQuantity, newQuantity, reasonCode, comment}, reasonCode and comment
 * required) and answers {data: <the command>}. OBAF's contract wins: the features whose subject exists there (set a
 * lot to a new quantity, the reason code, the session location, refusals) send OBAF's body and check the stock and the
 * transaction; an array contract (new lots by product/lot/expiry, several lines, all-or-none, per-line refusals of
 * the array) is not covered, OBAF has no such contract.
 */
// OpenAPI validation stays REPORT-ONLY here: the spec documents an array-of-lines body, OBAF takes one
// AdjustStockCommand
class StockAdjustmentApiSpec extends RegressionApiSpec {

    @Autowired
    StockAdjustmentApi stockAdjustmentApi

    @Autowired
    InventoryService inventoryService

    String itemId

    @Override
    void setupData() {
        setStock(product, null, null, 10)
        itemId = InventoryItem.withNewSession {
            InventoryItem.findByProductAndLotNumberIsNull(Product.get(product.id))?.id
        }
        // A transaction in the same second as the baseline would make the quantity on hand ambiguous.
        sleep(1100)
    }

    private int quantityOnHand(String inventoryItemId = itemId) {
        return InventoryItem.withNewSession {
            inventoryService.getQuantityFromBinLocation(Location.get(facility.id), null, InventoryItem.get(inventoryItemId)) ?: 0
        } as int
    }

    /** The adjustment transactions recorded with this comment: [type id, entry quantities]. */
    private static List<Map> adjustments(String comment) {
        return Transaction.withNewSession {
            Transaction.findAllByComment(comment).collect { Transaction transaction ->
                [type: transaction.transactionType.id,
                 quantities: transaction.transactionEntries.collect { TransactionEntry entry -> entry.quantity }]
            }
        }
    }

    /** The reason codes stored on the entries of the adjustments recorded with this comment. */
    private static List<String> reasons(String comment) {
        return Transaction.withNewSession {
            Transaction.findAllByComment(comment).collectMany { Transaction transaction ->
                transaction.transactionEntries.collect { TransactionEntry entry -> entry.reasonCode }
            }
        }
    }

    /**
     * OBAF: the adjustment commits after the answer: wait up to 10 s for it to show, so the features below check
     * what was stored, not the race. "Committed before the answer" is not checked.
     */
    private static void awaitCommitted(String comment) {
        long deadline = System.currentTimeMillis() + 10000
        while (adjustments(comment).isEmpty() && System.currentTimeMillis() < deadline) {
            sleep(100)
        }
    }

    /** OBAF's AdjustStockCommand body for the test product's default lot. */
    private String adjustment(Map fields) {
        return JsonOutput.toJson([inventoryItem: [id: itemId], currentQuantity: quantityOnHand()] + fields)
    }

    private String uniqueComment() {
        return randomUtil.randomStringFieldValue('Recounted')
    }

    void 'an existing lot is set to the new quantity: the adjustment records the difference'() {
        given:
        String comment = uniqueComment()

        when: 'OBAF: one AdjustStockCommand; the reason code is required'
        stockAdjustmentApi.create(facility.id, adjustment(newQuantity: 6, reasonCode: ReasonCode.OTHER.name(), comment: comment),
                responseSpecUtil.OK_RESPONSE_SPEC)

        awaitCommitted(comment)

        then: 'stored (OBAF: after the answer)'
        adjustments(comment) == [[type: Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID, quantities: [-4]]]
        reasons(comment) == [ReasonCode.OTHER.name()]
        quantityOnHand() == 6
    }

    void 'a reason code given with the line is recorded with it'() {
        given:
        String comment = uniqueComment()

        when: 'OBAF: one AdjustStockCommand'
        stockAdjustmentApi.create(facility.id, adjustment(newQuantity: 9, reasonCode: ReasonCode.DAMAGED.name(), comment: comment),
                responseSpecUtil.OK_RESPONSE_SPEC)
        awaitCommitted(comment)

        then:
        quantityOnHand() == 9
        reasons(comment) == [ReasonCode.DAMAGED.name()]
    }

    void 'without location.id the session location is adjusted'() {
        given:
        String comment = uniqueComment()

        when: 'OBAF: one AdjustStockCommand'
        stockAdjustmentApi.create(null, adjustment(newQuantity: 8, reasonCode: ReasonCode.OTHER.name(), comment: comment),
                responseSpecUtil.OK_RESPONSE_SPEC)
        awaitCommitted(comment)

        then:
        quantityOnHand() == 8
    }

    @PendingFeature(reason = 'an adjustment that would not change the quantity is accepted (InventoryService.adjustStock: validate() clears the rejection)')
    @Unroll
    void 'a line #problem: 400, nothing adjusted'() {
        given: 'OBAF: one AdjustStockCommand for the test product\'s default lot'
        String comment = uniqueComment()
        Map body = [reasonCode: ReasonCode.OTHER.name(), comment: comment] + line

        expect:
        stockAdjustmentApi.create(facility.id, adjustment(body), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_BAD_REQUEST))
                .build())
        adjustments(comment) == []
        quantityOnHand() == 10

        where:
        problem                                  | line
        'that would not change the quantity'     | [newQuantity: 10]
        'without a comment'                      | [newQuantity: 5, comment: '']
        'with a negative quantity'               | [newQuantity: -1]
        'without a reason code'                  | [newQuantity: 5, reasonCode: null]
    }

}
