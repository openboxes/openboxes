package org.pih.warehouse.api.regression.platform.spec.stocklist

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.Method
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.requisition.Requisition

// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class StocklistApiSpec extends StocklistSpec {

    void 'a stock list is created, read back, and listed by its destination but not by another one'() {
        given:
        String name = uniqueName('Ward stock list')
        String elsewhere = newStocklist(uniqueName('Other destination'), facility.id, facility.id)

        when:
        String id = newStocklist(name)

        then:
        stocklistApi.read(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data."requisition.id"', Matchers.equalTo(id))
                .expectBody('data.name', Matchers.equalTo(name))
                .expectBody('data."origin.id"', Matchers.equalTo(facility.id))
                .expectBody('data."destination.id"', Matchers.equalTo(ward.id))
                .build())
        stocklistApi.list([destination: ward.id, isPublished: false, max: 50], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(id))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(elsewhere)))
                .expectBody('totalCount', Matchers.greaterThanOrEqualTo(1))
                .build())
    }

    void 'the list holds published stock lists by default, drafts with includeUnpublished, and filters by origin'() {
        given:
        String published = newStocklist(uniqueName('Published'), facility.id, ward.id)
        stocklistApi.action(published, 'publish', responseSpecUtil.OK_RESPONSE_SPEC)
        String draft = newStocklist(uniqueName('Draft'), facility.id, ward.id)
        String otherOrigin = newStocklist(uniqueName('Other origin'), ward.id, facility.id)
        stocklistApi.action(otherOrigin, 'publish', responseSpecUtil.OK_RESPONSE_SPEC)

        expect:
        stocklistApi.list([origin: facility.id, destination: ward.id, max: 500], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(published))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(draft)))
                .expectBody('data.id', Matchers.not(Matchers.hasItem(otherOrigin)))
                .build())
        stocklistApi.list([origin: facility.id, destination: ward.id, includeUnpublished: true, max: 500],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.hasItems(published, draft))
                        .build())
    }

    void 'the list downloads as CSV with a column per stock list and a row per product'() {
        given:
        String name = uniqueName('CSV stock list')
        String id = newStocklist(name)
        newLine(id, 12)

        when:
        Response csv = stocklistApi.list([destination: ward.id, isPublished: false, format: 'csv'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType('text/csv')
                .build())

        then:
        csv.asString().contains(name)
        csv.asString().readLines().any { it.startsWith("${product.productCode},") }
    }

    @PendingFeature(reason = 'a partial stock list update wipes the name (400)')
    void 'updating a stock list with #method changes the given fields and keeps the others'() {
        given:
        String name = uniqueName('Before update')
        String id = newStocklist(name)
        String body = new JSONObject().put('destination', new JSONObject().put('id', facility.id)).toString()

        expect: 'the name was not sent, and stays'
        stocklistApi.update(id, body, method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data."destination.id"', Matchers.equalTo(facility.id))
                .expectBody('data.name', Matchers.equalTo(name))
                .build())

        where:
        method << [Method.PUT, Method.POST]
    }

    @PendingFeature(reason = 'updating an unknown stock list answers 500')
    void 'updating a stock list that does not exist answers 404'() {
        expect:
        stocklistApi.update(INVALID_ID, '{"name": "x"}', Method.PUT, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'a stock list without a name is refused with 400'() {
        expect:
        stocklistApi.create(new JSONObject()
                .put('origin', new JSONObject().put('id', facility.id))
                .put('destination', new JSONObject().put('id', ward.id))
                .put('requestedBy', new JSONObject().put('id', requestedById))
                .toString(), responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'a stock list is published and unpublished'() {
        given:
        String id = newStocklist()

        when:
        stocklistApi.action(id, 'publish', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Requisition.withNewTransaction { Requisition.get(id).isPublished }

        when:
        stocklistApi.action(id, 'unpublish', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        !Requisition.withNewTransaction { Requisition.get(id).isPublished }
    }

    void 'clearing a stock list removes its lines'() {
        given:
        String id = newStocklist()
        newLine(id)

        when:
        stocklistApi.action(id, 'clear', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Requisition.withNewTransaction { Requisition.get(id).requisitionItems.size() } == 0
    }

    void 'cloning a stock list makes an unpublished copy with the same lines'() {
        given:
        String name = uniqueName('Original')
        String id = newStocklist(name)
        newLine(id, 7)

        when:
        stocklistApi.action(id, 'clone', responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Requisition.withNewTransaction {
            Requisition copy = Requisition.findByNameAndIsTemplate("Copy of ${name}".toString(), true)
            copy && !copy.isPublished && copy.requisitionItems*.quantity == [7]
        }
    }

    void 'a stock list exports as CSV, an empty one with an empty row'() {
        given:
        String withLine = newStocklist()
        newLine(withLine, 9)
        String empty = newStocklist()

        when:
        Response csv = stocklistApi.export(withLine, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType('text/csv')
                .expectHeader('Content-Disposition', Matchers.containsString(ward.name))
                .build())

        then:
        csv.asString().readLines()[0].contains('Product Code')
        csv.asString().contains(product.productCode)
        csv.asString().contains('Unit cost')  // the test admin has the finance role

        when:
        Response emptyCsv = stocklistApi.export(empty, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        emptyCsv.asString().readLines().size() == 2
    }

    void 'a stock list is sent by mail with the spreadsheet and PDF attached (mail is disabled in tests)'() {
        given:
        String id = newStocklist()
        newLine(id)

        expect:
        stocklistApi.sendMail(id, new JSONObject()
                .put('subject', 'Your stock list')
                .put('text', 'See attached')
                .put('recipients', new JSONArray(['ward.manager@example.org']))
                .put('includePdf', true)
                .put('includeXls', true)
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC)
    }

    void 'deleting a stock list removes it with its lines'() {
        given:
        String id = newStocklist()
        newLine(id)

        when:
        stocklistApi.delete(id, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        stocklistApi.read(id, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'a conflicting delete answers 400 without the error shape')
    void 'deleting a stock list that a request was made from answers 409'() {
        given:
        String id = newStocklist()
        Requisition.withNewTransaction {
            Requisition template = Requisition.get(id)
            new Requisition(name: uniqueName('From template'), origin: template.origin, destination: template.destination,
                    requestedBy: template.requestedBy, requisitionTemplate: template).save(failOnError: true, flush: true)
        }

        expect:
        stocklistApi.delete(id, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CONFLICT)
                .expectBody('errorCode', Matchers.equalTo(HttpStatus.SC_CONFLICT))
                .build())
    }

    private Response callOperation(String operation, String id, ResponseSpecification responseSpec) {
        switch (operation) {
            case 'read': return stocklistApi.read(id, responseSpec)
            case 'delete': return stocklistApi.delete(id, responseSpec)
            case 'export': return stocklistApi.export(id, responseSpec)
            case 'sendMail': return stocklistApi.sendMail(id, '{"recipients": ["a@example.org"]}', responseSpec)
            default: return stocklistApi.action(id, operation, responseSpec)
        }
    }
}
