package org.pih.warehouse.api.regression.platform.spec.stockmovement

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * An outbound stock movement request: create, list, read, edit, status changes, documents and delete.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known html PUT /chooseLocation/.*',
        'known response GET /stockMovements',
])
class OutboundStockMovementApiSpec extends StockMovementSpec {

    void 'create answers 201 with the new request, which then reads back with its line items'() {
        when:
        String id = stockMovementApiWrapper.createOutboundOK(facility, destination, requestor, 'Weekly restock')
        JsonPath created = stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        created.getString('data.id') == id
        created.getString('data.description') == 'Weekly restock'
        created.getString('data.origin.id') == facility.id
        created.getString('data.destination.id') == destination.id
        created.getString('data.statusCode') == 'CREATED'
        created.getList('data.lineItems').isEmpty()

        when:
        JsonPath updated = stockMovementApiWrapper.addItemsOK(id, [(product): 7]).jsonPath()

        then:
        updated.getList('data.lineItems.product.id') == [product.id]
        updated.getList('data.lineItems.quantityRequested', BigDecimal) == [7]
        stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getInt('totalCount') == 1
    }

    void 'the list finds an outbound request by origin, by search term, and as CSV'() {
        given:
        String id = requested()
        String identifier = stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getString('data.identifier')

        when:
        JsonPath byOrigin = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id,
                                                             destination: destination.id], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath bySearch = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id, q: identifier,
                                                             requestedBy: requestor.id, createdBy: requestor.id,
                                                             requisitionStatusCode: 'CREATED', requestType: 'STOCK'],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String csv = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id, destination: destination.id,
                                                      format: 'csv'], responseSpecUtil.OK_RESPONSE_SPEC).body.asString()

        then:
        byOrigin.getList('data.id').contains(id)
        byOrigin.getInt('totalCount') >= 1
        bySearch.getList('data.id') == [id]
        csv.readLines()[0].startsWith('Status,Receipt Status,Identifier')
        csv.contains(identifier)
    }

    @PendingFeature(reason = 'an empty outbound list answers totalCount: [] instead of 0')
    void 'the list filters by creation date, and the inbound side sees a picked request as incoming'() {
        given:
        stockUp(20)
        String id = picked()
        String yesterday = (new Date() - 1).format('MM/dd/yyyy')
        String tomorrow = (new Date() + 1).format('MM/dd/yyyy')

        when:
        JsonPath inDateRange = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id,
                destination: destination.id, createdAfter: yesterday, createdBefore: tomorrow],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath beforeToday = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id,
                destination: destination.id, createdBefore: yesterday], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath inbound = stockMovementApiWrapper.api.list([direction: 'INBOUND', origin: facility.id,
                destination: destination.id, createdAfter: yesterday], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        inDateRange.getList('data.id').contains(id)
        !beforeToday.getList('data.id').contains(id)
        beforeToday.getInt('totalCount') == beforeToday.getList('data').size()
        inbound.getList('data.id').contains(id)
        inbound.getString("data.find { it.id == '${id}' }.destination.id") == destination.id
    }

    void 'the list filters by shipment type; the default type also matches movements without one'() {
        given: 'a picked request: its shipment has the default type'
        stockUp(20)
        String id = picked()
        String typeId = Shipment.withNewSession { Shipment.findByRequisition(Requisition.get(id)).shipmentType?.id }

        when:
        JsonPath sameType = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id,
                destination: destination.id, shipmentType: typeId ?: Constants.DEFAULT_SHIPMENT_TYPE_ID],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String otherTypeId = ShipmentType.withNewSession {
            ShipmentType.list()*.id.find { it != typeId && it != Constants.DEFAULT_SHIPMENT_TYPE_ID }
        }
        JsonPath otherType = stockMovementApiWrapper.api.list([direction: 'OUTBOUND', origin: facility.id,
                destination: destination.id, shipmentType: otherTypeId], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        otherTypeId != null
        sameType.getList('data.id').contains(id)
        !otherType.getList('data.id').contains(id)
    }

    @PendingFeature(reason = 'the refusal is {errorMessages: [...]}, not the error shape')
    void 'the list refuses a request without a direction, in the error shape'() {
        expect:
        stockMovementApiWrapper.api.list([origin: facility.id], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorMessage', Matchers.containsString('Direction parameter is required'))
                .build())
    }

    @PendingFeature(reason = 'updateRequisition answers an HTML redirect (302)')
    void 'updating the request changes its details and answers the updated request'() {
        given:
        String id = requested()
        String body = new JSONObject()
                .put('description', 'Changed description')
                .put('dateRequested', new Date().format('MM/dd/yyyy'))  // the format the web app sends
                .put('stocklist', new JSONObject().put('id', ''))
                .toString()

        when:
        JsonPath updated = stockMovementApiWrapper.api.updateRequisition(id, body, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()

        then:
        updated.getString('data.id') == id
        updated.getString('data.description') == 'Changed description'
        Requisition.withNewSession { Requisition.get(id).description } == 'Changed description'
    }

    void 'items can be changed, split, and removed when emptied'() {
        given:
        String id = requested([(product): 5])
        String itemId = firstItemId(id)

        when: 'the quantity changes'
        JSONArray lineItems = new JSONArray()
        lineItems.add(new JSONObject().put('id', itemId).put('product', jsonObjectUtil.asIdForRequestBody(product))
                .put('quantityRequested', 8).put('sortOrder', 100))
        JsonPath changed = stockMovementApiWrapper.api.updateItems(id,
                new JSONObject().put('lineItems', lineItems).toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        changed.getList('data.lineItems.quantityRequested', BigDecimal) == [8]

        when: 'the quantity is emptied and empty items are removed'
        lineItems[0].put('quantityRequested', 0)
        JsonPath emptied = stockMovementApiWrapper.api.updateItems(id, new JSONObject()
                .put('lineItems', lineItems).put('removeEmptyItems', true).toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        emptied.getList('data.lineItems').isEmpty()
    }

    void 'all items can be removed at once'() {
        given:
        String id = requested([(product): 5, (Product.withNewTransaction { newProduct() }): 2])

        when:
        stockMovementApiWrapper.api.removeAllItems(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getList('data.lineItems').isEmpty()
    }

    @PendingFeature(reason = 'DELETE status answers an HTML redirect (302)')
    void 'the status reads back and moves forward; rolling back a request that has not shipped leaves it as it is'() {
        given:
        String id = requested()

        expect:
        stockMovementApiWrapper.api.status(id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('data') == 'CREATED'

        when:
        stockMovementApiWrapper.updateStatusOK(id, 'VALIDATED')

        then:
        stockMovementApiWrapper.api.status(id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('data') == 'VERIFYING'

        when: 'rolling back answers the request (only a shipped request has something to roll back)'
        String before = stockMovementApiWrapper.api.read(id, [:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getString('data.statusCode')
        JsonPath rolledBack = stockMovementApiWrapper.api.deleteStatus(id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        rolledBack.getString('data.id') == id
        rolledBack.getString('data.statusCode') == before
        Requisition.withNewSession { Requisition.get(id).status } == RequisitionStatus.VERIFYING
    }

    @PendingFeature(reason = 'rollbackApproval answers 500')
    void 'an approved request can have its approval rolled back'() {
        given: 'a request from a depot that requires approval'
        Location approvingDepot = Location.withNewTransaction {
            Location depot = new LocationTestBuilder()
                    .name(randomUtil.randomStringFieldValue('Approving depot'))
                    .asFacility()
                    .build(false)
            depot.supportedActivities = ['APPROVE_REQUEST', 'MANAGE_INVENTORY', 'FULFILL_REQUEST'] as Set
            depot.save(flush: true, failOnError: true)
        }
        assert Location.withNewSession { Location.get(approvingDepot.id).approvalRequired }
        String id = stockMovementApiWrapper.createOutboundOK(approvingDepot, destination, requestor, 'Needs approval')
        stockMovementApiWrapper.addItemsOK(id, [(product): 1])
        stockMovementApiWrapper.updateStatusOK(id, 'PENDING_APPROVAL')
        stockMovementApiWrapper.updateStatusOK(id, 'APPROVED')

        expect:
        Requisition.withNewSession { Requisition.get(id).status } == RequisitionStatus.APPROVED

        when:
        stockMovementApiWrapper.api.rollbackApproval(id, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Requisition.withNewSession { Requisition.get(id).status } == RequisitionStatus.PENDING_APPROVAL
    }

    @PendingFeature(reason = 'rollbackApproval of a never-approved request answers 500, not 409')
    void 'rolling back the approval of a request that was never approved is refused with 409'() {
        given:
        String id = requested()

        expect:
        stockMovementApiWrapper.api.rollbackApproval(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CONFLICT)
                .expectBody('errorCode', Matchers.equalTo(409))
                .expectBody('errorMessage', Matchers.containsString('not in an approval state'))
                .build())
    }

    void 'documents list what can be printed or exported for the request'() {
        given:
        String id = requested()

        when:
        List<String> types = stockMovementApiWrapper.api.getDocuments(id, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.documentType')

        then:
        types.containsAll(['EXPORT', 'PICKLIST'])
    }

    void 'the status options depend on whether the list is of requests or outbound movements'() {
        when:
        List<String> outbound = stockMovementApiWrapper.api.requisitionStatusCodes([:], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getList('data.id')
        List<String> requests = stockMovementApiWrapper.api.requisitionStatusCodes([sourceType: 'ELECTRONIC'],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getList('data.id')

        then:
        outbound
        requests
        outbound != requests
    }

    @PendingFeature(reason = 'the packing list template download has no content type')
    void 'the packing list template downloads'() {
        when:
        Response response = stockMovementApiWrapper.api.downloadPackingListTemplate(responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        response.header('Content-disposition').contains('completedPackingList.xls')
        response.contentType.startsWith('application/vnd.ms-excel')
        response.body.asByteArray().length > 0
    }

    void 'a request can be deleted, and then no longer found'() {
        given:
        String id = requested()

        when:
        stockMovementApiWrapper.api.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        stockMovementApiWrapper.api.delete(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_NOT_FOUND)
                .expectBody('errorMessage', Matchers.containsString(id))
                .build())
    }
}
