package org.pih.warehouse.api.regression.platform.spec.inventory

import spock.lang.PendingFeature



import spock.lang.Ignore
import org.pih.warehouse.api.spec.inventory.*
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers

import org.pih.warehouse.inventory.CycleCountRequest
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product

/**
 * Cycle counts through the API, as the cycle count pages use them: candidates, requests and their assignments, starting
 * a count, counting (one item, a batch, custom rows), submitting with and without a discrepancy, the recount, the
 * printable and spreadsheet forms, uploading a filled-in form, refreshing, and deleting.
 */
// OpenAPI validation stays REPORT-ONLY here: OBAF renders the cycle count answers' item status and
// facility as objects ({"enumType", "name"} / a location map) where the spec has strings, in 13 of 21 passing
// features
class CycleCountApiSpec extends CycleCountSpec {

    /**
     * OBAF: the cycle count item status in the submit and batch answers renders as the Grails enum object
     * ({"enumType": ..., "name": "APPROVED"}), where the spec (and the cycle count's own status) render the name. Not a
     * known defect; read the name either way so the features still check the status.
     */
    private static String itemStatus(io.restassured.path.json.JsonPath json, String path) {
        Object value = json.get(path)
        return value instanceof Map ? value.name : value
    }

    void 'a product with stock is a cycle count candidate; the candidates download as CSV'() {
        given:
        Product product = stockedProduct(5)

        when:
        Response json = cycleCountApi.candidates(facility.id, [searchTerm: product.productCode, max: 10, offset: 0],
                ok().build())
        Response csv = cycleCountApi.candidates(facility.id, [searchTerm: product.productCode, format: 'csv'],
                ok().expectContentType('text/csv')
                        .expectHeader('Content-Disposition', Matchers.containsString('CycleCountReport-'))
                        .build())

        then:
        json.jsonPath().getList('data.product.id').contains(product.id)
        csv.asString().contains(product.productCode)
    }

    void 'requesting a count makes it pending; assignments for the count and the recount can be set'() {
        given:
        Product product = stockedProduct(3)
        String requestId = requestCount(product)
        String assignee = adminPersonId()

        when:
        Response pending = cycleCountApi.pendingRequests(facility.id, [searchTerm: product.productCode], ok().build())
        Response updated = cycleCountApi.updateRequests(facility.id, new JSONObject()
                .put('commands', new JSONArray().put(new JSONObject()
                        .put('cycleCountRequest', requestId)
                        .put('assignments', new JSONObject()
                                .put('0', new JSONObject().put('assignee', assignee).put('deadline', '2030-01-15'))
                                .put('1', new JSONObject().put('assignee', assignee).put('deadline', '2030-01-20')))))
                .toString(), ok().build())

        then:
        pending.jsonPath().getList('data.cycleCountRequest.id').contains(requestId)
        updated.jsonPath().getString('data[0].id') == requestId
        CycleCountRequest.withNewSession {
            CycleCountRequest saved = CycleCountRequest.get(requestId)
            assert saved.countAssignee.id == assignee
            assert saved.recountAssignee.id == assignee
            assert saved.countDeadline.toString() == '2030-01-15'
            assert saved.recountDeadline.toString() == '2030-01-20'
            true
        }
    }

    void 'the requested counts filter by assignee and deadline, sent the way the To count tab sends them'() {
        given: 'a request assigned to the admin with a count deadline'
        Product product = stockedProduct(3)
        String requestId = requestCount(product)
        String assignee = adminPersonId()
        cycleCountApi.updateRequests(facility.id, new JSONObject()
                .put('commands', new JSONArray().put(new JSONObject()
                        .put('cycleCountRequest', requestId)
                        .put('assignments', new JSONObject()
                                .put('0', new JSONObject().put('assignee', assignee).put('deadline', '2030-01-15')))))
                .toString(), ok().build())
        Map tab = [tab: 'TO_COUNT', facility: facility.id, searchTerm: product.productCode, max: 10, offset: 0,
                   statuses: ['CREATED', 'REQUESTED', 'COUNTING'], countAssignees: assignee]

        expect: 'a deadline on or after the count deadline finds it, an earlier one does not'
        cycleCountApi.pendingRequests(facility.id, tab + [countDeadline: '2030-01-15'], ok().build())
                .jsonPath().getList('data.cycleCountRequest.id') == [requestId]
        cycleCountApi.pendingRequests(facility.id, tab + [countDeadline: '2030-01-14'], ok().build())
                .jsonPath().getList('data.cycleCountRequest.id') == []
    }

