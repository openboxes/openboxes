package org.pih.warehouse.api.regression.platform.spec.fulfillment

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.response.Response
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.fulfillment.PickPackFileApi
import org.pih.warehouse.api.regression.platform.client.fulfillment.PicklistApi
import org.pih.warehouse.api.regression.platform.spec.stockmovement.StockMovementSpec
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

/**
 * The pick and pack spreadsheets of an outbound stock movement: templates and exports as CSV or Excel, and the CSV
 * imports that pick and pack from them.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class PickPackFileApiSpec extends StockMovementSpec {

    @Autowired
    PickPackFileApi pickPackFileApi

    @Autowired
    PicklistApi picklistApi

    private static final ResponseSpecification UNSUPPORTED_FORMAT = new ResponseSpecBuilder()
            .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
            .expectBody('errorCode', Matchers.equalTo(400))
            .expectBody('errorMessage', Matchers.equalTo('Unsupported format: pdf (use csv or xls)'))
            .build()

    /** The first bytes of an Excel 97-2003 (OLE2) file. */
    private static final List<Integer> OLE2_SIGNATURE = [0xD0, 0xCF, 0x11, 0xE0]

    private static boolean isExcel(Response response) {
        byte[] bytes = response.body.asByteArray()
        return response.contentType.startsWith('application/vnd.ms-excel') &&
                bytes.length > 4 && bytes[0..3].collect { it & 0xFF } == OLE2_SIGNATURE
    }

    private List<Integer> pickedQuantities(String stockMovementId) {
        return Requisition.withNewSession {
            Requisition.get(stockMovementId).picklist.picklistItems*.quantity*.intValue()
        }
    }

    private String picklistId(String stockMovementId) {
        return Requisition.withNewSession { Requisition.get(stockMovementId).picklist.id }
    }

    private String shipmentItemId(String stockMovementId) {
        return Shipment.withNewSession {
            Shipment.findByRequisition(Requisition.get(stockMovementId)).shipmentItems.first().id
        }
    }

    @PendingFeature(reason = 'an unsupported export format answers 500, not 400')
    void 'the pick template lists each requested line, as CSV by default or as Excel'() {
        given:
        stockUp(20)
        String id = picking([(product): 5])

        when:
        List<String> csv = pickPackFileApi.exportPicklistTemplate(id, null, responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()

        then:
        csv.size() == 2
        csv[0].split(',').size() == 7
        csv[1].startsWith("${firstItemId(id)},${product.productCode},")
        csv[1].endsWith(',5')

        expect:
        isExcel(pickPackFileApi.exportPicklistTemplate(id, 'xls', responseSpecUtil.OK_RESPONSE_SPEC))
        pickPackFileApi.exportPicklistTemplate(id, 'pdf', UNSUPPORTED_FORMAT)
    }

    @PendingFeature(reason = 'an unsupported export format answers 500, not 400')
    void 'the pick export lists the picks, or the unpicked line with quantity 0'() {
        given:
        stockUp(20)
        String picked = picking([(product): 5])
        String unpicked = requested([(product): 4])

        when:
        List<String> pickedCsv = pickPackFileApi.exportPicklistItems(picked, 'csv', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()
        List<String> unpickedCsv = pickPackFileApi.exportPicklistItems(unpicked, null, responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()

        then:
        pickedCsv.size() == 2
        pickedCsv[1].startsWith("${firstItemId(picked)},${product.productCode},")
        pickedCsv[1].contains(',SM-LOT-1,')
        pickedCsv[1].endsWith(',5')
        unpickedCsv.size() == 2
        unpickedCsv[1].startsWith("${firstItemId(unpicked)},${product.productCode},")
        unpickedCsv[1].endsWith(',0')

        expect:
        isExcel(pickPackFileApi.exportPicklistItems(picked, 'xls', responseSpecUtil.OK_RESPONSE_SPEC))
        pickPackFileApi.exportPicklistItems(picked, 'pdf', UNSUPPORTED_FORMAT)
    }

    void 'importing the pick CSV picks its lines'() {
        given: 'a request being picked, its picks cleared'
        stockUp(20)
        String id = picking([(product): 5])
        picklistApi.clearPicklist(picklistId(id), responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        String csv = "Id,Code,Name,Lot,Expiration,Bin,Quantity\n${firstItemId(id)},${product.productCode},,SM-LOT-1,,,5\n"

        when:
        JsonPath imported = pickPackFileApi.importPicklistItems(id, csv, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        imported.getString('message') == 'Data imported successfully'
        imported.getList('errors') == null
        pickedQuantities(id) == [5]
    }

    void 'rows of the pick CSV that do not validate are reported by row and not imported'() {
        given:
        stockUp(20)
        String id = picking([(product): 5])
        String csv = "Id,Code,Name,Lot,Expiration,Bin,Quantity\n${firstItemId(id)},${product.productCode},,NO-SUCH-LOT,,,5\n"

        when:
        JsonPath imported = pickPackFileApi.importPicklistItems(id, csv, responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()
        List<String> errors = imported.getList('errors', String)

        then: 'the row is reported, and the existing pick stays'
        imported.getString('message') == 'Data imported with errors'
        errors.every { it.startsWith('Row 1: ') }
        errors[0] == 'Row 1: Unable to find inventory item with lot number NO-SUCH-LOT'
        pickedQuantities(id) == [5]
    }

    @PendingFeature(reason = 'an unsupported export format answers 500, not 400')
    void 'the pack template lists the shipment items, or one empty row before picking'() {
        given:
        stockUp(20)
        String picked = picked([(product): 5])
        String unpicked = requested([(product): 4])

        when:
        List<String> csv = pickPackFileApi.exportPackTemplate(picked, null, responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()
        List<String> empty = pickPackFileApi.exportPackTemplate(unpicked, 'csv', responseSpecUtil.OK_RESPONSE_SPEC)
                .body.asString().readLines()

        then:
        csv.size() == 2
        csv[0].split(',').size() == 11
        csv[1].startsWith("${shipmentItemId(picked)},${product.productCode},")
        csv[1].contains(',SM-LOT-1,')
        empty.size() == 2
        empty[1].replace(',', '').replace('"', '').trim() == ''

        expect:
        isExcel(pickPackFileApi.exportPackTemplate(picked, 'xls', responseSpecUtil.OK_RESPONSE_SPEC))
        pickPackFileApi.exportPackTemplate(picked, 'pdf', UNSUPPORTED_FORMAT)
    }

    void 'importing the pack CSV puts shipment items on the named pallet and box; unknown items are reported'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])
        String itemId = shipmentItemId(id)
        String header = 'Id,Code,Name,Bin,Lot,Expiration,Quantity,UoM,Recipient,Pack level 1,Pack level 2\n'

        when:
        JsonPath imported = pickPackFileApi.importPackListItems(id,
                header + "${itemId},${product.productCode},,,SM-LOT-1,,5,EA,,Pallet 2,Box B\n",
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        imported.getString('message') == 'Data imported successfully'
        ShipmentItem.withNewSession {
            ShipmentItem item = ShipmentItem.get(itemId)
            [item.container?.name, item.container?.parentContainer?.name]
        } == ['Box B', 'Pallet 2']

        when:
        JsonPath withErrors = pickPackFileApi.importPackListItems(id,
                header + "no-such-item,${product.productCode},,,SM-LOT-1,,5,EA,,Pallet 3,\n" +
                        "${itemId},${product.productCode},,,SM-LOT-1,,5,EA,Nobody Known,Pallet 3,\n",
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        withErrors.getString('message') == 'Data imported with errors'
        withErrors.getList('errors', String).size() == 2
        withErrors.getList('errors', String)[0].startsWith('Row 1: ')
        withErrors.getList('errors', String)[1].startsWith('Row 2: ')
        ShipmentItem.withNewSession { ShipmentItem.get(itemId).container?.name } == 'Box B'
    }

    void 'the files of a stock movement that does not exist are a 404'() {
        expect:
        pickPackFileApi.exportPicklistTemplate('no-such-movement', null, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        pickPackFileApi.exportPicklistItems('no-such-movement', null, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        pickPackFileApi.exportPackTemplate('no-such-movement', null, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'a pick/pack import without a file is accepted (200), not a 400')
    void 'an import with an empty file, or without the file, is a 400'() {
        given:
        stockUp(20)
        String id = picked([(product): 5])
        ResponseSpecification fileRequired = new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorMessage', Matchers.equalTo('An import file (importFile) is required'))
                .build()

        expect:
        pickPackFileApi.importPicklistItems(id, '', fileRequired)
        pickPackFileApi.importPackListItems(id, '', fileRequired)
        pickPackFileApi.importPicklistItems(id, 'Id\n', fileRequired, 'anotherFile')
        pickPackFileApi.importPackListItems(id, 'Id\n', fileRequired, 'anotherFile')
    }
}
