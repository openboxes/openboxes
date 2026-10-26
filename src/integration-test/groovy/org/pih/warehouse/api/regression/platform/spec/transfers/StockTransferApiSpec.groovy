package org.pih.warehouse.api.regression.platform.spec.transfers

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import groovy.json.JsonOutput
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

/**
 * Stock transfers between bins of the main facility: the candidates are what the bins hold; a transfer is created
 * from candidates, approved with destination bins, and completed, which moves the stock.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class StockTransferApiSpec extends TransfersSpec {

    @Override
    void setupData() {
        super.setupData()
        stock(bulkBin, 20)
    }

    /** A stock transfer item as the web app sends it: nested references, from the bulk bin to the given bin. */
    private JSONObject transferItem(int quantity, String destinationBinId = pickBin.id, String id = null) {
        JSONObject json = new JSONObject()
                .put('product', new JSONObject().put('id', item.id))
                .put('inventoryItem', new JSONObject().put('id', lot().id))
                .put('lotNumber', LOT)
                .put('originBinLocation', new JSONObject().put('id', bulkBin.id))
                .put('destinationBinLocation', new JSONObject().put('id', destinationBinId))
                .put('quantity', quantity)
        if (id) {
            json.put('id', id)
        }
        return json
    }

    private JsonPath created(int quantity = 5) {
        return stockTransferApi.create(new JSONObject()
                .put('stockTransferItems', new JSONArray().put(transferItem(quantity))).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
    }

    private Map line(JsonPath stockTransfer) {
        return stockTransfer.getList('data.stockTransferItems', Map).find { it['product.id'] == item.id }
    }

    void 'the candidates are what the bins of the location hold'() {
        when:
        List<Map> candidates = stockTransferApi.candidates([:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getList('data', Map).findAll { it['product.id'] == item.id }

        then:
        candidates*.'originBinLocation.id' == [bulkBin.id]
        candidates*.quantityOnHand == [20]
        candidates*.lotNumber == [LOT]
    }

    void 'return candidates are filtered by product, bin or lot'() {
        expect:
        stockTransferApi.returnCandidates(new JSONObject().put('locationId', facility.id).put('productId', item.id)
                .toString(), new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data."product.id"', Matchers.everyItem(Matchers.equalTo(item.id)))
                .expectBody('data."originBinLocation.id"', Matchers.hasItem(bulkBin.id))
                .build())
        stockTransferApi.returnCandidates(new JSONObject().put('locationId', facility.id).put('lotNumber', LOT)
                .put('binLocationId', bulkBin.id).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data."product.id"', Matchers.hasItem(item.id))
                .build())
        and: 'with no filter, everything the location holds'
        stockTransferApi.returnCandidates(new JSONObject().put('locationId', facility.id).toString(),
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody("data.findAll { it['product.id'] == '${item.id}' }.'originBinLocation.id'".toString(),
                        Matchers.equalTo([bulkBin.id]))
                .build())
    }

    @PendingFeature(reason = 'an unknown location or unreadable date answers 500, not 400')
    void 'return candidates need a known location and a readable expiration date'() {
        expect:
        stockTransferApi.returnCandidates('{"binLocationId": "x"}', error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.returnCandidates(new JSONObject().put('locationId', INVALID_ID).toString(),
                error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.returnCandidates(new JSONObject().put('locationId', facility.id)
                .put('expirationDate', 'not a date').toString(),
                error(HttpStatus.SC_BAD_REQUEST))
        and: 'a readable date matches the lot expiry (the filters are alternatives; this lot has no expiry)'
        stockTransferApi.returnCandidates(new JSONObject().put('locationId', facility.id)
                .put('expirationDate', '12/31/2099').toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data."product.id"', Matchers.not(Matchers.hasItem(item.id)))
                .build())
    }

    @PendingFeature(reason = 'a create without a chosen location answers 500, not 400')
    void 'without a chosen location, transfers and replenishments ask for one'() {
        given: 'a session logged in without a location'
        String cookie = authApiWrapper.login(new JSONObject().put('username', username).put('password', password),
                HttpStatus.SC_OK).getCookie('JSESSIONID')
        AccessCheckApi noLocation = new AccessCheckApi(new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addCookie('JSESSIONID', cookie)
                .setContentType(ContentType.JSON)
                .setBody('{}')
                .build())

        expect:
        noLocation.call('POST', '/api/stockTransfers', error(HttpStatus.SC_BAD_REQUEST))
        noLocation.call('POST', '/api/replenishments', error(HttpStatus.SC_BAD_REQUEST))
    }

    void 'a stock transfer created from candidates waits to be approved'() {
        when:
        JsonPath stockTransfer = created()

        then:
        stockTransfer.getString('data.status') == 'PENDING'
        stockTransfer.getString('data.stockTransferNumber')
        line(stockTransfer).quantity == 5
        line(stockTransfer).'originBinLocation.id' == bulkBin.id
        line(stockTransfer).quantityOnHand == 20

        when:
        String id = stockTransfer.getString('data.id')

        then: 'the list of the location shows it'
        stockTransferApi.list([location: facility.id], new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(id))
                .expectBody('totalCount', Matchers.greaterThanOrEqualTo(1))
                .build())
        stockTransferApi.read(id, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(id)).build())
    }

    void 'the list is filtered by number, status and last update, as the stock transfer list page sends them'() {
        given:
        JsonPath stockTransfer = created()
        String id = stockTransfer.getString('data.id')
        String number = stockTransfer.getString('data.stockTransferNumber')
        String today = new Date().format('MM/dd/yyyy')

        expect:
        stockTransferApi.list([location: facility.id, q: number, status: 'PENDING', lastUpdatedStartDate: today,
                               sort: 'orderNumber', order: 'asc', max: '5', offset: '0'],
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.equalTo([id]))
                        .expectBody('data[0].orderNumber', Matchers.equalTo(number))
                        .expectBody('data[0].status', Matchers.equalTo('PENDING'))
                        .expectBody('data[0].orderItemsCount', Matchers.equalTo(1))
                        .expectBody('totalCount', Matchers.equalTo(1))
                        .build())
        stockTransferApi.list([location: facility.id, q: number, status: 'COMPLETED'],
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data', Matchers.empty())
                        .expectBody('totalCount', Matchers.equalTo(0))
                        .build())
        stockTransferApi.list([location: facility.id, q: number, lastUpdatedEndDate: '01/01/2020'],
                new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data', Matchers.empty()).build())
    }

    @PendingFeature(reason = 'unreadable list filters answer 500, not 400')
    void 'list filters it cannot read are refused'() {
        expect:
        stockTransferApi.list([location: facility.id, lastUpdatedStartDate: '2026-10-01'], error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.list([location: facility.id, lastUpdatedEndDate: '13/45/2026'], error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.list([location: facility.id, status: 'NOT_A_STATUS'], error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.list([location: facility.id, status: 'PLACED'], error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.list([location: facility.id, max: 'ten'], error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.list([location: facility.id, offset: '-1'], error(HttpStatus.SC_BAD_REQUEST))
        stockTransferApi.list([location: facility.id, sort: 'noSuchField'], error(HttpStatus.SC_BAD_REQUEST))
    }

    /** What the web app does with a response before sending it back: dotted keys become nested objects. */
    private static Object nested(Object value) {
        if (value instanceof List) {
            return value.collect { nested(it) }
        }
        if (!(value instanceof Map)) {
            return value
        }
        Map result = [:]
        value.each { String key, Object item ->
            List<String> parts = key.tokenize('.')
            Map target = result
            parts.init().each { String part -> target = (Map) target.computeIfAbsent(part) { [:] } }
            target[parts.last()] = nested(item)
        }
        return result
    }

    void 'the web app creates a transfer from candidates as listed and approves it with the stock transfer as read'() {
        given: 'a candidate as the create page holds it, sent to the pick bin'
        Map candidate = (Map) nested(stockTransferApi.candidates([:], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getList('data', Map).find { it['product.id'] == item.id })
        candidate.destinationBinLocation = [id: pickBin.id, name: pickBin.name]
        candidate.quantity = 5

        when:
        JsonPath stockTransfer = stockTransferApi.create(JsonOutput.toJson([stockTransferItems: [candidate]]),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        String id = stockTransfer.getString('data.id')

        then:
        line(stockTransfer).quantity == 5
        line(stockTransfer).'destinationBinLocation.id' == pickBin.id

        when: 'the second page sends the read back with status APPROVED'
        Map read = (Map) nested(stockTransferApi.read(id, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getMap('data'))
        read.status = 'APPROVED'
        JsonPath approved = stockTransferApi.update(id, Method.PUT, JsonOutput.toJson(read),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        approved.getString('data.status') == 'APPROVED'
        line(approved).quantity == 5
        line(approved).'destinationBinLocation.id' == pickBin.id
    }

    void 'items can be removed one by one or all at once'() {
        given:
        JsonPath stockTransfer = created()
        String id = stockTransfer.getString('data.id')

        when:
        stockTransferApi.removeItem(line(stockTransfer).id, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.stockTransferItems', Matchers.empty()).build())

        then:
        stockTransferApi.read(id, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.stockTransferItems', Matchers.empty()).build())

        when:
        String other = created(3).getString('data.id')

        then:
        stockTransferApi.removeAllItems(other, new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.stockTransferItems', Matchers.empty()).build())
    }

    void 'the status options are the stock transfer statuses'() {
        expect:
        stockTransferApi.statusOptions(new ResponseSpecBuilder().expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems('PENDING', 'COMPLETED'))
                .build())
    }

    @PendingFeature(reason = 'unknown stock transfers answer 500, not 404')
    void 'unknown stock transfers and items answer 404'() {
        expect:
        stockTransferApi.read(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.update(INVALID_ID, Method.PUT, '{"status": "APPROVED"}',
                error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.delete(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.removeItem(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.removeAllItems(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.sendShipment(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
        stockTransferApi.rollback(INVALID_ID, error(HttpStatus.SC_NOT_FOUND))
    }
}