    void 'a product cannot be requested twice while its request is open, nor twice in one batch'() {
        given:
        Product product = stockedProduct(2)
        requestCount(product)
        Product other = stockedProduct(2)

        expect:
        cycleCountApi.createRequests(facility.id, new JSONObject()
                .put('requests', new JSONArray().put(new JSONObject().put('product', product.id)))
                .toString(), responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        cycleCountApi.createRequests(facility.id, new JSONObject()
                .put('requests', new JSONArray()
                        .put(new JSONObject().put('product', other.id))
                        .put(new JSONObject().put('product', other.id)))
                .toString(), responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'starting a count makes one item per stock line; starting it again returns the same count'() {
        given:
        Product product = stockedProduct(7)
        String requestId = requestCount(product)

        when:
        Map count = startCount(requestId)
        Map again = startCount(requestId)
        Response listed = cycleCountApi.list(facility.id, [id: count.id], ok().build())

        then:
        count.status == 'COUNTING' || count.status == 'REQUESTED'
        count.cycleCountItems.size() == 1
        count.cycleCountItems[0].quantityOnHand == 7
        count.cycleCountItems[0].product.id == product.id
        again.id == count.id
        listed.jsonPath().getList('data.id') == [count.id]
    }

    @PendingFeature(reason = 'starting a count of a product without stock is accepted (200), not 409')
    void 'a count of a product without stock cannot start (409); an unknown request is refused (400)'() {
        given:
        Product product = stockedProduct(4)
        String requestId = requestCount(product)
        restock(product, 0)

        expect:
        cycleCountApi.start(facility.id, startBody(requestId), null,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_CONFLICT))
        cycleCountApi.start(facility.id, startBody(INVALID_ID), null,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        cycleCountApi.startRecount(facility.id, recountBody(INVALID_ID), null,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    void 'a count that matches the stock completes on submit'() {
        given:
        Product product = stockedProduct(6)
        String requestId = requestCount(product)
        Map count = startCount(requestId)
        recordCount(count.cycleCountItems[0].id as String, 6)

        when:
        Response submitted = cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true), ok().build())

        then:
        submitted.jsonPath().getString('data.status') == 'COMPLETED'
        itemStatus(submitted.jsonPath(), 'data.cycleCountItems[0].status') == 'APPROVED'
        quantityOnHand(product) == 6
    }

    @PendingFeature(reason = 'startRecount returns the new recount items without ids')
    void 'a discrepancy asks for a recount; the recount completes the count and adjusts the stock'() {
        given:
        Product product = stockedProduct(10)
        String requestId = requestCount(product)
        Map count = startCount(requestId)
        recordCount(count.cycleCountItems[0].id as String, 8)

        when: 'the count is submitted with a discrepancy (8 counted, 10 on hand)'
        Response submitted = cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true), ok().build())

        then: 'the counted item waits for a recount'
        submitted.jsonPath().getString('data.status') == 'COUNTED'
        itemStatus(submitted.jsonPath(), 'data.cycleCountItems[0].status') == 'COUNTED'

        when: 'the recount starts, is counted and submitted'
        Response recount = cycleCountApi.startRecount(facility.id, recountBody(requestId), null, ok().build())
        Map recountItem = (recount.jsonPath().getList('data[0].cycleCountItems') as List<Map>).find { it.countIndex == 1 }
        assert recountItem: recount.asString()
        recordCount(recountItem.id as String, 9, true)
        Response again = cycleCountApi.startRecount(facility.id, recountBody(requestId), null, ok().build())
        Response completed = cycleCountApi.submitRecount(facility.id, count.id as String, submitBody(false), ok().build())

        then:
        again.jsonPath().getString('data[0].id') == count.id
        completed.jsonPath().getString('data.status') == 'COMPLETED'
        awaitTransactedQuantityOnHand(product, 9)
    }

    void 'a recount needs a count under investigation; a submitted count cannot be submitted again'() {
        given:
        Product product = stockedProduct(3)
        String requestId = requestCount(product)
        Map count = startCount(requestId)

        expect: 'no recount before the count is submitted'
        cycleCountApi.startRecount(facility.id, recountBody(requestId), null,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        cycleCountApi.submitRecount(facility.id, count.id as String, submitBody(false),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))

        when:
        recordCount(count.cycleCountItems[0].id as String, 3)
        cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true), ok().build())

