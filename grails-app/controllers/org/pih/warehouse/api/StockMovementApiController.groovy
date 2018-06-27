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

class StockMovementApiController {

    def stockMovementService

    def list = {
        def stockMovements = stockMovementService.getStockMovements(10)
//        if (params.fields) {
//            String [] propertyNames = params.fields?.split(",")
//            stockMovements = stockMovements.collect { StockMovement stockMovement ->
//                return stockMovement.getPropertyMap(propertyNames)
//            }
//        }
        render ([stockMovements.collect { StockMovement stockMovement -> stockMovement.toJson() }] as JSON)
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

        JSONObject json = request.JSON
        log.info "json: " + json


        def lineItems = json.remove("lineItems")
        StockMovement stockMovement = new StockMovement()
        bindData(stockMovement, json)
        
        log.info "line items: " + lineItems
        lineItems.each { lineItem ->
            log.info "product" + lineItem["product.id"]
            StockMovementItem stockMovementItem = new StockMovementItem()
            stockMovementItem.id = lineItem.id
            stockMovementItem.deleted = lineItem.deleted ? Boolean.parseBoolean(lineItem.deleted):Boolean.FALSE
            stockMovementItem.product = lineItem["product.id"] ? Product.load(lineItem["product.id"]) : null
            stockMovementItem.inventoryItem = lineItem["inventoryItem.id"] ? InventoryItem.load(lineItem["inventoryItem.id"]) : null
            //stockMovementItem.recipient = lineItem["recipient.id"] ? Person.load(lineItem["recipient.id"]) : null
            stockMovementItem.quantityRequested = lineItem.quantityRequested ? new BigDecimal(lineItem.quantityRequested) : null
            stockMovementItem.sortOrder = lineItem.sortOrder ? new Integer(lineItem.sortOrder) : null
            stockMovement.lineItems.add(stockMovementItem)
        }

        stockMovement = stockMovementService.updateStockMovement(stockMovement)
        render ([data:stockMovement] as JSON)
    }

    def delete = {
        stockMovementService.deleteStockMovement(params.id)
        render status: 204
    }

}