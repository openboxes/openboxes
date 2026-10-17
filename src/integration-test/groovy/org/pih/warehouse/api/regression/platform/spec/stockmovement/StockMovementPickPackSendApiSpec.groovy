package org.pih.warehouse.api.regression.platform.spec.stockmovement

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Picking, packing and sending an outbound stock movement, and the reports on picked and shipped stock.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html PUT /chooseLocation/.*',
        'known response GET /stockMovements',
])
class StockMovementPickPackSendApiSpec extends StockMovementSpec {

    private BigDecimal quantityPicked(String id) {
        return Requisition.withNewSession {
            Requisition.get(id).requisitionItems*.calculateQuantityPicked().sum() as BigDecimal
        }
    }

    private String shipmentItemId(String id) {
        return Shipment.withNewSession {
            Shipment.findByRequisition(Requisition.get(id)).shipmentItems.first().id
        }
    }

    private ShipmentStatusCode shipmentStatus(String id) {
        return Shipment.withNewSession { Shipment.findByRequisition(Requisition.get(id)).currentStatus }
    }

    void 'a picklist is created from the available stock, and validates'() {
        given:
        stockUp(20)
        String id = requested([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')

        when:
        stockMovementApiWrapper.api.createPickList(id, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        quantityPicked(id) == 5
        stockMovementApiWrapper.api.validatePicklist(id, responseSpecUtil.OK_RESPONSE_SPEC).statusCode == 200
    }

    void 'picking a product with no available stock is refused by the picklist validation'() {
        given: 'a request for a product that has no stock'
        String id = requested([(Product.withNewTransaction { newProduct() }): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')

        expect: 'creating the picklist validates it, and refuses'
        stockMovementApiWrapper.api.createPickList(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(400))
                .expectBody('errorMessages', Matchers.hasItem(Matchers.containsString('has no available inventory')))
                .build())
        quantityPicked(id) == 0
    }

    void 'the pick and pack steps read back their items'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])

        when:
        JsonPath pick = stockMovementApiWrapper.api.read(id, [stepNumber: 4, refreshPicklistItems: true],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath pack = stockMovementApiWrapper.api.read(id, [stepNumber: 5], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath send = stockMovementApiWrapper.api.read(id, [stepNumber: 6], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        pick.getInt('totalCount') == 1
        pack.getInt('totalCount') == 1
        send.getInt('totalCount') == 1
        pick.getString('data.id') == id
    }

    void 'picked stock that has not shipped shows in the pending reports'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])
        String requestNumber = Requisition.withNewSession { Requisition.get(id).requestNumber }

        when: 'the pending items of the origin, as CSV'
        String csv = stockMovementApiWrapper.api.pendingRequisitionItems([origin: facility.id],
                responseSpecUtil.OK_RESPONSE_SPEC).body.asString()

        then:
        csv.readLines()[0].startsWith('Shipment Number,Description,Destination')
        csv.contains(requestNumber)

        when: 'the pending requests of the product, as seen from another request'
        JsonPath details = stockMovementApiWrapper.api.getPendingRequisitionDetails(
                ['origin.id': facility.id, 'product.id': product.id, stockMovementId: 'another-request'],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then: 'grouped by destination, with the quantities of this request'
        Map forDestination = details.getList('data', Map).find { it['destination.name'] == destination.name }
        forDestination['requisition.requestNumber'] == requestNumber
        forDestination.quantityRequested == 5
    }

    void 'packing puts the shipment items on pallets and boxes'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])
        String itemId = shipmentItemId(id)
        JSONArray packPageItems = new JSONArray()
        packPageItems.add(new JSONObject()
                .put('shipmentItemId', itemId)
                .put('quantityShipped', 5)
                .put('palletName', 'Pallet 1')
                .put('boxName', 'Box A')
                .put('recipient', jsonObjectUtil.asIdForRequestBody(requestor)))

        when:
        JsonPath packed = stockMovementApiWrapper.api.updateShipmentItems(id,
                new JSONObject().put('packPageItems', packPageItems).toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        packed.getList('data.palletName') == ['Pallet 1']
        packed.getList('data.boxName') == ['Box A']
        ShipmentItem.withNewSession { ShipmentItem.get(itemId).container?.parentContainer?.name } == 'Pallet 1'
    }

    @PendingFeature(reason = 'DELETE status (roll back the send) answers an HTML redirect (302)')
    void 'sending ships the stock; the destination then sees it incoming; rolling back undoes the send'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'PACKED')
        String shipmentBody = new JSONObject()
                .put('destination', jsonObjectUtil.asIdForRequestBody(destination))
                .put('dateShipped', new Date().format('MM/dd/yyyy HH:mm Z'))  // the format the web app sends
                .put('shipmentType', new JSONObject().put('id', '1'))
                .put('trackingNumber', 'TRACK-123')
                .put('driverName', 'Driver')
                .put('comments', 'Handle with care')
                .toString()

        when: 'the shipment type as an object, then as the plain id (as the inbound send page sends it)'
        stockMovementApiWrapper.api.updateShipment(id, shipmentBody, responseSpecUtil.OK_RESPONSE_SPEC)
        stockMovementApiWrapper.api.updateShipment(id, new JSONObject().put('shipmentType', '1').toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)
        stockMovementApiWrapper.updateStatusOK(id, 'DISPATCHED')

        then:
        shipmentStatus(id) == ShipmentStatusCode.SHIPPED
        Shipment.withNewSession {
            Shipment shipment = Shipment.findByRequisition(Requisition.get(id))
            [shipment.referenceNumbers*.identifier.contains('TRACK-123'), shipment.driverName,
             shipment.additionalInformation]
        } == [true, 'Driver', 'Handle with care']

        when: 'the shipped-but-not-received items of the destination, as CSV'
        String csv = stockMovementApiWrapper.api.shippedItems([destination: destination.id],
                responseSpecUtil.OK_RESPONSE_SPEC).body.asString()

        then:
        csv.readLines()[0].startsWith('Code,Product Name,Quantity Incoming')
        csv.readLines().find { it.contains(product.productCode) }?.contains(',5,')

        when: 'rolling back the send'
        JsonPath rolledBack = stockMovementApiWrapper.api.deleteStatus(id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        rolledBack.getString('data.id') == id
        shipmentStatus(id) == ShipmentStatusCode.PENDING
    }
}
