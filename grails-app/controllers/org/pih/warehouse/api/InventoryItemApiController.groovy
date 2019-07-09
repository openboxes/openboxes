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
import org.pih.warehouse.inventory.InventoryItem


class InventoryItemApiController {

    def inventoryService
    def productService

    def list = {

       InventoryItem inventoryItem =  inventoryService.getInventoryItemByProduct(params.id)
        render ([data :inventoryItem] as JSON)
    }

    def update = {
        JSONObject jsonObject = request.JSON
        log.debug "update: " + jsonObject.toString(4)

        InventoryItem inventoryItem = inventoryService.getInvetoryItemById(params.id)
        if (!inventoryItem) {
            inventoryItem = new InventoryItem()
        }

        bindData(inventoryItem, jsonObject)
       // stocklist = stocklistService.updateStocklist(stocklist)

        inventoryItem = inventoryService.updateInvetoryItem(inventoryItem)

        render ([data:inventoryItem] as JSON)
    }

    def create = { InventoryItem inventoryItem ->
        JSONObject jsonObject = request.JSON

       inventoryItem = inventoryService.findOrCreateInventoryItem(inventoryItem)

        render ([data:inventoryItem] as JSON)
    }

    def delete = {
        inventoryService.deleteInvetoryItem(params.id)

        render status: 204
    }
}
