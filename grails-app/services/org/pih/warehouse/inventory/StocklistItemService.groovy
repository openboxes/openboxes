/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.validation.ValidationException
import org.pih.warehouse.api.StocklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem

class StocklistItemService {

    def requisitionService
    def locationService

    boolean transactional = true

    List<StocklistItem> getStocklistItems(String productId) {
        List<Requisition> templates = requisitionService.getRequisitionTemplates()
        List<StocklistItem> stocklistItems = []

        templates.each { Requisition requisition ->
            stocklistItems.addAll(requisition.requisitionItems?.findAll {
                it.product.id == productId
            }?.collect { StocklistItem.createFromRequisitionItem(it) } ?: [])
        }

        return stocklistItems
    }

    StocklistItem getStocklistItem(String id) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)

        return requisitionItem ? StocklistItem.createFromRequisitionItem(requisitionItem) : null
    }

    StocklistItem createStocklistItem(StocklistItem stocklistItem, String productId) {
        Requisition requisition = Requisition.get(stocklistItem.stocklistId)

        if (!requisition) {
            throw new IllegalArgumentException("No stocklist fund with ID ${stocklistItem.stocklistId}")
        }

        RequisitionItem requisitionItem = new RequisitionItem()
        requisitionItem.quantity = stocklistItem.maxQuantity
        requisitionItem.product = Product.get(productId)

        requisition.addToRequisitionItems(requisitionItem)

        requisitionService.saveTemplateRequisition(requisition)

        return StocklistItem.createFromRequisitionItem(requisitionItem)
    }

    StocklistItem updateStocklistItem(StocklistItem stocklistItem) {
        RequisitionItem requisitionItem = stocklistItem.requisitionItem
        requisitionItem.quantity = stocklistItem.maxQuantity

        if (requisitionItem.hasErrors() || !requisitionItem.save(flush: true)) {
            throw new ValidationException("Invalid requisitionItem", requisitionItem.errors)
        }

        return StocklistItem.createFromRequisitionItem(requisitionItem)
    }

    void deleteStocklistItem(String id) {
        RequisitionItem requisitionItem = RequisitionItem.get(id)
        if (!requisitionItem) {
            throw new IllegalArgumentException("No Stock List Item found with ID ${id}")
        }

        requisitionItem.requisition.removeFromRequisitionItems(requisitionItem)
        requisitionItem.delete()
    }

    def getAvailableStocklists() {
        List<Requisition> templates = requisitionService.getRequisitionTemplates()

        return templates?.collect {
            [
                    id                  : it.id,
                    name                : it.name,
                    "location.id"       : it.origin?.id,
                    "location.name"     : it.origin?.name,
                    "locationGroup.id"  : it.origin?.locationGroup?.id,
                    "locationGroup.name": it.origin?.locationGroup?.name,
                    "manager.id"        : it.requestedBy?.id,
                    "manager.name"      : it.requestedBy?.name,
            ]
        }
    }
}
