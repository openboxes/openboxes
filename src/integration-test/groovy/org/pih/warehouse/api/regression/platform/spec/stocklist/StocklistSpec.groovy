package org.pih.warehouse.api.regression.platform.spec.stocklist

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import io.restassured.response.Response
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.regression.platform.client.stocklist.StocklistApi
import org.pih.warehouse.api.regression.platform.client.stocklist.StocklistItemApi
import org.pih.warehouse.common.domain.builder.core.LocationTestBuilder
import org.pih.warehouse.core.Location

/**
 * Shared fixtures for the stock list specs: a ward that stock lists replenish from the main facility, and helpers
 * to make stock lists and lines through the API. Tests never rely on a clean database: names are unique per run.
 */
abstract class StocklistSpec extends RegressionApiSpec {

    @Autowired
    StocklistApi stocklistApi

    @Autowired
    StocklistItemApi stocklistItemApi

    @Shared
    Location ward

    @Shared
    String requestedById

    @Override
    void setupData() {
        if (ward) {
            return
        }
        ward = new LocationTestBuilder().name("Stock list ward ${UUID.randomUUID().toString().take(8)}").asFacility()
                .build(true)
        requestedById = '1'  // the admin user's person record
    }

    String uniqueName(String prefix) {
        return "${prefix} ${UUID.randomUUID().toString().take(8)}".toString()
    }

    /** A new (unpublished) stock list, by default replenishing the ward from the main facility; returns its id. */
    String newStocklist(String name = uniqueName('Stock list'), String originId = facility.id, String destinationId = ward.id) {
        Response created = stocklistApi.create(new JSONObject()
                .put('name', name)
                .put('origin', new JSONObject().put('id', originId))
                .put('destination', new JSONObject().put('id', destinationId))
                .put('requestedBy', new JSONObject().put('id', requestedById))
                .toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        return created.jsonPath().getString('data."requisition.id"')
    }

    /** Adds a line of the shared test product to the stock list; returns the line id. */
    String newLine(String stocklistId, int maxQuantity = 10) {
        Response created = stocklistItemApi.create(product.id, new JSONObject()
                .put('stocklistId', stocklistId)
                .put('maxQuantity', maxQuantity)
                .toString(), responseSpecUtil.CREATED_RESPONSE_SPEC)
        return created.jsonPath().getString('data."requisitionItem.id"')
    }
}
