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
import org.pih.warehouse.requisition.RequisitionItem

class StockMovementItemApiController {

    def stockMovementService
    def productAvailabilityService

    def list = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params?.stockMovement?.id)
        if (!stockMovement) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }
        render([data: stockMovement.lineItems] as JSON)
    }

    def read = {
        def stockMovementItem = stockMovementService.getStockMovementItem(params.id, params.stepNumber)
        render([data: stockMovementItem] as JSON)
    }

    def details = {
        def stockMovementItem = stockMovementService.getStockMovementItem(params.id, params.stepNumber, true)
        render([data: stockMovementItem] as JSON)
    }

    def getStockMovementItems = {
        List<StockMovementItem> stockMovementItems = stockMovementService.getStockMovementItems(params.id, params.stepNumber, params.max, params.offset)
        render([data: stockMovementItems] as JSON)
    }

    def getSubstitutionItems = {
        RequisitionItem requisitionItem = RequisitionItem.load(params.id)
        def location = Location.get(session.warehouse.id)
        List<SubstitutionItem> substitutionItems = stockMovementService.getAvailableSubstitutions(location, requisitionItem)
        render([data: substitutionItems] as JSON)
    }

    def updatePicklist = {
        JSONObject jsonObject = request.JSON

        log.debug "JSON " + jsonObject.toString(4)
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        log.debug("Updating picklist items")
        List picklistItems = jsonObject.remove("picklistItems")
        String reasonCode = jsonObject.reasonCode

        if (!picklistItems) {
            throw new IllegalArgumentException("Must specifiy picklistItems")
        }

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        stockMovementService.updatePicklistItem(stockMovementItem, picklistItems, reasonCode)

        RequisitionItem requisitionItem = RequisitionItem.get(params.id)
        stockMovementService.createMissingShipmentItem(requisitionItem)

        render status: 200
    }

    def createPicklist = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentItemsForModifiedRequisitionItem(stockMovementItem)

        log.debug "Creating picklist for stock movement item ${stockMovementItem}"
        stockMovementService.createPicklist(stockMovementItem, false)

        RequisitionItem requisitionItem = RequisitionItem.get(params.id)
        stockMovementService.createMissingShipmentItem(requisitionItem)

        render status: 200
    }

    def clearPicklist = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentAndPicklistItemsForModifiedRequisitionItem(stockMovementItem)

        log.debug "Clear picklist for stock movement item ${stockMovementItem}"
        stockMovementService.clearPicklist(stockMovementItem)

        render status: 200
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

        render status: 200
    }

    def revertItem = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.revertItemAndCreateMissingPicklist(stockMovementItem)

        render status: 200
    }

    def cancelItem = {
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        stockMovementService.removeShipmentAndPicklistItemsForModifiedRequisitionItem(stockMovementItem)

        RequisitionItem requisitionItem = stockMovementItem.requisitionItem

        log.debug "Item canceled " + requisitionItem.id
        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
        requisitionItem.quantityApproved = 0

        requisitionItem.save()

        stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)

        render([data: stockMovementItem] as JSON)
    }

    def eraseItem = {
        stockMovementService.removeStockMovementItem(params.id)
        render status: 204
    }

    void bindLineItem(StockMovementItem stockMovementItem, def lineItem) {
        stockMovementItem.newQuantity = lineItem.newQuantity ? new BigDecimal(lineItem.remove("newQuantity")) : null
        stockMovementItem.quantityRevised = lineItem.quantityRevised ? new BigDecimal(lineItem.remove("quantityRevised")) : null

        bindData(stockMovementItem, lineItem)
    }

    def suggestPickableItems = {
        Location location = Location.get(session.warehouse.id)
        StockMovementItem stockMovementItem = stockMovementService.getStockMovementItem(params.id)

        Integer quantityRequired = stockMovementItem?.requisitionItem?.calculateQuantityRequired()
        List<AvailableItem> availableItems = stockMovementService.getAvailableItems(location, stockMovementItem.requisitionItem)
        log.info "available items: "
        availableItems.each {
            log.info new JSONObject(getAvailableItemDetails(it, quantityRequired)).toString(4)
        }
        log.info "suggested items: "
        List suggestedItems = stockMovementService.getSuggestedItems(availableItems, quantityRequired)
        suggestedItems.each {
            log.info new JSONObject(it.toJson()).toString(4)
        }

        def data = availableItems.collect { AvailableItem availableItem ->
            return getSuggestedItemDetails(suggestedItems, availableItem, quantityRequired)
        }
        render(template: "/common/dataTable", model: [data: data])
    }

    def getAvailableItemDetails = { AvailableItem availableItem, Integer quantityRequired ->
        return [
                locationNumber: availableItem.binLocation?.locationNumber,
                locationName: availableItem.binLocation?.name,
                locationType: availableItem.binLocation?.locationType?.name,
                lotNumber: availableItem.inventoryItem?.lotNumber,
                expirationDate: availableItem.inventoryItem?.expirationDate,
                quantityAvailable: availableItem.quantityAvailable,
                quantityOnHand: availableItem.quantityOnHand,
                isFullPallet: availableItem.quantityAvailable == quantityRequired,
                pickClassification: availableItem?.pickClassification
            ]
    }

    def getSuggestedItemDetails = { List suggestedItems, AvailableItem availableItem, Integer quantityRequired ->

        boolean isSuggested = suggestedItems.find { it.binLocation == availableItem.binLocation &&
                            it.inventoryItem == availableItem.inventoryItem }
        return [
                locationNumber: availableItem.binLocation?.locationNumber,
                locationName: availableItem.binLocation?.name,
                locationType: availableItem.binLocation?.locationType?.name,
                lotNumber: availableItem.inventoryItem?.lotNumber,
                expirationDate: availableItem.inventoryItem?.expirationDate,
                quantityAvailable: availableItem.quantityAvailable,
                quantityOnHand: availableItem.quantityOnHand,
                isFullPallet: availableItem.quantityAvailable == quantityRequired,
                pickClassification: availableItem?.pickClassification,
                isSuggested: isSuggested
            ]
    }




}
