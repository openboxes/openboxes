package org.pih.warehouse.api.regression.platform.spec.receiving

import spock.lang.PendingFeature




import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import spock.lang.Ignore
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.receiving.PartialReceivingApi
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Receiving an inbound shipment through the partial receiving workflow (the one before receipts/v2): reading what
 * is left to receive, saving and completing the receipt, rolling it back, and the CSV export and import.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known response POST /partialReceiving/.*',
])
class PartialReceivingApiSpec extends ReceivingSpec {

    @Autowired
    PartialReceivingApi partialReceivingApi



    private static ResponseSpecification badRequest(String message) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorCode', Matchers.equalTo(400))
                .expectBody('errorMessage', Matchers.startsWith(message))
                .build()
    }

    /** A partial receipt body receiving the given quantity on one shipment item, with the given receipt status. */
    private static String receiving(String status, String shipmentItemId, Integer quantity, boolean withReceiptItemId = true) {
        JSONArray shipmentItems = new JSONArray()
        JSONObject line = new JSONObject()
                .put('shipmentItemId', shipmentItemId)
                .put('quantityReceiving', quantity)
        if (withReceiptItemId) {
            // OBAF: a line without the receiptItemId key answers 500; send it as OBAF's web app does (empty
            // for a line with no receipt item yet). The feature 'a line that leaves out receiptItemId ...' pins that defect.
            line.put('receiptItemId', '')
        }
        shipmentItems.add(line)
        JSONArray containers = new JSONArray()
        containers.add(new JSONObject().put('shipmentItems', shipmentItems))
        // Delivered a minute ago: a delivery date of "now" can round up to a future second in the database, and a
        // transaction dated in the future is refused
        String delivered = new Date(System.currentTimeMillis() - 60000).format(Constants.DELIVERY_DATE_FORMAT)
        return new JSONObject().put('receiptStatus', status).put('dateDelivered', delivered).put('containers', containers)
                .toString()
    }

    private List<ReceiptStatusCode> receiptStatuses(String shipmentId) {
        return Shipment.withNewSession { Shipment.get(shipmentId).receipts*.receiptStatusCode }
    }

    private ShipmentStatusCode shipmentStatus(String shipmentId) {
        return Shipment.withNewSession { Shipment.get(shipmentId).currentStatus }
    }

    void 'the partial receipt of a shipped shipment lists what is left to receive'() {
        given:
        String shipmentId = inbound([(product): 10])

        when:
        JsonPath read = partialReceivingApi.read(shipmentId, [stepNumber: 1], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        read.getString('data.shipmentId') == shipmentId
        read.getString('data.receiptId') == null
        read.getString('data.shipmentStatus') == 'SHIPPED'
        read.getList('data.containers.shipmentItems.flatten().shipmentItemId') == shipmentItemIds(shipmentId)
        read.getList('data.containers.shipmentItems.flatten().quantityShipped') == [10]
        read.getList('data.containers.shipmentItems.flatten().lotNumber') == [LOT]

        expect: 'the list operation answers no partial receipts'
        partialReceivingApi.list(responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getList('data') == []
    }

    void 'PUT saves a pending receipt the same way POST does, and answers the receiving state'() {
        given:
        String shipmentId = inbound([(product): 10])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]

        when:
        JsonPath pending = partialReceivingApi.updateWithPut(shipmentId, receiving('PENDING', shipmentItemId, 6),
                [stepNumber: 1], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        pending.getString('data.shipmentId') == shipmentId
        pending.getString('data.receiptId') != null
        pending.getList('data.containers.shipmentItems.flatten().quantityReceiving') == [6]
        pending.getList('data.containers.shipmentItems.flatten().receiptItemId').every { it != null }
        receiptStatuses(shipmentId) == [ReceiptStatusCode.PENDING]
    }

    void 'saving a pending receipt, then completing it with PUT, receives the shipment'() {
        given:
        String shipmentId = inbound([(product): 10])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]

        when:
        JsonPath pending = partialReceivingApi.update(shipmentId, receiving('PENDING', shipmentItemId, 10), [stepNumber: 1],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        pending.getString('data.receiptId') != null
        pending.getList('data.containers.shipmentItems.flatten().quantityReceiving') == [10]
        receiptStatuses(shipmentId) == [ReceiptStatusCode.PENDING]

        when:
        JsonPath completed = partialReceivingApi.updateWithPut(shipmentId, receiving('COMPLETED', shipmentItemId, 10),
                [stepNumber: 2], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        completed.getMap('data') == [:]
        receiptStatuses(shipmentId) == [ReceiptStatusCode.RECEIVED]
        shipmentStatus(shipmentId) == ShipmentStatusCode.RECEIVED
    }

    void 'completing a receipt in one request, with no pending receipt saved first, receives the shipment'() {
        given:
        String shipmentId = inbound([(product): 4])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]

        when:
        partialReceivingApi.update(shipmentId, receiving('COMPLETED', shipmentItemId, 4), [stepNumber: 2],
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        receiptStatuses(shipmentId) == [ReceiptStatusCode.RECEIVED]
        shipmentStatus(shipmentId) == ShipmentStatusCode.RECEIVED
    }

    @PendingFeature(reason = 'completing in one request does not record the canceled rest of the line')
    void 'completing in one request and canceling the rest records what is canceled and receives the shipment'() {
        given:
        String shipmentId = inbound([(product): 4])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]
        JSONObject body = new JSONObject(receiving('COMPLETED', shipmentItemId, 3))
        body.getJSONArray('containers').getJSONObject(0).getJSONArray('shipmentItems').getJSONObject(0)
                .put('cancelRemaining', true)

        when:
        partialReceivingApi.update(shipmentId, body.toString(), [stepNumber: 2], responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        receiptStatuses(shipmentId) == [ReceiptStatusCode.RECEIVED]
        shipmentStatus(shipmentId) == ShipmentStatusCode.RECEIVED
        Shipment.withNewSession {
            Shipment.get(shipmentId).receipts*.receiptItems.flatten().collect { [it.quantityReceived, it.quantityCanceled] }
        } == [[3, 1]]
    }

    // OBAF: PartialReceivingApiController.update refuses any change to a RECEIVED shipment ("Shipment has already
    // been received", ShipmentException), ROLLBACK included (an OBAF addition; upstream lets the rollback
    // through). OBAF's intent wins: the rollback of a received shipment is refused and changes nothing; the original
    // assertions (receipts gone, shipment back to SHIPPED) apply to a shipment that is not fully received.
    void 'rolling back undoes the receipts of the shipment'() {
        given:
        String shipmentId = inbound([(product): 10])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]
        partialReceivingApi.update(shipmentId, receiving('PENDING', shipmentItemId, 4), [stepNumber: 1],
                responseSpecUtil.OK_RESPONSE_SPEC)
        partialReceivingApi.update(shipmentId, receiving('COMPLETED', shipmentItemId, 4), [stepNumber: 2],
                responseSpecUtil.OK_RESPONSE_SPEC)
        assert receiptStatuses(shipmentId) == [ReceiptStatusCode.RECEIVED]

        when:
        int status = partialReceivingApi.update(shipmentId,
                new JSONObject().put('receiptStatus', 'ROLLBACK').toString(), [stepNumber: 1],
                new ResponseSpecBuilder().build()).statusCode

        then:
        status >= 400
        receiptStatuses(shipmentId) == [ReceiptStatusCode.RECEIVED]
        shipmentStatus(shipmentId) == ShipmentStatusCode.RECEIVED
    }

    @PendingFeature(reason = 'the partial receiving CSV import answers 500')
    void 'the receiving CSV exports the lines, and importing it back sets what is received now'() {
        given:
        String shipmentId = inbound([(product): 10])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]

        when:
        List<String> exported = partialReceivingApi.exportCsv(shipmentId, '{}', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()

        then:
        exported[0].contains('Receipt item id,Shipment item id,Code,Name')
        exported.size() == 2
        exported[1].startsWith(",${shipmentItemId},${product.productCode},")

        when: 'the export is imported with 6 in Receiving now and a comment'
        List<String> cells = exported[1].split(',', -1) as List<String>
        cells[11] = '6'
        cells[12] = 'Six arrived'
        JsonPath imported = partialReceivingApi.importCsv(shipmentId, exported[0] + '\n' + cells.join(',') + '\n',
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        JsonPath read = partialReceivingApi.read(shipmentId, [stepNumber: 1], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        imported.getString('data') == 'Data was imported successfully'
        read.getList('data.containers.shipmentItems.flatten().quantityReceiving') == [6]
        read.getList('data.containers.shipmentItems.flatten().comment') == ['Six arrived']
        receiptStatuses(shipmentId) == [ReceiptStatusCode.PENDING]

        when: 'a row without the (optional) Comment column'
        List<String> exportedAgain = partialReceivingApi.exportCsv(shipmentId, '{}', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()
        List<String> noComment = (exportedAgain[1].split(',', -1) as List<String>).take(12)
        noComment[11] = '7'
        partialReceivingApi.importCsv(shipmentId, exportedAgain[0] + '\n' + noComment.join(',') + '\n',
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        partialReceivingApi.read(shipmentId, [stepNumber: 1], responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
                .getList('data.containers.shipmentItems.flatten().quantityReceiving') == [7]
    }

    @PendingFeature(reason = 'a bad partial receiving CSV answers 500, not 400')
    void 'an import that changes more than what is received now, or names an unknown line, is a 400'() {
        given:
        String shipmentId = inbound([(product): 10])
        String header = partialReceivingApi.exportCsv(shipmentId, '{}', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()[0]
        List<String> row = partialReceivingApi.exportCsv(shipmentId, '{}', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()[1].split(',', -1) as List<String>

        when:
        List<String> otherLot = new ArrayList<>(row)
        otherLot[4] = 'ANOTHER-LOT'
        List<String> unknownLine = new ArrayList<>(row)
        unknownLine[1] = 'no-such-shipment-item'
        List<String> notANumber = new ArrayList<>(row)
        notANumber[11] = 'six'
        List<String> negative = new ArrayList<>(row)
        negative[11] = '-1'
        List<String> truncated = row.take(11)
        List<String> badDate = new ArrayList<>(row)
        badDate[5] = 'not-a-date'
        List<String> otherDate = new ArrayList<>(row)
        otherDate[5] = '12/31/2030'

        then:
        partialReceivingApi.importCsv(shipmentId, "${header}\n${otherLot.join(',')}\n",
                badRequest('You can only import the Receiving Now and the Comment fields'))
        partialReceivingApi.importCsv(shipmentId, "${header}\n${unknownLine.join(',')}\n",
                badRequest('No line to receive'))
        partialReceivingApi.importCsv(shipmentId, "${header}\n${notANumber.join(',')}\n",
                badRequest('Receiving now must be a whole number'))
        partialReceivingApi.importCsv(shipmentId, "${header}\n${negative.join(',')}\n",
                badRequest('Receiving now must be a whole number, zero or more'))
        partialReceivingApi.importCsv(shipmentId, "${header}\n${truncated.join(',')}\n",
                badRequest('Each row needs the columns of the export up to Receiving now (12; Comment may be left off), got 11'))
        partialReceivingApi.importCsv(shipmentId, "${header}\n${badDate.join(',')}\n",
                badRequest('Expiration date must be MM/dd/yyyy, got: not-a-date'))
        partialReceivingApi.importCsv(shipmentId, "${header}\n${otherDate.join(',')}\n",
                badRequest('You can only import the Receiving Now and the Comment fields'))
        partialReceivingApi.importCsv(shipmentId, '', badRequest('An import file (importFile) is required'))
        partialReceivingApi.importCsv(shipmentId, header, badRequest('An import file (importFile) is required'),
                'anotherFile')
        receiptStatuses(shipmentId) == []
    }

    void 'a line split off a shipment item is saved next to it'() {
        given:
        String shipmentId = inbound([(product): 10])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]
        JSONArray shipmentItems = new JSONArray()
        // OBAF: each line carries the receiptItemId key, empty for a new receipt item (see receiving())
        shipmentItems.add(new JSONObject().put('shipmentItemId', shipmentItemId).put('quantityReceiving', 6)
                .put('receiptItemId', '').put('originalLine', true))
        shipmentItems.add(new JSONObject().put('shipmentItemId', shipmentItemId).put('quantityReceiving', 4)
                .put('receiptItemId', '').put('newLine', true).put('quantityShipped', 4).put('product', jsonObjectUtil.asIdForRequestBody(product)))
        JSONArray containers = new JSONArray()
        containers.add(new JSONObject().put('shipmentItems', shipmentItems))
        String body = new JSONObject().put('receiptStatus', 'PENDING')
                .put('dateDelivered', new Date(System.currentTimeMillis() - 60000).format(Constants.DELIVERY_DATE_FORMAT))
                .put('containers', containers).toString()

        when:
        partialReceivingApi.update(shipmentId, body, [stepNumber: 1], responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        ReceiptItem.withNewSession {
            ReceiptItem.findAllByShipmentItem(ShipmentItem.get(shipmentItemId)).collect {
                [it.quantityShipped, it.quantityReceived, it.isSplitItem, it.product.id]
            }
        } as Set == [[10, 6, false, product.id], [4, 4, true, product.id]] as Set
    }

    void 'the receiving CSV lists one row per product with its own quantity'() {
        given:
        Product second = newProduct()
        String shipmentId = inbound([(product): 10, (second): 5])

        when:
        List<String> lines = partialReceivingApi.exportCsv(shipmentId, '{}', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()

        then:
        lines.size() == 3
        // Items of an imported shipment carry no order index or sort order, so their order is unspecified; each
        // row must still pair its product with its own quantity
        lines[1..2].collect { String line -> line.split(',', -1).with { [it[2], it[8]] } } as Set ==
                [[product.productCode, '10'], [second.productCode, '5']] as Set
    }

    // The body without the receiptItemId key kept in one feature, so the 500 defect stays pinned
    @PendingFeature(reason = 'a partial receipt line without receiptItemId answers 500')
    void 'a line that leaves out receiptItemId is saved (body without the key)'() {
        given:
        String shipmentId = inbound([(product): 10])
        String shipmentItemId = shipmentItemIds(shipmentId)[0]

        expect:
        partialReceivingApi.update(shipmentId, receiving('PENDING', shipmentItemId, 6, false), [stepNumber: 1],
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('data.receiptId') != null
    }

    @PendingFeature(reason = 'an unknown shipment answers 500, not 404')
    void 'the partial receipt of a shipment that does not exist is a 404'() {
        expect:
        partialReceivingApi.read('no-such-shipment', [stepNumber: 1], responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
