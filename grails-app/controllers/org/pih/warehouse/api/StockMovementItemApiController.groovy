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
import org.pih.warehouse.picklist.PicklistItem

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

        log.info "JSON " + jsonObject.toString(4)
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        Boolean createPicklist = jsonObject.createPicklist ?
                Boolean.parseBoolean(jsonObject.createPicklist):Boolean.FALSE

        Boolean clearPicklist = jsonObject.clearPicklist ?
                Boolean.parseBoolean(jsonObject.clearPicklist):Boolean.FALSE

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        if (createPicklist) {
            log.info "Auto creating picklist for stock movement item ${stockMovementItem}"
            stockMovementService.createPicklist(stockMovementItem)
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
            picklistItems.each { picklistItemMap ->

                PicklistItem picklistItem = picklistItemMap["id"] ?
                        PicklistItem.get(picklistItemMap["id"]) : null

                InventoryItem inventoryItem = picklistItemMap["inventoryItem.id"] ?
                        InventoryItem.get(picklistItemMap["inventoryItem.id"]) : null

                Location binLocation = picklistItemMap["binLocation.id"] ?
                        Location.get(picklistItemMap["binLocation.id"]) : null

                BigDecimal quantityPicked = (picklistItemMap.quantityPicked != null && picklistItemMap.quantityPicked != "") ?
                        new BigDecimal(picklistItemMap.quantityPicked) : null

                String reasonCode = picklistItemMap.reasonCode
                String comment = picklistItemMap.comment

                stockMovementService.createOrUpdatePicklistItem(stockMovementItem, picklistItem, inventoryItem, binLocation,
                        quantityPicked?.intValueExact(), reasonCode, comment)
            }
        }
        render ([data:stockMovementItem] as JSON)

    }

}
