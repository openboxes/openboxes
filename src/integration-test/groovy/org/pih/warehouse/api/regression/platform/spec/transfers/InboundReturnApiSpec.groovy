package org.pih.warehouse.api.regression.platform.spec.transfers



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.receiving.PartialReceivingApi
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Inbound returns: stock coming back to the main facility (the logged-in location) from a location that does not
 * manage inventory (here a ward), as the inbound return wizard does it - the lines name a lot and an expiry date but
 * no bin, placing the return creates its shipment straight from the lines (no picking), sending it ships it from the
 * ward, and the facility receives it like any inbound shipment.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class InboundReturnApiSpec extends TransfersSpec {

    static final String RETURN_LOT = 'RET-LOT-1'

    static final String EXPIRY = '12/31/2030'

    @Autowired
    PartialReceivingApi partialReceivingApi

    Location ward

    @Override
    void setupData() {
        super.setupData()
        ward = Location.withNewTransaction {
            // A location type the wizard offers as a return origin: no inventory management, not a supplier, and a
            // place of its own (not a bin, zone or internal location)
            LocationType wardType = LocationType.list().find { LocationType type ->
                !type.supports(ActivityCode.MANAGE_INVENTORY) && !(type.locationTypeCode in [LocationTypeCode.SUPPLIER,
                        LocationTypeCode.BIN_LOCATION, LocationTypeCode.INTERNAL, LocationTypeCode.ZONE])
            }
            assert wardType: 'no location type without inventory management in the test data'
            // Built as a facility first: every location save refreshes product availability, which needs an
            // inventory (synchronous in tests); then given the ward's type
            Location built = new LocationTestBuilder().name(randomUtil.randomStringFieldValue('Return ward'))
                    .asFacility().build(true)
            built.inventory.warehouse = built
            built.inventory.save(failOnError: true, flush: true)
            built.locationType = wardType
            built.save(failOnError: true, flush: true)
            return built
        }
    }

    private JSONObject returnItem(int quantity, String id = null) {
        JSONObject json = new JSONObject()
                .put('product', new JSONObject().put('id', item.id))
                .put('lotNumber', RETURN_LOT)
                .put('expirationDate', EXPIRY)
                .put('quantity', quantity)
        if (id) {
            json.put('id', id)
        }
        return json
    }

    private JSONObject inboundReturn(String status, JSONArray items) {
        return new JSONObject()
                .put('type', 'RETURN_ORDER')
                .put('status', status)
                .put('origin', new JSONObject().put('id', ward.id))
                .put('destination', new JSONObject().put('id', facility.id))
                .put('stockTransferItems', items)
    }

    /** An inbound return of the given quantity, created and placed (which creates its shipment). Returns its id. */
    private String placedInboundReturn(int quantity) {
        JsonPath created = stockTransferApi.create(inboundReturn('PENDING', new JSONArray().put(returnItem(quantity)))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String id = created.getString('data.id')
        String itemId = created.getList('data.stockTransferItems', Map).find { it['product.id'] == item.id }.id
        stockTransferApi.update(id, Method.PUT,
                inboundReturn('PLACED', new JSONArray().put(returnItem(quantity, itemId)))
                        .put('dateShipped', new Date().format('MM/dd/yyyy'))
                        .put('expectedDeliveryDate', (new Date() + 3).format('MM/dd/yyyy')).toString(),
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.status', Matchers.equalTo('PLACED'))
                        .build())
        return id
    }

    private Map<String, Object> shipmentOf(String id) {
        return Order.withNewSession {
            Shipment shipment = Order.get(id).shipments?.find { true }
            if (!shipment) {
                return [:]
            }
            [id    : shipment.id, status: shipment.currentStatus, origin: shipment.origin?.id,
             items : shipment.shipmentItems.collect { ShipmentItem line ->
                 [id: line.id, lot: line.inventoryItem?.lotNumber, expiry: line.inventoryItem?.expirationDate
                         ?.format(Constants.EXPIRATION_DATE_FORMAT), quantity: line.quantity, bin: line.binLocation?.id]
             }]
        }
    }

    private OrderStatus statusOf(String id) {
        return Order.withNewSession { Order.get(id).status }
    }

    /** What the shipment took out of any inventory, and what its receipts put into the facility, as quantities. */
    private Map<String, List<Integer>> transacted(String shipmentId) {
        return Transaction.withNewSession {
            Shipment shipment = Shipment.get(shipmentId)
            [out: Transaction.findAllByOutgoingShipment(shipment).collectMany { it.transactionEntries*.quantity }
                    .collect { it as int },
             in : Transaction.findAllByIncomingShipment(shipment)
                     .findAll { it.inventory.id == Location.get(facility.id).inventory.id }
                     .collectMany { Transaction transaction ->
                         transaction.transactionEntries.findAll { TransactionEntry entry ->
                             entry.inventoryItem.lotNumber == RETURN_LOT
                         }*.quantity
                     }.collect { it as int }]
        }
    }

    private static String receiving(String status, String shipmentItemId, Integer quantity) {
        JSONArray shipmentItems = new JSONArray()
        shipmentItems.add(new JSONObject().put('shipmentItemId', shipmentItemId).put('quantityReceiving', quantity))
        JSONArray containers = new JSONArray()
        containers.add(new JSONObject().put('shipmentItems', shipmentItems))
        String delivered = new Date(System.currentTimeMillis() - 60000).format(Constants.DELIVERY_DATE_FORMAT)
        return new JSONObject().put('receiptStatus', status).put('dateDelivered', delivered)
                .put('containers', containers).toString()
    }

    void 'the origin of an inbound return is a location that does not manage inventory'() {
        expect: 'the wizard offers only such locations as the origin (isReturnOrder)'
        Location.withNewSession { !Location.get(ward.id).supports(ActivityCode.MANAGE_INVENTORY) }
    }

    void 'creating an inbound return keeps the lot and expiry of its lines and picks nothing'() {
        when:
        JsonPath created = stockTransferApi.create(inboundReturn('PENDING', new JSONArray().put(returnItem(3)))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        Map line = created.getList('data.stockTransferItems', Map).find { it['product.id'] == item.id }

        then:
        created.getString('data."origin.id"') == ward.id
        created.getString('data."destination.id"') == facility.id
        line.quantity == 3
        line.lotNumber == RETURN_LOT
        line.expirationDate == EXPIRY
        !line.picklistItems
        shipmentOf(created.getString('data.id')) == [:]
    }

}
