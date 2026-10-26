package org.pih.warehouse.api.regression.platform.client.fulfillment

import groovy.transform.InheritConstructors
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person

@TestComponent
@InheritConstructors
class FulfillmentApiWrapper extends ApiWrapper<FulfillmentApi> {

    /**
     * A packing list import body, as the outbound import screen sends it: request details, sending options (shipped
     * today, delivered in two days) and the rows. Each row gets its row id and the origin; a row is a map of the
     * other row fields (product: product code, lotNumber, binLocation: bin name, quantityPicked, recipient, ...).
     */
    String packingListBody(Location origin, Location destination, Person requestedBy, List<Map> rows) {
        Date today = new Date()
        JSONArray packingList = new JSONArray()
        rows.eachWithIndex { Map row, int index ->
            JSONObject item = new JSONObject().put('rowId', "${index + 2}".toString()).put('origin', origin.id)
            row.each { String key, value -> item.put(key, value) }
            packingList.add(item)
        }
        return new JSONObject()
                .put('fulfillmentDetails', new JSONObject()
                        .put('origin', jsonObjectUtil.asIdForRequestBody(origin))
                        .put('destination', jsonObjectUtil.asIdForRequestBody(destination))
                        .put('requestedBy', jsonObjectUtil.asIdForRequestBody(requestedBy))
                        .put('dateRequested', today.format('MM/dd/yyyy'))
                        .put('description', 'Imported outbound'))
                .put('sendingOptions', new JSONObject()
                        .put('expectedShippingDate', today.format(Constants.DELIVERY_DATE_FORMAT))
                        .put('expectedDeliveryDate', (today + 2).format('MM/dd/yyyy'))
                        .put('shipmentType', new JSONObject().put('id', '1'))
                        .put('trackingNumber', 'TRACK-IMPORT'))
                .put('packingList', packingList)
                .toString()
    }
}
