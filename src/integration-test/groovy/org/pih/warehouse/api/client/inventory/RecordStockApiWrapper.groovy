package org.pih.warehouse.api.client.inventory

import groovy.transform.InheritConstructors
import io.restassured.response.Response
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.boot.test.context.TestComponent

import org.pih.warehouse.api.client.base.ApiWrapper
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.RecordInventoryCommand
import org.pih.warehouse.inventory.RecordInventoryRowCommand

@TestComponent
@InheritConstructors
class RecordStockApiWrapper extends ApiWrapper<RecordStockApi> {

    Response saveRecordStockOK(Location facility, RecordInventoryCommand command) {
        JSONArray rows = new JSONArray()
        for (RecordInventoryRowCommand row in (command.recordInventoryRows as List<RecordInventoryRowCommand>)) {
            rows.add(new JSONObject()
                    .put('id', row.id)
                    .put('binLocation', jsonObjectUtil.asIdForRequestBody(row.binLocation))
                    .put('lotNumber', row.lotNumber)
                    .put('expirationDate', jsonObjectUtil.asDateForRequestBody(row.expirationDate))
                    .put('oldQuantity', row.oldQuantity)
                    .put('newQuantity', row.newQuantity)
                    .put('comment', row.comment))
        }

        String body = new JSONObject()
                .put('product', jsonObjectUtil.asIdForRequestBody(command.product))
                .put('inventory', jsonObjectUtil.asIdForRequestBody(command.inventory))
                .put('transactionDate', jsonObjectUtil.asDateForRequestBody(command.transactionDate))
                .put('comment', command.comment)
                .put('recordInventoryRows', rows)
                .toString()

        return api.saveRecordStock(facility.id, body, responseSpecUtil.OK_RESPONSE_SPEC)
    }
}
