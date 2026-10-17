package org.pih.warehouse.api.regression.platform.spec.transfers

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Method
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.api.regression.platform.base.AccessCheckApi
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.inventory.Transaction

/**
 * Outbound returns: a return order from the main facility to another depot is picked, placed (which creates its
 * shipment), sent, and can be rolled back.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ReturnOrderApiSpec extends TransfersSpec {

    Location depot

    @Override
    void setupData() {
        super.setupData()
        stock(bulkBin, 20)
        depot = otherFacility('Return depot')
    }

    /** Another facility, committed (the server reads it). */
    private Location otherFacility(String name) {
        return Location.withNewTransaction {
            Location built = new LocationTestBuilder().name(randomUtil.randomStringFieldValue(name))
                    .asFacility().build(true)
            // The test data builder gives the facility an inventory of its own making; point it back at the facility
            built.inventory.warehouse = built
            built.inventory.save(failOnError: true, flush: true)
            return built
        }
    }

    private JSONObject returnItem(int quantity, String id = null) {
        JSONObject json = new JSONObject()
                .put('product', new JSONObject().put('id', item.id))
                .put('inventoryItem', new JSONObject().put('id', lot().id))
                .put('lotNumber', LOT)
                .put('originBinLocation', new JSONObject().put('id', bulkBin.id))
                .put('quantity', quantity)
        if (id) {
            json.put('id', id)
        }
        return json
    }

    private JSONObject returnOrder(String status, JSONArray items) {
        return new JSONObject()
                .put('type', 'RETURN_ORDER')
                .put('status', status)
                .put('origin', new JSONObject().put('id', facility.id))
                .put('destination', new JSONObject().put('id', depot.id))
                .put('stockTransferItems', items)
    }

    /** An outbound return of 4, placed: picked from the bulk bin, with its shipment. Returns its id. */
    private String placedReturn() {
        JsonPath created = stockTransferApi.create(returnOrder('PENDING', new JSONArray().put(returnItem(4)))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String id = created.getString('data.id')
        String itemId = created.getList('data.stockTransferItems', Map).find { it['product.id'] == item.id }.id
        String shipped = new Date().format('MM/dd/yyyy')
        String delivery = (new Date() + 3).format('MM/dd/yyyy')
        stockTransferApi.update(id, Method.PUT,
                returnOrder('PLACED', new JSONArray().put(returnItem(4, itemId)))
                        .put('dateShipped', shipped)
                        .put('expectedDeliveryDate', delivery).toString(),
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.status', Matchers.equalTo('PLACED'))
                        .expectBody('data.dateShipped', Matchers.equalTo(shipped))
                        .expectBody('data.expectedDeliveryDate', Matchers.equalTo(delivery))
                        .build())
        return id
    }

    /** The bins and quantities the return's shipment took out of the facility, as "bin id:quantity". */
    private List<String> shippedOut(String id) {
        return Order.withNewSession {
            Shipment shipment = Order.get(id).shipments?.find { true }
            Transaction.findAllByOutgoingShipment(shipment).collectMany { Transaction transaction ->
                transaction.transactionEntries.collect { "${it.binLocation?.id}:${it.quantity}".toString() }
            }
        }
    }

    private OrderStatus statusOf(String id) {
        return Order.withNewSession { Order.get(id).status }
    }

    @PendingFeature(reason = 'an outbound return naming only the origin bin is picked from no bin')
    void 'an outbound return is picked from the bin it is taken from (the request names only that bin)'() {
        when:
        JsonPath created = stockTransferApi.create(returnOrder('PENDING', new JSONArray().put(returnItem(4)))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String line = "data.stockTransferItems.find { it['product.id'] == '${item.id}' }.picklistItems"

        then:
        created.getString('data."destination.id"') == depot.id
        stockTransferApi.read(created.getString('data.id'), new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody("${line}.quantity".toString(), Matchers.contains(4))
                .expectBody("${line}.'originBinLocation.id'".toString(), Matchers.contains(bulkBin.id))
                .build())
    }

}
