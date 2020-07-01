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
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementItemApiController {

    def inventoryService
    def stockMovementService

    def list = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params?.stockMovement?.id)
        if (!stockMovement) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }
        render([data: stockMovement.lineItems] as JSON)
    }

    def read = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)
        render([data: stockMovementItem] as JSON)
    }

    def getStockMovementItems = {
        List<StockMovementItem> stockMovementItems = stockMovementService.getStockMovementItems(params.id, params.stepNumber, params.max, params.offset)
        render([data: stockMovementItems] as JSON)
    }

    def updatePicklist = {
        JSONObject jsonObject = request.JSON

        log.debug "JSON " + jsonObject.toString(4)
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        log.debug("Updating picklist items")
        List picklistItems = jsonObject.remove("picklistItems")
        String reasonCode = jsonObject.reasonCode

        if (!picklistItems) {
            throw new IllegalArgumentException("Must specifiy picklistItems")
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

            String comment = picklistItemMap.comment

            stockMovementService.createOrUpdatePicklistItem(stockMovementItem, picklistItem, inventoryItem, binLocation,
                    quantityPicked?.intValueExact(), reasonCode, comment)
        }

        RequisitionItem requisitionItem = RequisitionItem.get(params.id)
        stockMovementService.createMissingShipmentItem(requisitionItem)

        PickPageItem pickPageItem = stockMovementService.buildPickPageItem(requisitionItem, stockMovementItem.sortOrder)

        render([data: pickPageItem] as JSON)
    }

    def createPicklist = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        log.debug "Creating picklist for stock movement item ${stockMovementItem}"
        stockMovementService.createPicklist(stockMovementItem)

        RequisitionItem requisitionItem = RequisitionItem.get(params.id)
        stockMovementService.createMissingShipmentItem(requisitionItem)

        PickPageItem pickPageItem = stockMovementService.buildPickPageItem(requisitionItem, stockMovementItem.sortOrder)

        render([data: pickPageItem] as JSON)
    }

    def clearPicklist = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        log.debug "Clear picklist for stock movement item ${stockMovementItem}"
        stockMovementService.clearPicklist(stockMovementItem)

        RequisitionItem requisitionItem = RequisitionItem.get(params.id)
        PickPageItem pickPageItem = stockMovementService.buildPickPageItem(requisitionItem, stockMovementItem.sortOrder)

        render([data: pickPageItem] as JSON)
    }

    def substituteItem = {
        JSONObject jsonObject = request.JSON

        log.debug "JSON " + jsonObject.toString(4)

        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        List substitutionItems = jsonObject.remove("substitutionItems")

        bindLineItem(stockMovementItem, jsonObject)

        substitutionItems?.each { item ->
            StockMovementItem subItem = new StockMovementItem()

            bindLineItem(subItem, item)

            stockMovementItem.substitutionItems?.add(subItem)
        }

        stockMovementService.substituteItem(stockMovementItem)

        EditPageItem editPageItem = stockMovementService.buildEditPageItem(stockMovementItem)

        render([data: editPageItem] as JSON)
    }

    def revertItem = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.revertItem(stockMovementItem)

        EditPageItem editPageItem = stockMovementService.buildEditPageItem(stockMovementItem)

        render([data: editPageItem] as JSON)
    }

    def cancelItem = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem

        log.debug "Item canceled " + requisitionItem.id
        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
        requisitionItem.quantityApproved = 0

        requisitionItem.save()

        stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)

        render([data: stockMovementItem] as JSON)
    }

    def removeItem = {
        stockMovementService.removeStockMovementItem(params.id)
        render status: 204
    }

    void bindLineItem(StockMovementItem stockMovementItem, def lineItem) {
        stockMovementItem.newQuantity = lineItem.newQuantity ? new BigDecimal(lineItem.remove("newQuantity")) : null
        stockMovementItem.quantityRevised = lineItem.quantityRevised ? new BigDecimal(lineItem.remove("quantityRevised")) : null

        bindData(stockMovementItem, lineItem)
    }
}
