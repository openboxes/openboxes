package org.pih.warehouse.api.regression.platform.client.stockmovement

import groovy.transform.InheritConstructors
import io.restassured.response.Response
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Product

@TestComponent
@InheritConstructors
class StockMovementApiWrapper extends ApiWrapper<StockMovementApi> {

    /** Creates an outbound stock movement (a request from origin to destination) and returns its id. */
    String createOutboundOK(Location origin, Location destination, Person requestedBy, String description) {
        String body = new JSONObject()
                .put('name', '')
                .put('description', description)
                .put('dateRequested', jsonObjectUtil.asDateForRequestBody(new Date()))
                .put('origin', jsonObjectUtil.asIdForRequestBody(origin))
                .put('destination', jsonObjectUtil.asIdForRequestBody(destination))
                .put('requestedBy', jsonObjectUtil.asIdForRequestBody(requestedBy))
                .put('stocklist', new JSONObject().put('id', ''))
                .put('requestType', 'STOCK')
                .toString()
        return api.create(body, responseSpecUtil.CREATED_RESPONSE_SPEC).jsonPath().getString('data.id')
    }

    /** Sets the requested line items: [(product): quantity]. Returns the response of updateItems. */
    Response addItemsOK(String id, Map<Product, Integer> quantities) {
        JSONArray lineItems = new JSONArray()
        int sortOrder = 100
        quantities.each { Product product, Integer quantity ->
            lineItems.add(new JSONObject()
                    .put('product', jsonObjectUtil.asIdForRequestBody(product))
                    .put('quantityRequested', quantity)
                    .put('sortOrder', sortOrder))
            sortOrder += 100
        }
        String body = new JSONObject().put('id', id).put('lineItems', lineItems).toString()
        return api.updateItems(id, body, responseSpecUtil.OK_RESPONSE_SPEC)
    }

    /** Moves the stock movement to the given status (with any extra flags, e.g. createPicklist). */
    Response updateStatusOK(String id, String status, Map<String, ?> extra = [:]) {
        JSONObject body = new JSONObject().put('status', status)
        extra.each { String key, value -> body.put(key, value) }
        return api.updateStatus(id, body.toString(), responseSpecUtil.OK_RESPONSE_SPEC)
    }
}
