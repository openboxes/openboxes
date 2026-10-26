package org.pih.warehouse.api.regression.platform.spec.transfers

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.path.json.JsonPath
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

/**
 * Bin replenishment: the picking bin of a product falls below its minimum, the bulk bin has stock; a replenishment
 * moves stock from the bulk bin into the picking bin through a picklist.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class ReplenishmentApiSpec extends TransfersSpec {

    @Override
    void setupData() {
        super.setupData()
        stock(bulkBin, 20)
        minMax(pickBin, 5, 10)
    }

    /**
     * The replenishment page's create request: the checked requirements with a quantity. The page lists the
     * requirements with flattened "product.id" keys and sends them back as nested objects.
     */
    private String fromRequirements(int quantity) {
        return new JSONObject().put('replenishmentItems', new JSONArray().put(new JSONObject()
                .put('product', new JSONObject().put('id', item.id))
                .put('binLocation', new JSONObject().put('id', pickBin.id))
                .put('location', new JSONObject().put('id', facility.id))
                .put('quantity', quantity))).toString()
    }

    private JsonPath created(int quantity = 10) {
        return replenishmentApi.createFromRequirements(fromRequirements(quantity),
                responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath()
    }

    /** The quantities picked for the product's item of the replenishment. */
    private List picked(String id) {
        JsonPath replenishment = replenishmentApi.read(id, false, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        return itemOf(replenishment).picklistItems*.quantity
    }

    private Map itemOf(JsonPath replenishment) {
        return replenishment.getList('data.replenishmentItems', Map).find { it.product?.id == item.id }
    }

    void 'requirements list the picking bin below its minimum with what can be moved into it'() {
        when:
        List<Map> requirements = replenishmentApi.requirements(['location.id': facility.id],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getList('data', Map)
        Map requirement = requirements.find { it['product.id'] == item.id }

        then:
        requirement['binLocation.id'] == pickBin.id
        requirement.quantityInBin == 0
        requirement.minQuantity == 5
        requirement.maxQuantity == 10
        requirement.quantityAvailable == 20
        requirement.quantityNeeded == 10
    }

    void 'requirements by inventory level status: below maximum also lists bins below their minimum'() {
        expect:
        replenishmentApi.requirements(['location.id': facility.id, inventoryLevelStatus: 'BELOW_MAXIMUM'],
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody("data.find { it['product.id'] == '${item.id}' }.'binLocation.id'".toString(),
                                Matchers.equalTo(pickBin.id))
                        .build())
        replenishmentApi.requirements(['location.id': facility.id, inventoryLevelStatus: 'IN_STOCK'],
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.findAll { it["product.id"] == "' + item.id + '" }', Matchers.empty())
                        .build())
    }

    @PendingFeature(reason = 'an unknown location or inventory level status answers 500, not 400')
    void 'requirements need a known location and a known inventory level status'() {
        expect:
        replenishmentApi.requirements([:], error(HttpStatus.SC_BAD_REQUEST))
        replenishmentApi.requirements(['location.id': INVALID_ID], error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.requirements(['location.id': facility.id, inventoryLevelStatus: 'NOT_A_STATUS'],
                error(HttpStatus.SC_BAD_REQUEST))
    }

    void 'a replenishment created from requirements picks the stock from the bulk bin'() {
        when:
        JsonPath replenishment = created()

        then:
        replenishment.getString('data.status') == 'PENDING'
        replenishment.getString('data.replenishmentNumber')
        replenishment.getString('data."origin.id"') == facility.id

        when:
        Map line = itemOf(replenishmentApi.read(replenishment.getString('data.id'), false,
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath())

        then: 'the read fills in what the bin holds and needs, and the pick from the bulk bin'
        line.quantity == 10
        line['currentBinLocation.id'] == pickBin.id
        line.minQuantity == 5
        line.maxQuantity == 10
        line.quantityInBin == 0
        line.picklistItems*.'binLocation.id' == [bulkBin.id]
        line.picklistItems*.quantity == [10]
    }

    void 'POST /api/replenishments creates a replenishment as well, and the list shows it'() {
        when:
        String id = replenishmentApi.create(fromRequirements(4), responseSpecUtil.CREATED_RESPONSE_SPEC)
                .jsonPath().getString('data.id')

        then:
        replenishmentApi.list(new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(id)).build())
        replenishmentApi.read(id, true, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id)).build())
    }

    @PendingFeature(reason = 'a replenishment validation failure answers 500, not 400')
    void 'a replenishment cannot ask for more than the other bins hold'() {
        expect:
        replenishmentApi.createFromRequirements(fromRequirements(50),
                error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'an item without product/bin answers 500, not 400')
    void 'a replenishment item needs a product and a bin'() {
        expect:
        replenishmentApi.createFromRequirements(new JSONObject().put('replenishmentItems', new JSONArray()
                .put(new JSONObject().put('quantity', 1))).toString(),
                error(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'the picklist of a bin without an inventory level answers 500')
    void 'the picklist of an item can be read, cleared, picked again, and changed by hand'() {
        given:
        String id = created().getString('data.id')
        String orderItemId = itemOf(replenishmentApi.read(id, false, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()).id

        when:
        JsonPath picklist = replenishmentApi.getPicklist(orderItemId, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then: 'the pick, and the bins it could come from'
        picklist.getList('data.picklistItems', Map)*.quantityPicked == [10]
        picklist.getList('data.availableItems', Map)*.'binLocation.id'.contains(bulkBin.id)

        when: 'cleared'
        replenishmentApi.deletePicklist(orderItemId, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        itemOf(replenishmentApi.read(id, false, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()).picklistItems == []

        when: 'picked again from the suggestion'
        replenishmentApi.createPicklistItem(orderItemId, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        picked(id) == [10]

        when: 'changed by hand to pick 6, as the edit pick dialog sends it (text quantities, empty ids)'
        replenishmentApi.updatePicklist(orderItemId, new JSONObject().put('picklistItems', new JSONArray()
                .put(new JSONObject()
                        .put('id', '')
                        .put('inventoryItem.id', lot().id)
                        .put('binLocation.id', bulkBin.id)
                        .put('quantityPicked', '6'))).toString(), responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        picked(id) == [6]

        when: 'the whole replenishment is picked again'
        replenishmentApi.createPicklist(id, responseSpecUtil.CREATED_RESPONSE_SPEC)

        then:
        picked(id) == [10]
    }

    @PendingFeature(reason = 'a picklist update without items answers 500, not 400')
    void 'a picklist change by hand needs picklist items'() {
        given:
        String id = created().getString('data.id')
        String orderItemId = itemOf(replenishmentApi.read(id, false, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()).id

        expect:
        replenishmentApi.updatePicklist(orderItemId, '{}',
                error(HttpStatus.SC_BAD_REQUEST))
    }

    void 'approving and then completing a replenishment moves the stock into the picking bin'() {
        given:
        String id = created().getString('data.id')

        when:
        replenishmentApi.update(id, Method.POST, false, '{"status": "APPROVED"}', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        replenishmentApi.read(id, false, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.status', Matchers.equalTo('APPROVED')).build())

        when:
        replenishmentApi.update(id, Method.PUT, false, '{"status": "COMPLETED"}', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        onHand(pickBin) == 10
        onHand(bulkBin) == 10
        replenishmentApi.read(id, true, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.status', Matchers.equalTo('COMPLETED')).build())
    }

    void 'the update also answers at the trailing-slash path, with POST or PUT'() {
        given:
        String id = created().getString('data.id')

        expect:
        replenishmentApi.update(id, method, true, '{"status": "PENDING"}', responseSpecUtil.OK_RESPONSE_SPEC)

        where:
        method << [Method.POST, Method.PUT]
    }

    void 'an item can be removed from a replenishment'() {
        given:
        String id = created().getString('data.id')
        String orderItemId = itemOf(replenishmentApi.read(id, false, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()).id

        when:
        replenishmentApi.removeItem(orderItemId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        replenishmentApi.read(id, false, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.replenishmentItems', Matchers.empty()).build())
    }

    void 'the status options are the inventory level statuses a replenishment can start from'() {
        expect:
        replenishmentApi.statusOptions(new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems('BELOW_MINIMUM', 'BELOW_MAXIMUM'))
                .build())
    }

    @PendingFeature(reason = 'unknown replenishments answer 500, not 404')
    void 'unknown replenishments and items answer 404'() {
        expect:
        replenishmentApi.read(INVALID_ID, false, error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.read(INVALID_ID, true, error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.update(INVALID_ID, Method.PUT, false, '{"status": "APPROVED"}',
                error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.removeItem(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.getPicklist(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.createPicklist(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.updatePicklist(INVALID_ID, '{"picklistItems": [{"quantityPicked": 1}]}',
                error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.deletePicklist(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        replenishmentApi.createPicklistItem(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
    }

}
