package org.pih.warehouse.api.regression.platform.spec.fulfillment


import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.path.json.JsonPath
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.fulfillment.PicklistApi
import org.pih.warehouse.api.regression.platform.spec.stockmovement.StockMovementSpec
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.requisition.Requisition

/**
 * The picklists of outbound requests: reading and editing them as resources, and clearing their items.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class PicklistApiSpec extends StockMovementSpec {

    @Autowired
    PicklistApi picklistApi

    private String picklistOf(String stockMovementId) {
        return Requisition.withNewSession { Requisition.get(stockMovementId).picklist?.id }
    }

    private List<Integer> pickedQuantities(String picklistId) {
        return Picklist.withNewSession { Picklist.get(picklistId).picklistItems*.quantity }
    }

    void 'a picklist reads back with its items, and is listed'() {
        given:
        stockUp(20)
        String stockMovementId = picking([(product): 5])
        String picklistId = picklistOf(stockMovementId)

        when:
        JsonPath read = picklistApi.read(picklistId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        // The newest few only: the list is unpaged by default, and a test database holds every picklist ever made
        JsonPath listed = picklistApi.list([max: 5, sort: 'dateCreated', order: 'desc'], responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath()

        then:
        read.getString('data.id') == picklistId
        read.getString("data.'requisition.id'") == stockMovementId
        read.getList('data.picklistItems.quantityPicked') == [5]
        read.getList("data.picklistItems.'requisitionItem.id'") == [firstItemId(stockMovementId)]
        listed.getList('data').size() <= 5
        listed.getList('data.id').contains(picklistId)
    }

    void 'a picklist is updated with PUT or POST'() {
        given:
        stockUp(20)
        String picklistId = picklistOf(picking([(product): 5]))

        when:
        JsonPath put = picklistApi.update(picklistId, new JSONObject().put('name', 'Morning pick').toString(),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath post = picklistApi.updateWithPost(picklistId,
                new JSONObject().put('description', 'Aisle 3 first').toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        put.getString('data.name') == 'Morning pick'
        post.getString('data.description') == 'Aisle 3 first'
        Picklist.withNewSession { [Picklist.get(picklistId).name, Picklist.get(picklistId).description] } ==
                ['Morning pick', 'Aisle 3 first']
    }

    void 'a picklist is created for a request, then deleted'() {
        given:
        String stockMovementId = requested([(product): 5])
        String body = new JSONObject()
                .put('name', 'Created picklist')
                .put('requisition', jsonObjectUtil.asIdForRequestBody(Requisition.withNewSession { Requisition.get(stockMovementId) }))
                .toString()

        when:
        String picklistId = picklistApi.create(body, responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath().getString('data.id')

        then:
        picklistOf(stockMovementId) == picklistId

        when:
        picklistApi.delete(picklistId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        Picklist.withNewSession { Picklist.get(picklistId) } == null
        picklistApi.read(picklistId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC).jsonPath().getInt('errorCode') == 404
    }

    void 'clearing a picklist removes its items and keeps the picklist'() {
        given:
        stockUp(20)
        String stockMovementId = picking([(product): 5])
        String picklistId = picklistOf(stockMovementId)

        when:
        picklistApi.clearPicklist(picklistId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        pickedQuantities(picklistId) == []
        picklistOf(stockMovementId) == picklistId
    }

    void 'clearing a picklist that does not exist is a 404'() {
        expect:
        picklistApi.clearPicklist('no-such-picklist', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
                .jsonPath().getString('errorMessage').contains('no-such-picklist')
    }
}
