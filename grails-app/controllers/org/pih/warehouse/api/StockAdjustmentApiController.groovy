/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.AdjustStockCommand
import org.pih.warehouse.product.Product

class StockAdjustmentApiController {

    def inventoryService

    def create = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)

        if (!location) {
            throw new IllegalArgumentException("Cannot create stock adjustments without a location - sign in or provide location.id as a request parameter")
        }

        def jsonObject = request.JSON
        List<StockAdjustment> stockAdjustments = new ArrayList<StockAdjustment>()
        bindStockAdjustmentData(stockAdjustments, jsonObject)

        // FIXME Forgot there was already a command object for this
        stockAdjustments.each { StockAdjustment stockAdjustment ->
            AdjustStockCommand adjustStockCommand = new AdjustStockCommand()
            adjustStockCommand.inventoryItem = stockAdjustment.inventoryItem
            adjustStockCommand.quantity = stockAdjustment.quantityAdjusted
            adjustStockCommand.comment = stockAdjustment.comments
            adjustStockCommand.location = location
            adjustStockCommand.binLocation = stockAdjustment.binLocation
            inventoryService.adjustStock(adjustStockCommand)
        }

        render([data: stockAdjustments] as JSON)
    }


    void bindStockAdjustmentData(List<StockAdjustment> stockAdjustments, JSONArray jsonArray) {
        jsonArray.each {
            stockAdjustments << bindStockAdjustmentData(new StockAdjustment(), it)
        }
    }

    void bindStockAdjustmentData(List<StockAdjustment> stockAdjustments, JSONObject jsonObject) {
        stockAdjustments << bindStockAdjustmentData(new StockAdjustment(), jsonObject)
    }

    StockAdjustment bindStockAdjustmentData(StockAdjustment stockAdjustment, JSONObject jsonObject) {
        bindData(stockAdjustment, jsonObject)

        if (!stockAdjustment.inventoryItem) {
            Product product = Product.get(jsonObject.productId)
            Date expirationDate = jsonObject.expirationDate ? Constants.EXPIRATION_DATE_FORMATTER.parse(jsonObject.expirationDate) : null
            stockAdjustment.inventoryItem = inventoryService.findOrCreateInventoryItem(product, jsonObject.lotNumber, expirationDate)
        }

        return stockAdjustment
    }

}

