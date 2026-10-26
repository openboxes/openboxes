package org.pih.warehouse.api.regression.platform.spec.receiving

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.fulfillment.FulfillmentApiWrapper
import org.pih.warehouse.api.regression.platform.client.receiving.ReceiptApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

/**
 * Shared setup for the receiving and putaway API specs: a supplying depot with stock, and inbound shipments from it
 * to the main facility (the logged-in location), shipped through the packing list import.
 */
abstract class ReceivingSpec extends RegressionApiSpec {

    static final String LOT = 'RCV-LOT-1'

    @Autowired
    FulfillmentApiWrapper fulfillmentApiWrapper

    @Autowired
    ReceiptApi receiptApi

    Location supplier

    User requestor

    @Override
    void setupData() {
        supplier = new LocationTestBuilder()
                .name(randomUtil.randomStringFieldValue('Supplying depot'))
                .asFacility()
                .build(true)
        // The test data builder gives the depot an inventory of its own making; point it back at the depot
        supplier.inventory.warehouse = supplier
        supplier.inventory.save(failOnError: true, flush: true)
        requestor = User.findByUsername(username)
    }

    Product newProduct() {
        return Product.withNewTransaction {
            new ProductTestBuilder()
                    .name(randomUtil.randomStringFieldValue('Product'))
                    .category(rootCategory)
                    .build(true)
        }
    }

    /** An inbound shipment of the given quantities (one lot per product, rows packed as rowExtras say) from the supplier; returns its id. */
    String inbound(Map<Product, Integer> items = [(product): 10], Map rowExtras = [:]) {
        items.each { Product shipped, Integer quantity ->
            recordStockApiWrapper.saveRecordStockOK(supplier, new RecordInventoryCommandTestBuilder()
                    .product(shipped)
                    .inventory(supplier.inventory)
                    .transactionDateNow()
                    .row(LOT, null, null, quantity)
                    .build())
        }
        List<Map> rows = items.collect { Product shipped, Integer quantity ->
            [product: shipped.productCode, lotNumber: LOT, quantityPicked: quantity] + rowExtras
        }
        String stockMovementId = fulfillmentApiWrapper.api.save(
                fulfillmentApiWrapper.packingListBody(supplier, facility, requestor, rows),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('data.id')
        return Shipment.withNewSession { Shipment.findByRequisition(Requisition.get(stockMovementId)).id }
    }

    /** The ids of the shipment's items, in product code order. */
    List<String> shipmentItemIds(String shipmentId) {
        return Shipment.withNewSession {
            Shipment.get(shipmentId).shipmentItems.sort { it.product.productCode }*.id
        }
    }

    /** Receives every line of the shipment in full (receipts/v2), into the default receiving bin. */
    void receiveInFull(String shipmentId) {
        JsonPath receipt = receiptApi.start(shipmentId, responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath()
        String receiptId = receipt.getString('data.id')
        JSONArray itemsToSave = new JSONArray()
        receipt.getList('data.receiptItems', Map).each { Map receiptItem ->
            Integer shipped = Shipment.withNewSession {
                Shipment.get(shipmentId).shipmentItems.find { it.id == receiptItem.shipmentItemId }.quantity
            }
            itemsToSave.add(new JSONObject()
                    .put('shipmentItem', new JSONObject().put('id', receiptItem.shipmentItemId))
                    .put('receiptItem', new JSONObject().put('id', receiptItem.id))
                    .put('quantityReceiving', shipped)
                    .put('binLocation', new JSONObject().put('id', receiptItem.binLocation.id)))
        }
        receiptApi.updateItemsBatch(receiptId, new JSONObject().put('itemsToSave', itemsToSave).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)
        receiptApi.complete(receiptId, new JSONObject().put('itemsToComplete', new JSONArray()).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)
    }
}
