package org.pih.warehouse.api.regression.platform.spec.purchasing



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.purchasing.CombinedShipmentApi
import org.pih.warehouse.common.domain.builder.product.ProductTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * Combined shipments as the inbound stock movement page builds them: an open purchase order of a vendor of the
 * test's own (10 of the test product to the main facility) and an empty inbound shipment from that vendor's supplier
 * location, filled through the item picker (find, add) or the CSV template (export, import).
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html POST /combinedShipmentItems/importTemplate/.*',
])
class CombinedShipmentApiSpec extends PurchasingSpec {

    @Autowired
    CombinedShipmentApi combinedShipmentApi

    Map<String, String> po

    String shipmentId

    @Override
    void setupData() {
        super.setupData()
        po = purchaseOrder(OrderStatus.PLACED, null, true)
        shipmentId = newShipment(supplier)
    }

    void 'products in the vendor\'s open orders, optionally matching a search'() {
        given:
        Map<String, ?> route = [vendor: supplier.id, destination: facility.id]

        expect:
        combinedShipmentApi.productsInOrders(route, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo([product.id]))
                .build())
        combinedShipmentApi.productsInOrders(route + [name: product.name], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo([product.id]))
                .build())
        combinedShipmentApi.productsInOrders(route + [name: 'zzz-no-such-product'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())
    }

    void 'the order picker lists the vendor\'s placed orders to this destination that have something left to ship'() {
        given: 'a pending order of the same vendor, which is not offered'
        Map<String, String> pending = purchaseOrder(OrderStatus.PENDING, null, true)

        expect:
        combinedShipmentApi.orderNumberOptions([vendor: supplier.id, destination: facility.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo([po.order]))
                .expectBody('data.orderNumber', Matchers.equalTo([po.orderNumber]))
                .build())
        pending.order != po.order
    }

    void 'order items of the vendor\'s open orders with what is left to ship'() {
        expect:
        combinedShipmentApi.findOrderItems(JsonOutput.toJson([vendor: supplier.id, destination: facility.id]),
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('orderItems.orderItemId', Matchers.equalTo([po.line]))
                        .expectBody('orderItems[0].orderNumber', Matchers.equalTo(po.orderNumber))
                        .expectBody('orderItems[0].quantityAvailable', Matchers.equalTo(10))
                        .build())
    }

    void 'adding order items to the shipment ships part of the line'() {
        when:
        combinedShipmentApi.addToShipment(shipmentId, items([orderItemId: po.line, quantityToShip: 4, sortOrder: 0]),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        shipmentQuantities() == [4]

        and: 'what is left to ship goes down'
        combinedShipmentApi.findOrderItems(JsonOutput.toJson([vendor: supplier.id, destination: facility.id]),
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('orderItems[0].quantityAvailable', Matchers.equalTo(6))
                        .build())
    }

    void 'the export template lists the vendor\'s open lines; a blank template has the header and an empty row'() {
        when:
        Response template = combinedShipmentApi.exportTemplate([vendor: supplier.id, destination: facility.id],
                responseSpecUtil.OK_RESPONSE_SPEC)
        Response blank = combinedShipmentApi.exportTemplate([blank: 'true'], responseSpecUtil.OK_RESPONSE_SPEC)
        List<String> lines = template.body().asString().readLines()

        then:
        template.contentType().startsWith('text/csv')
        template.header('Content-disposition').endsWith('.csv"')
        lines.size() == 2
        lines[1].contains(po.orderNumber)
        lines[1].contains(po.line)
        blank.body().asString().readLines().size() == 2
    }

    void 'importing the filled template adds its lines to the shipment'() {
        when:
        combinedShipmentApi.importTemplate(shipmentId, csv(po.line, 3), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build())

        then:
        shipmentQuantities() == [3]
    }

    // ---------------------------------------------------------------- helpers

    static ResponseSpecification error(int status) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.notNullValue())
                .build()
    }

    private static String items(Map... items) {
        return JsonOutput.toJson([itemsToAdd: items as List])
    }

    /** The template as the page exports it, filled in for one line. */
    private String csv(String orderItemId, int quantity, String orderNumber = po.orderNumber,
                       String productCode = product.productCode) {
        return 'Order number,Order Item id,Product code,Product name,Lot number,Expiry,Quantity to ship,UOM,' +
                'Pack level 1,Pack level 2,Recipient,Budget code\n' +
                "${orderNumber},${orderItemId},${productCode},Product,,,${quantity},,,,,\n"
    }

    /** An inbound shipment from the supplier to the main facility, no items yet. */
    private String newShipment(Location from) {
        return Shipment.withNewTransaction {
            new Shipment(
                    name: randomUtil.randomStringFieldValue('Combined shipment'),
                    origin: Location.get(from.id),
                    destination: Location.get(facility.id),
                    expectedShippingDate: new Date(),
                    shipmentType: ShipmentType.list().first())
                    .save(failOnError: true, flush: true).id
        }
    }

    private List<Integer> shipmentQuantities() {
        return Shipment.withNewTransaction {
            Shipment.get(shipmentId).shipmentItems*.quantity as List<Integer>
        }
    }
}
