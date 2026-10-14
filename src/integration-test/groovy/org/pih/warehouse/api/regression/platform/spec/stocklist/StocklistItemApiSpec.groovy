package org.pih.warehouse.api.regression.platform.spec.stocklist

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers


// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class StocklistItemApiSpec extends StocklistSpec {

    void 'a line is added to a stock list, read back, and listed for its product once the list is published'() {
        given:
        String name = uniqueName('Lines stock list')
        String stocklistId = newStocklist(name)

        when:
        String lineId = newLine(stocklistId, 15)

        then:
        stocklistItemApi.read(lineId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data."requisitionItem.id"', Matchers.equalTo(lineId))
                .expectBody('data.stocklistId', Matchers.equalTo(stocklistId))
                .expectBody('data.name', Matchers.equalTo(name))
                .expectBody('data.maxQuantity', Matchers.equalTo(15))
                .build())

        and: 'the product lists only the lines of published stock lists'
        stocklistItemApi.list(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.stocklistId', Matchers.not(Matchers.hasItem(stocklistId)))
                .build())

        when:
        stocklistApi.action(stocklistId, 'publish', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        stocklistItemApi.list(product.id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.stocklistId', Matchers.hasItem(stocklistId))
                .build())
    }

    void 'updating a line with #method changes its maximum quantity'() {
        given:
        String lineId = newLine(newStocklist(), 5)

        expect:
        stocklistItemApi.update(lineId, new JSONObject().put('maxQuantity', 25).toString(), method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.maxQuantity', Matchers.equalTo(25))
                .build())

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'a line is removed'() {
        given:
        String lineId = newLine(newStocklist())

        when:
        stocklistItemApi.remove(lineId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        stocklistItemApi.read(lineId, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'the stock lists a line can be added to are the published ones'() {
        given:
        String published = newStocklist()
        stocklistApi.action(published, 'publish', responseSpecUtil.OK_RESPONSE_SPEC)
        String draft = newStocklist()

        expect:
        stocklistItemApi.availableStocklists(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(published))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(draft)))
                .build())
    }

    @PendingFeature(reason = 'listing lines without product.id answers 500, not 400')
    void 'the lines of a product need product.id: 400 without it'() {
        expect:
        stocklistItemApi.list(null, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'a line without or with an unknown product/stock list answers 500, not 400')
    void 'adding a line is refused with 400 without a product, for an unknown product or an unknown stock list'() {
        given:
        String stocklistId = newStocklist()
        String body = new JSONObject().put('stocklistId', stocklistId).put('maxQuantity', 1).toString()
        String unknownList = new JSONObject().put('stocklistId', INVALID_ID).put('maxQuantity', 1).toString()

        expect:
        stocklistItemApi.create(null, body, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        stocklistItemApi.create(INVALID_ID, body, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        stocklistItemApi.create(product.id, unknownList, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'updating an unknown line answers 500, not 404')
    void 'a line that does not exist answers 404 to read, update and remove'() {
        expect:
        stocklistItemApi.read(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        stocklistItemApi.update(INVALID_ID, '{"maxQuantity": 1}', Method.PUT, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        stocklistItemApi.remove(INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
