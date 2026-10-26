package org.pih.warehouse.api.regression.platform.spec.inventory

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.inventory.*
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

import java.time.Instant

import org.pih.warehouse.api.regression.platform.client.inventory.CycleCountApi
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability

/**
 * Fixtures for the cycle count specs: a new product with stock at the main facility (each test its own product, so
 * the open-request rule never meets a request left by an earlier test or run), and helpers that walk a product
 * through request, start and count.
 */
abstract class CycleCountSpec extends RegressionApiSpec {

    @Autowired
    CycleCountApi cycleCountApi

    @Autowired
    ProductAvailabilityService productAvailabilityService

    /** A new product with the given quantity on hand at the main facility (default lot, default bin). */
    Product stockedProduct(int quantity) {
        Product unsaved = Product.withNewTransaction {
            new ProductTestBuilder()
                    .name("Cycle count product ${UUID.randomUUID().toString().take(8)}")
                    .category(rootCategory)
                    .build()
        }
        Product saved = productApiWrapper.saveOK(unsaved)
        // The product is new, so it has no transactions for a baseline to collide with. Dating its stock a few
        // seconds back keeps the transactions of a count (or an import) made right after out of its second without
        // waiting for the clock, as restock() has to.
        setStock(new RecordInventoryCommandTestBuilder()
                .product(saved)
                .inventory(facility.inventory)
                .transactionDate(Instant.now().minusSeconds(STOCK_BACKDATE_SECONDS))
                .row(null, null, null, quantity)
                .build())
        awaitQuantityOnHand(saved, quantity)
        return saved
    }

    /** How far back stockedProduct() dates the baseline (its adjustment is a second later). */
    static final long STOCK_BACKDATE_SECONDS = 5

    /** Sets the product's stock and waits until the facility's availability shows it (written after the response). */
    void restock(Product product, int quantity) {
        // A new stock baseline in the same second as the previous transaction is refused (MySQL rounds to the
        // second)
        sleep(1100)
        setStock(product, null, null, quantity)
        awaitQuantityOnHand(product, quantity)
        // Nor may the transactions of a count completed right after (its baseline is dated a second before the
        // submit) share the second of this baseline
        sleep(2100)
    }

    void awaitQuantityOnHand(Product product, int quantity) {
        long deadline = System.currentTimeMillis() + 15000
        while (quantityOnHand(product) != quantity) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("Quantity on hand of ${product.productCode} never became ${quantity}, " +
                        "it is ${quantityOnHand(product)}")
            }
            sleep(50)
        }
    }

    /**
     * Waits until the stock the API's transactions leave equals the quantity, refreshing the product's availability
     * from those transactions each time. Needed after operations whose own refresh can run before their stock
     * transactions are committed (completing a cycle count, the inventory import).
     */
    void awaitTransactedQuantityOnHand(Product product, int quantity, long timeoutMs = 15000) {
        long deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            ProductAvailability.withNewTransaction {
                productAvailabilityService.refreshProductsAvailability(facility.id, [product.id], true)
            }
            if (quantityOnHand(product) == quantity) {
                return
            }
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("Quantity on hand of ${product.productCode} never became ${quantity}, " +
                        "it is ${quantityOnHand(product)}")
            }
            sleep(200)
        }
    }

    int quantityOnHand(Product product) {
        return ProductAvailability.withNewSession {
            (ProductAvailability.findAllByLocationAndProduct(facility, Product.load(product.id))
                    *.quantityOnHand.sum() ?: 0) as int
        }
    }

    String adminPersonId() {
        return User.withNewSession { User.findByUsername(username).id }
    }

    /** Requests a count of the product; returns the request id. */
    String requestCount(Product product) {
        Response created = cycleCountApi.createRequests(facility.id, new JSONObject()
                .put('requests', new JSONArray().put(new JSONObject()
                        .put('product', product.id)
                        .put('blindCount', false)))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC)
        return created.jsonPath().getString('data[0].id')
    }

    /** Starts the count of the request; returns the count as JSON (id, status, cycleCountItems). */
    Map startCount(String requestId) {
        Response started = cycleCountApi.start(facility.id, startBody(requestId), null, responseSpecUtil.OK_RESPONSE_SPEC)
        return started.jsonPath().getMap('data[0]')
    }

    String startBody(String requestId) {
        return new JSONObject()
                .put('requests', new JSONArray().put(new JSONObject().put('cycleCountRequest', requestId)))
                .toString()
    }

    String recountBody(String requestId) {
        return new JSONObject()
                .put('requests', new JSONArray().put(new JSONObject()
                        .put('cycleCountRequest', requestId)
                        .put('countIndex', 1)))
                .toString()
    }

    /** Records a counted quantity on an item of the initial count (or of the recount). */
    void recordCount(String itemId, int quantity, boolean recount = false) {
        cycleCountApi.updateItem(facility.id, itemId, new JSONObject()
                // the item id goes in the body as well, as the web app sends it
                .put('id', itemId)
                .put('quantityCounted', quantity)
                .put('recount', recount)
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC)
    }

    String submitBody(boolean requireRecountOnDiscrepancy, boolean refreshQuantityOnHand = false,
                      boolean failOnOutdatedQuantity = false) {
        return new JSONObject()
                .put('requireRecountOnDiscrepancy', requireRecountOnDiscrepancy)
                .put('refreshQuantityOnHand', refreshQuantityOnHand)
                .put('failOnOutdatedQuantity', failOnOutdatedQuantity)
                .toString()
    }

    static ResponseSpecBuilder ok() {
        return new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
    }
}
