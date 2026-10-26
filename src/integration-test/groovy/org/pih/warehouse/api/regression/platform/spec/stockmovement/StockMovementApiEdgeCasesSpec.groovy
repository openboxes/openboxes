package org.pih.warehouse.api.regression.platform.spec.stockmovement

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.specification.ResponseSpecification
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import static io.restassured.RestAssured.given

/**
 * The stock movement API's refusals and less travelled paths: report parameters, deleting what cannot be deleted,
 * split items, shipment-based stock movements, and the status options of a location that requires approval.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html PUT /chooseLocation/.*',
        'known response GET /stockMovements',
])
class StockMovementApiEdgeCasesSpec extends StockMovementSpec {

    private ResponseSpecification error(int status, String message) {
        return new ResponseSpecBuilder()
                .expectStatusCode(status)
                .expectBody('errorCode', Matchers.equalTo(status))
                .expectBody('errorMessage', Matchers.containsString(message))
                .build()
    }

    private Location approvingDepot() {
        return Location.withNewTransaction {
            Location depot = new LocationTestBuilder()
                    .name(randomUtil.randomStringFieldValue('Approving depot'))
                    .asFacility()
                    .build(false)
            depot.supportedActivities = ['APPROVE_REQUEST', 'MANAGE_INVENTORY', 'FULFILL_REQUEST'] as Set
            depot.save(flush: true, failOnError: true)
        }
    }

    @PendingFeature(reason = 'the 400 is {errorMessages: [...]}, not the error shape')
    void 'the shipped-items report needs a known destination with incoming shipments'() {
        expect:
        stockMovementApiWrapper.api.shippedItems([:], error(400, 'Destination parameter cannot be the empty'))
        stockMovementApiWrapper.api.shippedItems([destination: INVALID_ID], error(404, 'Destination does not exist'))
        stockMovementApiWrapper.api.shippedItems([destination: destination.id], error(404, 'No shipment items found'))
    }

    @PendingFeature(reason = 'the 400 is {errorMessages: [...]}, not the error shape')
    void 'the pending-items report needs a known origin with picked requests'() {
        expect:
        stockMovementApiWrapper.api.pendingRequisitionItems([:], error(400, 'Origin parameter cannot be the empty'))
        stockMovementApiWrapper.api.pendingRequisitionItems([origin: INVALID_ID], error(404, 'Origin does not exist'))
        stockMovementApiWrapper.api.pendingRequisitionItems([origin: destination.id],
                error(404, 'No pending requisition items found'))
    }

    @PendingFeature(reason = 'pending requisition details without parameters answer 500')
    void 'the pending-request details need a known origin and product'() {
        expect:
        stockMovementApiWrapper.api.getPendingRequisitionDetails([:], error(400, 'Both origin location and product'))
        stockMovementApiWrapper.api.getPendingRequisitionDetails(['origin.id': facility.id, 'product.id': INVALID_ID],
                error(400, 'Both origin location and product'))
    }

    @PendingFeature(reason = 'deleting a shipped movement answers 400, not 409')
    void 'a shipped stock movement cannot be deleted'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])
        stockMovementApiWrapper.updateStatusOK(id, 'PACKED')
        stockMovementApiWrapper.api.updateShipment(id, new JSONObject()
                .put('dateShipped', new Date().format('MM/dd/yyyy HH:mm Z'))
                .put('shipmentType', new JSONObject().put('id', '1'))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC)
        stockMovementApiWrapper.updateStatusOK(id, 'DISPATCHED')

        expect:
        stockMovementApiWrapper.api.delete(id, error(409, 'You cannot delete a shipment with status'))
    }

    @PendingFeature(reason = 'deleting from another location answers 400, not 403')
    void 'a stock movement from another location cannot be deleted from here'() {
        given:
        String id = stockMovementApiWrapper.createOutboundOK(approvingDepot(), destination, requestor, 'Elsewhere')

        expect:
        stockMovementApiWrapper.api.delete(id, error(403, 'not able to delete stock movement from your location'))
    }

    void 'an inbound shipment-based stock movement can have its items removed and be deleted'() {
        given:
        String body = new JSONObject()
                .put('description', 'Shipment to delete')
                .put('origin', jsonObjectUtil.asIdForRequestBody(facility))  // deleting is allowed from the origin
                .put('destination', jsonObjectUtil.asIdForRequestBody(destination))
                .put('requestedBy', jsonObjectUtil.asIdForRequestBody(requestor))
                .put('dateRequested', new Date().format('MM/dd/yyyy'))
                .toString()
        String id = stockMovementApiWrapper.api.createCombinedShipments(body, responseSpecUtil.CREATED_RESPONSE_SPEC)
                .jsonPath().getString('data.id')

        when:
        stockMovementApiWrapper.api.removeAllItems(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        stockMovementApiWrapper.api.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        Shipment.withNewSession { Shipment.get(id) } == null
    }

    void 'line items can carry split lines, and pack items split across pallets'() {
        given:
        stockUp(20)
        String id = requested([(product): 5])
        String itemId = firstItemId(id)
        JSONArray splits = new JSONArray()
        splits.add(new JSONObject().put('product', jsonObjectUtil.asIdForRequestBody(product)).put('quantityRequested', 1))
        JSONArray lineItems = new JSONArray()
        lineItems.add(new JSONObject().put('id', itemId).put('product', jsonObjectUtil.asIdForRequestBody(product))
                .put('quantityRequested', 6).put('splitLineItems', splits))

        when: 'split lines are accepted with the line item'
        JsonPath updated = stockMovementApiWrapper.api.updateItems(id,
                new JSONObject().put('lineItems', lineItems).toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        updated.getList('data.lineItems.quantityRequested', BigDecimal) == [6]

        when: 'picked, then packed as 4 on one pallet and 2 on another'
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')
        stockMovementApiWrapper.updateStatusOK(id, 'PICKING', [createPicklist: true])
        stockMovementApiWrapper.updateStatusOK(id, 'PICKED')
        String shipmentItemId = Shipment.withNewSession {
            Shipment.findByRequisition(Requisition.get(id)).shipmentItems.first().id
        }
        JSONArray splitPack = new JSONArray()
        splitPack.add(new JSONObject().put('shipmentItemId', shipmentItemId).put('quantityShipped', 2)
                .put('palletName', 'Pallet 2'))
        JSONArray packPageItems = new JSONArray()
        packPageItems.add(new JSONObject().put('shipmentItemId', shipmentItemId).put('quantityShipped', 4)
                .put('palletName', 'Pallet 1').put('splitLineItems', splitPack))
        JsonPath packed = stockMovementApiWrapper.api.updateShipmentItems(id,
                new JSONObject().put('packPageItems', packPageItems).toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then: 'the split line is packed on its own pallet'
        packed.getList('data.palletName').contains('Pallet 2')
        packed.getList('data.quantityShipped', BigDecimal).every { it > 0 }
    }

    void 'the status options of a location that requires approval'() {
        given: 'working from a location that requires approval'
        Location depot = approvingDepot()
        given().baseUri('http://localhost').port(serverPort).basePath('/openboxes').cookie(cookie)
                .accept('application/json').put("/api/chooseLocation/${depot.id}").then().statusCode(200)

        when:
        List<String> requests = stockMovementApiWrapper.api.requisitionStatusCodes([sourceType: 'ELECTRONIC'],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getList('data.id')
        List<String> outbound = stockMovementApiWrapper.api.requisitionStatusCodes([:], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.id')

        then: 'approval statuses are offered'
        outbound.contains('APPROVED')
        requests != outbound
    }
}
