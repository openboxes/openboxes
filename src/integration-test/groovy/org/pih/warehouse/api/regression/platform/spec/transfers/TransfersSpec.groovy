package org.pih.warehouse.api.regression.platform.spec.transfers

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import java.time.Instant

import org.pih.warehouse.api.regression.platform.client.transfers.ReplenishmentApi
import org.pih.warehouse.api.regression.platform.client.transfers.StockTransferApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.product.Product

/**
 * Shared setup for the replenishment and stock transfer API specs: two bins of the main facility (the logged-in
 * location), a bulk bin with stock and a picking bin, and a product of its own per test.
 */
abstract class TransfersSpec extends RegressionApiSpec {

    static final String LOT = 'TRF-LOT-1'

    @Autowired
    ReplenishmentApi replenishmentApi

    @Autowired
    StockTransferApi stockTransferApi

    @Autowired
    InventoryService inventoryService

    Location bulkBin

    Location pickBin

    /** A product of this test only, so earlier tests' stock and orders don't show up. */
    Product item

    @Override
    void setupData() {
        // Committed in their own transaction: the server records stock against them during the setup
        (bulkBin, pickBin) = Location.withNewTransaction {
            ['Bulk', 'Pick'].collect { String name ->
                new LocationTestBuilder().name(randomUtil.randomStringFieldValue(name)).asBinLocation(facility)
                        .build(true)
            }
        }
        item = Product.withNewTransaction {
            new ProductTestBuilder().name(randomUtil.randomStringFieldValue('Transfer product')).category(rootCategory)
                    .build(true)
        }
    }

    /** An error response: the status and the common error body (errorCode is the status, errorMessage is set). */
    static ResponseSpecification error(int status) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.notNullValue())
                .build()
    }

    /** Records the given quantity of the product's lot in the bin (the main facility). */
    void stock(Location bin, int quantity, Product stocked = item) {
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(stocked)
                .inventory(facility.inventory)
                // In the past: a "now" can round up to a later second in the database, and a stock count after the
                // transfers of the test would then undo them
                .transactionDate(Instant.now().minusSeconds(120))
                .row(LOT, null, bin, quantity)
                .build())
    }

    InventoryItem lot(Product stocked = item) {
        return InventoryItem.withNewSession { InventoryItem.findByProductAndLotNumber(stocked, LOT) }
    }

    /** A minimum and maximum for the product in the bin: below the minimum, the bin needs replenishing. */
    void minMax(Location bin, int min, int max, Product stocked = item) {
        InventoryLevel.withNewTransaction {
            new InventoryLevel(inventory: facility.inventory, product: stocked, internalLocation: bin,
                    minQuantity: min, maxQuantity: max).save(failOnError: true)
        }
    }

    /**
     * Quantity on hand of the lot in the bin of the main facility, from the inventory transactions. (The product
     * availability table is refreshed inside the request, before the transfer commits, so it lags behind here.)
     */
    int onHand(Location bin, Product stocked = item) {
        return InventoryItem.withNewSession {
            InventoryItem lotInSession = InventoryItem.findByProductAndLotNumber(Product.get(stocked.id), LOT)
            inventoryService.getQuantity(Location.get(facility.id).inventory, Location.get(bin.id), lotInSession)
        } as int
    }
}
