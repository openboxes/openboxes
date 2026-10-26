package org.pih.warehouse.api.regression.platform.spec.stockmovement

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.client.inventory.RecordStockApiWrapper
import org.pih.warehouse.api.regression.platform.client.stockmovement.StockMovementApiWrapper
import org.pih.warehouse.api.regression.platform.client.stockmovement.StockMovementItemApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

/**
 * Shared setup for the stock movement API specs: a destination depot, the requesting user, stock of the main
 * product at the main facility, and helpers that take an outbound stock movement through its workflow via the API.
 */
abstract class StockMovementSpec extends RegressionApiSpec {

    @Autowired
    StockMovementApiWrapper stockMovementApiWrapper

    @Autowired
    StockMovementItemApi stockMovementItemApi

    @Autowired
    RecordStockApiWrapper recordStockApiWrapper

    Location destination

    User requestor

    @Override
    void setupData() {
        destination = new LocationTestBuilder()
                .name(randomUtil.randomStringFieldValue('Destination depot'))
                .asFacility()
                .build(true)
        requestor = User.findByUsername(username)
    }

    /** Records the given quantity of a product (default: the main product) at the main facility, in one lot. */
    void stockUp(int quantity, Product stockedProduct = product, String lotNumber = 'SM-LOT-1') {
        recordStockApiWrapper.saveRecordStockOK(facility, new RecordInventoryCommandTestBuilder()
                .product(stockedProduct)
                .inventory(facility.inventory)
                .transactionDateNow()
                .row(lotNumber, null, null, quantity)
                .build())
    }

    Product newProduct() {
        return new ProductTestBuilder()
                .name(randomUtil.randomStringFieldValue('Product'))
                .category(rootCategory)
                .build(true)
    }

    /** An outbound request from the main facility with the given line items; returns its id. */
    String requested(Map<Product, Integer> items = [(product): 5]) {
        String id = stockMovementApiWrapper.createOutboundOK(facility, destination, requestor,
                randomUtil.randomStringFieldValue('Request'))
        stockMovementApiWrapper.addItemsOK(id, items)
        return id
    }

    /** A request that has been validated and picked (picklist created from available stock). */
    String picking(Map<Product, Integer> items = [(product): 5]) {
        String id = requested(items)
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')
        stockMovementApiWrapper.updateStatusOK(id, 'PICKING', [createPicklist: true])
        return id
    }

    /** A picked request with its shipment created. */
    String picked(Map<Product, Integer> items = [(product): 5]) {
        String id = picking(items)
        stockMovementApiWrapper.updateStatusOK(id, 'PICKED')
        return id
    }

    /** The id of the first line item (the requisition item) of a stock movement. */
    String firstItemId(String stockMovementId) {
        return stockMovementApiWrapper.api.getStockMovementItems(stockMovementId, [:], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getString('data[0].id')
    }
}