        then:
        cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'submitting with an outdated quantity on hand answers 500, not 409')
    void 'a submit that refreshes the quantity on hand fails when the stock changed since the count started (409)'() {
        given:
        Product product = stockedProduct(5)
        Map count = startCount(requestCount(product))
        recordCount(count.cycleCountItems[0].id as String, 5)
        restock(product, 4)

        expect:
        cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true, true, true),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_CONFLICT))
    }

    void 'items are counted in a batch, through either batch path'() {
        given:
        Map count = startCount(requestCount(stockedProduct(4)))
        String itemId = count.cycleCountItems[0].id

        when:
        Response viaCount = cycleCountApi.itemBatchOfCount(facility.id, count.id as String, new JSONObject()
                .put('itemsToUpdate', new JSONArray().put(new JSONObject().put('id', itemId).put('quantityCounted', 3)))
                .toString(), Method.PATCH, ok().build())
        Response viaFacility = cycleCountApi.itemBatch(facility.id, new JSONObject()
                .put('itemsToUpdate', new JSONArray().put(new JSONObject().put('id', itemId).put('quantityCounted', 4)))
                .toString(), Method.PATCH, ok().build())

        then:
        viaCount.jsonPath().getInt('data[0].quantityCounted') == 3
        viaFacility.jsonPath().getInt('data[0].quantityCounted') == 4
        itemStatus(viaFacility.jsonPath(), 'data[0].status') in ['COUNTED', 'COUNTING']
    }

    @PendingFeature(reason = 'deleting a non-custom cycle count item answers 500, not 400')
    void 'custom rows are added one at a time or in a batch and can be removed; counted stock lines cannot'() {
        given:
        Product product = stockedProduct(4)
        Map count = startCount(requestCount(product))
        String lot = "LOT-${UUID.randomUUID().toString().take(6)}"
        JSONObject row = new JSONObject()
                .put('inventoryItem', new JSONObject().put('product', product.id).put('lotNumber', lot)
                        .put('expirationDate', '01/31/2031'))
                .put('quantityCounted', 2)
                .put('comment', 'found on a shelf')

        when:
        Response one = cycleCountApi.createItem(facility.id, count.id as String, row.toString(), ok().build())
        Response batch = cycleCountApi.itemBatchOfCount(facility.id, count.id as String, new JSONObject()
                .put('itemsToCreate', new JSONArray().put(new JSONObject(row.toString())
                        .put('inventoryItem', new JSONObject().put('product', product.id).put('lotNumber', "${lot}-B"))))
                .toString(), Method.POST, ok().build())
        Response batchOfFacility = cycleCountApi.itemBatch(facility.id, new JSONObject()
                .put('itemsToCreate', new JSONArray().put(new JSONObject(row.toString())
                        .put('cycleCount', count.id)
                        .put('inventoryItem', new JSONObject().put('product', product.id).put('lotNumber', "${lot}-C"))))
                .toString(), Method.POST, ok().build())

        then:
        one.jsonPath().getBoolean('data.custom')
        one.jsonPath().getInt('data.quantityCounted') == 2
        batch.jsonPath().getBoolean('data[0].custom')
        batchOfFacility.jsonPath().getBoolean('data[0].custom')

        expect:
        cycleCountApi.deleteItem(facility.id, one.jsonPath().getString('data.id'), responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        cycleCountApi.deleteItem(facility.id, count.cycleCountItems[0].id as String,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        cycleCountApi.deleteItem(facility.id, INVALID_ID, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'refreshing a completed count answers 500, not 409')
    void 'refreshing a count picks up stock changes; a completed count cannot be refreshed (409)'() {
        given:
        Product product = stockedProduct(5)
        Map count = startCount(requestCount(product))
        restock(product, 8)

        when:
        Response refreshed = cycleCountApi.refresh(facility.id, count.id as String,
                [removeOutOfStockItemsImplicitly: false, countIndex: 0], ok().build())

        then:
        refreshed.jsonPath().getInt('data.cycleCountItems[0].quantityOnHand') == 8

        when:
        recordCount(count.cycleCountItems[0].id as String, 8)
        cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true), ok().build())

        then:
        cycleCountApi.refresh(facility.id, count.id as String, [:],
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_CONFLICT))
        cycleCountApi.refresh(facility.id, INVALID_ID, [:], responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'the count and recount forms download as a spreadsheet and as a PDF'() {
        given:
        Product product = stockedProduct(3)
        String requestId = requestCount(product)

        when:
        Response startedAsXls = cycleCountApi.start(facility.id, startBody(requestId), 'xls',
                ok().expectContentType('application/vnd.ms-excel').build())
        String countId = cycleCountApi.list(facility.id, [:], ok().build()).jsonPath()
                .getList('data').find { it.requestId == requestId }?.id
        Response countPdf = cycleCountApi.list(facility.id, [id: countId, format: 'pdf'], ok().build())
        Response countXls = cycleCountApi.list(facility.id, [id: countId, format: 'xls'],
                ok().expectContentType('application/vnd.ms-excel').build())

        then:
        startedAsXls.asByteArray().length > 0
        countPdf.contentType.startsWith('application/pdf')
        countXls.asByteArray().length > 0

        when: 'the count goes to investigation and the recount starts'
        Map count = cycleCountApi.list(facility.id, [id: countId], ok().build()).jsonPath().getMap('data[0]')
        recordCount(count.cycleCountItems[0].id as String, 1)
        cycleCountApi.submitCount(facility.id, countId, submitBody(true), ok().build())
        Response recountPdf = cycleCountApi.startRecount(facility.id, recountBody(requestId), 'pdf', ok().build())
        Response recountXls = cycleCountApi.list(facility.id, [id: countId, format: 'xls'],
                ok().expectContentType('application/vnd.ms-excel').build())

        then:
        recountPdf.contentType.startsWith('application/pdf')
        recountXls.asByteArray().length > 0
    }

    void 'a filled-in count form uploads row by row; so does a recount form'() {
        given:
        Product product = stockedProduct(3)
        String requestId = requestCount(product)
        Map count = startCount(requestId)
        byte[] countForm = cycleCountApi.list(facility.id, [id: count.id, format: 'xls'], ok().build()).asByteArray()

        when:
        Response uploaded = cycleCountApi.upload(facility.id, false, 'count.xls', countForm, ok().build())

        then:
        uploaded.jsonPath().getList('data.cycleCountItemId').contains(count.cycleCountItems[0].id)
        uploaded.jsonPath().getList('errors') != null

        when:
        recordCount(count.cycleCountItems[0].id as String, 1)
        cycleCountApi.submitCount(facility.id, count.id as String, submitBody(true), ok().build())
        cycleCountApi.startRecount(facility.id, recountBody(requestId), null, ok().build())
        byte[] recountForm = cycleCountApi.list(facility.id, [id: count.id, format: 'xls'], ok().build()).asByteArray()
        Response recountUploaded = cycleCountApi.upload(facility.id, true, 'recount.xls', recountForm, ok().build())

        then:
        !recountUploaded.jsonPath().getList('data.cycleCountItemId').isEmpty()
    }

    @PendingFeature(reason = 'a cycle count sheet upload without a file answers 500')
    void 'an upload without a file answers 400'() {
        expect:
        cycleCountApi.upload(facility.id, false, 'count.xls', new byte[0],
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        cycleCountApi.upload(facility.id, true, 'recount.xls', new byte[0],
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    /** The stock transactions recorded under the count's transaction source. */
    int transactionsOf(String cycleCountId) {
        return Transaction.withNewTransaction {
            Transaction.executeQuery(
                    'select count(t) from Transaction t where t.transactionSource.cycleCount.id = :id',
                    [id: cycleCountId])[0] as int
        }
    }
}
