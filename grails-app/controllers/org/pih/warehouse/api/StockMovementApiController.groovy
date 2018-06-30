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
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import java.text.SimpleDateFormat

class StockMovementApiController {

    def stockMovementService

    def list = {
        int max = Math.min(params.max ? params.int('max') : 10, 1000)
        int offset = params.offset? params.int("offset") : 0
        def stockMovements = stockMovementService.getStockMovements(max, offset)
        stockMovements = stockMovements.collect { StockMovement stockMovement ->
            Map json = stockMovement.toJson()
            def excludes = params.list("exclude")
            if (excludes) {
                excludes.each { exclude ->
                    json.remove(exclude)
                }
            }
            return json
        }
        render ([data:stockMovements] as JSON)
    }

    def read = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        render ([data:stockMovement] as JSON)
    }

    def create = { StockMovement stockMovement ->
        stockMovement = stockMovementService.createStockMovement(stockMovement)
        response.status = 201
        render ([data:stockMovement] as JSON)
	}

    def update = { //StockMovement stockMovement ->

        Object jsonObject = request.JSON
        log.info "json: " + jsonObject

        // Remove attributes that cause issues in the default grails data binder
        List lineItems = jsonObject.remove("lineItems")
        String dateRequested = jsonObject.remove("dateRequested")

        // Bind all other properties to stock movement
        StockMovement stockMovement = new StockMovement()
        stockMovement.dateRequested = new SimpleDateFormat("MM/dd/yyyy").parse(dateRequested)
        bindData(stockMovement, jsonObject)
        bindLineItems(stockMovement, lineItems)

        stockMovement = stockMovementService.updateStockMovement(stockMovement)
        render ([data:stockMovement] as JSON)
    }

    def delete = {
        stockMovementService.deleteStockMovement(params.id)
        render status: 204
    }

    /**
     * Bind the given line items (JSONArray) to StockMovementItem objects and add them to the given
     * StockMovement object.
     *
     * NOTE: THis method was necessary because the default data binder for Grails command objects
     * does not see to handle nested objects very well.
     *
     * @param stockMovement
     * @param lineItems
     */
    void bindLineItems(StockMovement stockMovement, List lineItems) {
        log.info "line items: " + lineItems
        lineItems.each { lineItem ->
            StockMovementItem stockMovementItem = new StockMovementItem()
            stockMovementItem.id = lineItem.id
            stockMovementItem.product = lineItem["product.id"] ? Product.load(lineItem["product.id"]) : null
            stockMovementItem.inventoryItem = lineItem["inventoryItem.id"] ? InventoryItem.load(lineItem["inventoryItem.id"]) : null
            stockMovementItem.quantityRequested = lineItem.quantityRequested ? new BigDecimal(lineItem.quantityRequested) : null
            stockMovementItem.sortOrder = lineItem.sortOrder && !lineItem.isNull("sortOrder") ? new Integer(lineItem.sortOrder) : null

            // Actions
            stockMovementItem.delete = lineItem.delete ? Boolean.parseBoolean(lineItem.delete):Boolean.FALSE
            stockMovementItem.revert = lineItem.revert ? Boolean.parseBoolean(lineItem.revert):Boolean.FALSE
            stockMovementItem.cancel = lineItem.cancel ? Boolean.parseBoolean(lineItem.cancel):Boolean.FALSE

            // When revising quantity you need quantity revised and reason code
            stockMovementItem.quantityRevised = lineItem.quantityRevised ? new BigDecimal(lineItem.quantityRevised) : null
            stockMovementItem.reasonCode = lineItem.reasonCode
            stockMovementItem.comments = lineItem.comments

            // Not supported yet because recipient is a String on Requisition Item and a Person on Shipment Item.
            //stockMovementItem.recipient = lineItem["recipient.id"] ? Person.load(lineItem["recipient.id"]) : null

            stockMovement.lineItems.add(stockMovementItem)
        }
    }

}