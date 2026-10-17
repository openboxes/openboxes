package org.pih.warehouse.api.regression.platform.spec.fulfillment

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.path.json.JsonPath
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.fulfillment.FulfillmentApiWrapper
import org.pih.warehouse.api.regression.platform.spec.stockmovement.StockMovementSpec
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode

/**
 * Importing a packing list as a shipped outbound: validating it row by row, and saving it.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class FulfillmentApiSpec extends StockMovementSpec {

    @Autowired
    FulfillmentApiWrapper fulfillmentApiWrapper

    private static final ResponseSpecification INVALID = new ResponseSpecBuilder()
            .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
            .expectBody('errorCode', Matchers.equalTo(400))
            .expectBody('errorMessage', Matchers.equalTo('The packing list has validation errors'))
            .build()

    private String body(List<Map> rows) {
        return fulfillmentApiWrapper.packingListBody(facility, destination, requestor, rows)
    }

    private Map row(Map overrides = [:]) {
        return [product: product.productCode, lotNumber: 'SM-LOT-1', quantityPicked: 5] + overrides
    }

    private int requestsTo() {
        return Requisition.withNewSession { Requisition.countByDestination(destination) }
    }

    void 'a valid packing list validates and comes back as table rows, and nothing is saved'() {
        given:
        stockUp(20)

        when:
        JsonPath validated = fulfillmentApiWrapper.api.validate(body([row(palletName: 'Pallet 1', boxName: 'Box A')]),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath()

        then:
        validated.getMap('errors.packingList') == [:]
        validated.getList('data.rowId') == ['2']
        validated.getString('data[0].product.productCode') == product.productCode
        validated.getString('data[0].lotNumber') == 'SM-LOT-1'
        validated.getInt('data[0].quantityPicked') == 5
        validated.getString('data[0].boxName') == 'Box A'
        requestsTo() == 0
    }

    @PendingFeature(reason = 'fulfillment validation errors answer {errors: ...} without errorCode/errorMessage')
    void 'each invalid row reports its own field errors, and every row still comes back'() {
        given:
        stockUp(20)

        when:
        JsonPath invalid = fulfillmentApiWrapper.api.validate(body([
                row(quantityPicked: 50),                    // more than the 20 in stock
                row(lotNumber: 'NO-SUCH-LOT'),
                row(binLocation: 'NO-SUCH-BIN'),
                row(boxName: 'Box without a pallet'),
                row(recipient: 'Nobody Known'),
                row(),                                      // valid
        ]), INVALID).jsonPath()

        then:
        invalid.getList('data.rowId') == ['2', '3', '4', '5', '6', '7']
        Map rows = invalid.getMap('errors.packingList')
        rows.keySet() == ['2', '3', '4', '5', '6'] as Set
        rows['2'].quantityPicked == ['The quantity you have picked (50) is not available in stock']
        rows['3'].lotNumber[0].startsWith('Inventory item for given lot number (NO-SUCH-LOT)')
        rows['4'].binLocation == ['The provided bin location (NO-SUCH-BIN) has not been found']
        rows['5'].boxName[0].startsWith('You have to specify Pack Level 1')
        rows['6'].recipient[0].startsWith('Recipient with provided name (Nobody Known')
    }

    @PendingFeature(reason = 'fulfillment validation errors answer {errors: ...} without errorCode/errorMessage')
    void 'the same origin and destination is refused on the details'() {
        given:
        stockUp(20)
        String sameBothEnds = fulfillmentApiWrapper.packingListBody(facility, facility, requestor, [row()])

        when:
        JsonPath invalid = fulfillmentApiWrapper.api.validate(sameBothEnds, INVALID).jsonPath()

        then:
        invalid.getList('errors.fulfillmentDetails.origin', String)[0].contains('be the same as destination')
        invalid.getList('errors.fulfillmentDetails.destination').size() == 1
    }

    @PendingFeature(reason = 'a fulfillment request with a missing section answers 500')
    void 'missing sections and an empty packing list are reported on their own fields'() {
        when: 'nothing at all'
        JsonPath empty = fulfillmentApiWrapper.api.validate('{}', INVALID).jsonPath()

        then:
        empty.getList('errors.fulfillmentDetails.fulfillmentDetails').size() == 1
        empty.getList('errors.sendingOptions.sendingOptions').size() == 1
        empty.getList('errors.packingList.generalErrors').size() == 1
        empty.getList('data') == []
        !empty.getString('errors').contains('org.pih')

        when: 'the details, but an empty packing list'
        JsonPath noRows = fulfillmentApiWrapper.api.validate(body([]), INVALID).jsonPath()

        then:
        noRows.getMap('errors.fulfillmentDetails') == [:]
        noRows.getMap('errors.sendingOptions') == [:]
        noRows.getList('errors.packingList.generalErrors').size() == 1
    }

    void 'saving creates the outbound already shipped, packed as listed, and debits the stock'() {
        given:
        stockUp(20)

        when:
        String id = fulfillmentApiWrapper.api.save(body([row(palletName: 'Pallet 1', boxName: 'Box A')]),
                responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('data.id')

        then:
        Shipment.withNewSession {
            Shipment shipment = Shipment.findByRequisition(Requisition.get(id))
            Transaction debit = Transaction.findByOutgoingShipment(shipment)
            [shipment.currentStatus, shipment.shipmentItems*.quantity, shipment.shipmentItems*.container*.name,
             debit.inventory.id, debit.transactionEntries*.quantity]
        } == [ShipmentStatusCode.SHIPPED, [5], ['Box A'], facility.inventory.id, [5]]
    }

    @PendingFeature(reason = 'fulfillment validation errors answer {errors: ...} without errorCode/errorMessage')
    void 'saving an invalid packing list is refused with the row errors, and nothing is saved'() {
        given:
        stockUp(20)

        when:
        JsonPath invalid = fulfillmentApiWrapper.api.save(body([row(quantityPicked: 50)]), INVALID).jsonPath()

        then:
        invalid.getMap('errors.packingList').keySet() == ['2'] as Set
        requestsTo() == 0
    }

}
