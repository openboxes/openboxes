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
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem

class StockMovementItemApiController {

    def inventoryService
    def stockMovementService

    
    def list = { 
        StockMovement stockMovement = stockMovementService.getStockMovement(params?.stockMovement?.id)
        if (!stockMovement) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }
        render ([data:stockMovement.lineItems] as JSON)
    }
    
    def read = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)
        render ([data:stockMovementItem] as JSON)
    }


    def update = {
        JSONObject jsonObject = request.JSON

        log.info "JSON " + jsonObject
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        Boolean autoSuggest = jsonObject.autoSuggest ?
                Boolean.parseBoolean(jsonObject.autoSuggest):Boolean.FALSE

        Boolean clearPicklist = jsonObject.clearPicklist ?
                Boolean.parseBoolean(jsonObject.clearPicklist):Boolean.FALSE

        if (autoSuggest) {
            log.info "Auto creating picklist for stock movement item ${stockMovementItem}"
            stockMovementService.autoCreatePicklist(stockMovementItem)
        }
        else if (clearPicklist) {
            stockMovementService.clearPicklist(stockMovementItem)
        }
        else {
            log.info("Updating picklist items")
            List picklistItems = jsonObject.remove("picklistItems")

            if (!picklistItems) {
                throw new IllegalArgumentException("Must specifiy picklistItems if autoSuggest is not enabled")
            }
            picklistItems.each { picklistItem ->

                InventoryItem inventoryItem = picklistItem["inventoryItem.id"] ?
                        InventoryItem.get(picklistItem["inventoryItem.id"]) : null

                Location binLocation = picklistItem["binLocation.id"] ?
                        Location.get(picklistItem["binLocation.id"]) : null

                BigDecimal quantity = picklistItem.quantityPicked ? new BigDecimal(picklistItem.quantityPicked) : null

                String reasonCode = picklistItem.reasonCode
                String comment = picklistItem.comment

                stockMovementService.createOrUpdatePicklistItem(stockMovementItem, inventoryItem, binLocation,
                        quantity.intValueExact(), reasonCode, comment)
            }
        }
        render ([data:stockMovementItem] as JSON)

    }

}