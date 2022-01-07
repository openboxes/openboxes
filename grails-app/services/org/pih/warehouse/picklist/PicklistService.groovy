/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.picklist

import grails.validation.ValidationException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

class PicklistService {

    Picklist save(Map data) {
        def itemsData = data.picklistItems ?: []
        data.remove("picklistItems")

        def picklist = Picklist.get(data.id) ?: new Picklist()
        picklist.properties = data
        picklist.name = picklist.requisition.name

        def requisition = Requisition.get(picklist.requisition.id)
        requisition.status = RequisitionStatus.CREATED

        def picklistItems = itemsData.collect {
            itemData ->
                def picklistItem = picklist.picklistItems?.find { i -> itemData.id && i.id == itemData.id }
                if (picklistItem) {
                    picklistItem.properties = itemData
                } else {
                    picklistItem = new PicklistItem(itemData)
                    picklist.addToPicklistItems(picklistItem)
                }
                picklistItem
        }

        def itemsToDelete = picklist.picklistItems.findAll {
            dbItem -> !picklistItems.any { clientItem -> clientItem.id == dbItem.id }
        }
        itemsToDelete.each {
            picklist.removeFromPicklistItems(it)
            it.delete()
        }
        return picklist.save()
    }

    def updatePicklistItem(String id, String productId, String productCode, BigDecimal quantityPicked) {

        PicklistItem picklistItem = PicklistItem.get(id)
        Product product = Product.get(productId)
        if (product != picklistItem?.requisitionItem?.product) {
            throw new IllegalArgumentException("Scanned product ${productCode} does not match picklist item ${picklistItem?.requisitionItem?.product?.productCode}")
        }

        // Initialize quantity picked
        if (!picklistItem.quantityPicked) {
            picklistItem.quantityPicked = 0
        }
        picklistItem.quantityPicked += quantityPicked
        picklistItem.datePicked = new Date()
        picklistItem.picker = User.get(session.user.id)
        if (!picklistItem.save(flush:true)) {
            throw new ValidationException("Unable to save picklist item", picklistItem.errors)
        }
    }

    def triggerPicklistStatusUpdate(Picklist picklist) {

        Requisition requisition = picklist.requisition
        if (!requisition) {
            throw new IllegalStateException("Picklist should exist without a requisition")
        }

        // If requisition is in picking, but is fully picked, then transition to PICKED status
        if (requisition.status == RequisitionStatus.PICKING) {
            boolean stillPicking = picklist.picklistItems.any { PicklistItem picklistItem ->
                picklistItem.quantityRemaining > 0
            }
            if (!stillPicking) {
                requisition.status = Requisition.PICKED
                requisition.save(flush:true)
            }
        }

    }



    def getQuantityPickedByProductAndLocation(Location location, Product product) {
        Picklist.createCriteria().list {
            projections {
                picklistItems {
                    groupProperty("binLocation.id", "binLocation")
                    groupProperty("inventoryItem.id", "inventoryItem")
                    sum("quantity", "quantity")
                    groupProperty("sortOrder", "sortOrder")
                }
            }
            requisition {
                'in'("status", RequisitionStatus.listPending())
                eq("origin", location)
            }
            picklistItems {
                if (product) {
                    requisitionItem {
                        eq("product", product)
                    }
                }
                order("binLocation")
                order("inventoryItem")
            }
        }.collect { [binLocation: it[0], inventoryItem: it[1], quantityAllocated: it[2]] }
    }
}
