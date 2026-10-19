package org.pih.warehouse.api.spec.shipping

import io.restassured.builder.ResponseSpecBuilder
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.client.shipping.CombinedShipmentItemApiWrapper
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.common.domain.builder.order.OrderItemTestBuilder
import org.pih.warehouse.common.domain.builder.order.OrderTestBuilder
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.common.domain.builder.shipping.ShipmentTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.PurchaseOrderIdentifierService
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment

class CombinedShipmentItemApiSpec extends ApiSpec {

    static final String CSV_HEADER = "Order number,Order Item id,Product code,Product name,Lot number,Expiry," +
            "Quantity to ship,UOM,Pack level 1,Pack level 2,Recipient,Budget code"

    @Autowired
    CombinedShipmentItemApiWrapper combinedShipmentItemApiWrapper

    @Autowired
    PurchaseOrderIdentifierService purchaseOrderIdentifierService

    @Shared
    Location supplier

    @Shared
    Shipment shipment

    @Shared
    Order order

    @Shared
    Product orderedProduct

    @Shared
    OrderItem orderItem

    @Override
    void setupData() {
        supplier = new LocationTestBuilder().name(randomUtil.randomStringFieldValue("Supplier")).asFacility().build(true)
        shipment = new ShipmentTestBuilder().origin(supplier).destination(facility).build(true)
        order = new OrderTestBuilder().origin(supplier).destination(facility).build()
        order.orderNumber = purchaseOrderIdentifierService.generate(order)
        order.save(failOnError: true)
        orderedProduct = new ProductTestBuilder().randomizedName().category(rootCategory).build(true)
        orderItem = new OrderItemTestBuilder().order(order).product(orderedProduct).quantity(10).build(true)
    }

    @Override
    void cleanupData() {
        Order.get(order.id)?.delete()
        Shipment.get(shipment.id)?.delete()
        Product.get(orderedProduct.id)?.delete()
    }

    void 'add items to shipment should create shipment items for given order items'() {
        given:
        String body = new JSONObject()
                .put('itemsToAdd', new JSONArray([
                        new JSONObject().put('orderItemId', orderItem.id).put('quantityToShip', 3).put('sortOrder', 0),
                ]))
                .toString()

        expect:
        combinedShipmentItemApiWrapper.api.addToShipment(shipment.id, body, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(shipment.id))
                .expectBody('data.shipmentItems', Matchers.hasSize(1))
                .expectBody('data.shipmentItems[0].quantity', Matchers.equalTo(3))
                .expectBody('data.shipmentItems[0].shipment.id', Matchers.equalTo(shipment.id))
                .build())
    }

    void 'add items to shipment should fail when shipment does not exist'() {
        given:
        String body = new JSONObject().put('itemsToAdd', new JSONArray()).toString()

        expect:
        combinedShipmentItemApiWrapper.api.addToShipment(INVALID_ID, body, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo("Shipment not found"))
                .build())
    }

    void 'import template should add items from csv file to shipment'() {
        given:
        String csv = CSV_HEADER + "\n${order.orderNumber},,${orderedProduct.productCode},,,,4,,,,,"

        expect:
        combinedShipmentItemApiWrapper.api.importTemplate(shipment.id, "import.csv", csv, "text/csv",
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody(Matchers.equalTo("Successfully imported template"))
                        .build())
    }

    void 'import template should fail when csv contains invalid lines'() {
        given:
        String csv = CSV_HEADER + "\nUNKNOWN-PO,,${orderedProduct.productCode},,,,4,,,,,"

        expect:
        combinedShipmentItemApiWrapper.api.importTemplate(shipment.id, "import.csv", csv, "text/csv",
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .expectBody('errorMessage', Matchers.containsString("Row 1: There is no order with number: UNKNOWN-PO"))
                        .build())
    }

    void 'import template should fail when file is not a csv'() {
        expect:
        combinedShipmentItemApiWrapper.api.importTemplate(shipment.id, "import.txt", CSV_HEADER, "text/plain",
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .expectBody('errorMessage', Matchers.equalTo("File must be in CSV format"))
                        .build())
    }
}
