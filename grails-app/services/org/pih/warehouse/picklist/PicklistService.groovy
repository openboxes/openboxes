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

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class PicklistService {

    def productAvailabilityService

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

    void clearPicklist(Order order) {
        order?.orderItems?.each { OrderItem orderItem ->
            clearPicklist(orderItem)
        }
    }

    void clearPicklist(OrderItem orderItem) {
        Picklist picklist = orderItem?.order?.picklist

        if (picklist) {
            picklist.picklistItems.findAll {
                it.orderItem == orderItem
            }.toArray().each {
                it.disableRefresh = Boolean.TRUE
                picklist.removeFromPicklistItems(it)
                orderItem.removeFromPicklistItems(it)
                it.delete()
            }
            picklist.save()
        }

        productAvailabilityService.refreshProductsAvailability(orderItem?.order?.origin?.id, [orderItem?.product?.id], false)
    }

    void createPicklist(Order order) {
        order?.orderItems?.each { OrderItem orderItem ->
            createPicklist(orderItem)
        }
    }

    void createPicklist(OrderItem orderItem) {
        Location location = orderItem?.order?.origin

        if (orderItem?.quantity) {
            // Retrieve all available items and then calculate suggested
            List<AvailableItem> availableItems = getAvailableItems(location, orderItem)
            log.info "Available items: ${availableItems}"
            List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, orderItem?.quantity, orderItem?.order?.destination)
            log.info "Suggested items " + suggestedItems
            clearPicklist(orderItem)
            if (suggestedItems) {
                for (SuggestedItem suggestedItem : suggestedItems) {
                    createOrUpdatePicklistItem(
                            orderItem,
                            null,
                            suggestedItem.inventoryItem,
                            suggestedItem.binLocation,
                            suggestedItem.quantityPicked.intValueExact()
                    )
                }
            }
        }
    }

    void createPicklistFromItem(Order order) {
        order?.orderItems?.each { OrderItem orderItem ->
            createPicklistFromItem(orderItem)
        }
    }

    void createPicklistFromItem(OrderItem orderItem) {
        if (orderItem?.quantity) {
            clearPicklist(orderItem)
            createOrUpdatePicklistItem(
                orderItem,
                null,
                orderItem.inventoryItem,
                orderItem.destinationBinLocation,
                orderItem.quantity
            )
        }
    }

    void createOrUpdatePicklistItem(OrderItem orderItem, PicklistItem picklistItem,
                                    InventoryItem inventoryItem, Location binLocation,
                                    Integer quantity) {

        Order order = orderItem.order

        Picklist picklist = Picklist.findByOrder(order)
        if (!picklist) {
            picklist = new Picklist()
            picklist.order = order
        }

        // If one does not exist create it and add it to the list
        if (!picklistItem) {
            picklistItem = new PicklistItem()
            picklist.addToPicklistItems(picklistItem)
        }

        // Remove from picklist
        if (quantity == null) {
            picklist.removeFromPicklistItems(picklistItem)
        }
        // Populate picklist item
        else {
            orderItem.addToPicklistItems(picklistItem)
            picklistItem.inventoryItem = inventoryItem
            picklistItem.binLocation = binLocation
            picklistItem.quantity = quantity
            picklistItem.sortOrder = orderItem.orderIndex
            picklistItem.disableRefresh = Boolean.TRUE
        }
        picklist.save(flush: true)

        productAvailabilityService.refreshProductsAvailability(orderItem?.order?.origin?.id, [inventoryItem?.product?.id], false)
    }

    List getSuggestedItems(List<AvailableItem> availableItems, Integer quantityRequested, Location warehouse) {

        List suggestedItems = []
        List<AvailableItem> autoPickableItems = availableItems?.findAll { it.quantityAvailable > 0 && it.pickable }

        // As long as quantity requested is less than the total available we can iterate through available items
        // and pick until quantity requested is 0. Otherwise, we don't suggest anything because the user must
        // choose anyway. This might be improved in the future.
        Integer quantityAvailable = autoPickableItems ? autoPickableItems?.sum {
            it.quantityAvailable
        } : 0

        if (quantityRequested <= quantityAvailable) {

            for (AvailableItem availableItem : autoPickableItems) {
                InventoryLevel inventoryLevel = getInventoryLevelForItem(
                        availableItem?.inventoryItem?.product,
                        availableItem.binLocation,
                        warehouse
                )

                if (quantityRequested == 0 || (inventoryLevel && inventoryLevel.maxQuantity > quantityAvailable)) {
                    break
                }

                // The quantity to pick is either the quantity available (if less than requested) or
                // the quantity requested (if less than available).
                int quantityPicked = (quantityRequested >= availableItem.quantityAvailable) ?
                        availableItem.quantityAvailable : quantityRequested

                log.info "Suggested quantity ${quantityPicked}"
                suggestedItems << new SuggestedItem(inventoryItem: availableItem?.inventoryItem,
                        binLocation: availableItem?.binLocation,
                        quantityAvailable: availableItem?.quantityAvailable,
                        quantityRequested: quantityRequested,
                        quantityPicked: quantityPicked)
                quantityRequested -= quantityPicked
            }
        }
        return suggestedItems
    }

    List<AvailableItem> getAvailableItems(Location location, OrderItem orderItem) {
        List<AvailableItem> availableItems = productAvailabilityService.getAllAvailableBinLocations(location, orderItem.product)
        def picklistItems = getPicklistItems(orderItem)

        availableItems = availableItems.findAll { it.quantityOnHand > 0 && it.inventoryItem != orderItem.inventoryItem && it.binLocation != orderItem.destinationBinLocation }
        availableItems = calculateQuantityAvailableToPromise(availableItems, picklistItems)

        return availableItems
    }

    Set<PicklistItem> getPicklistItems(OrderItem orderItem) {
        Picklist picklist = orderItem?.order?.picklist

        if (picklist) {
            return picklist.picklistItems.findAll {
                it.orderItem == orderItem
            }
        }

        return []
    }

    List<AvailableItem> calculateQuantityAvailableToPromise(List<AvailableItem> availableItems, def picklistItems) {
        for (PicklistItem picklistItem : picklistItems) {
            AvailableItem availableItem = availableItems.find {
                it.inventoryItem == picklistItem.inventoryItem && it.binLocation == picklistItem.binLocation
            }

            if (!availableItem) {
                availableItem = new AvailableItem(
                        inventoryItem: picklistItem.inventoryItem,
                        binLocation: picklistItem.binLocation,
                        quantityAvailable: 0,
                        quantityOnHand: 0
                )

                availableItems.add(availableItem)
            } else {
                availableItem.quantityAvailable += picklistItem.quantity
            }
        }

        return productAvailabilityService.sortAvailableItems(availableItems)
    }

    void updatePicklist(OrderItem orderItem, List picklistItems) {

        clearPicklist(orderItem)

        picklistItems.each { picklistItemMap ->

            PicklistItem picklistItem = picklistItemMap["id"] ?
                    PicklistItem.get(picklistItemMap["id"]) : null

            InventoryItem inventoryItem = picklistItemMap["inventoryItem.id"] ?
                    InventoryItem.get(picklistItemMap["inventoryItem.id"]) : null

            Location binLocation = picklistItemMap["binLocation.id"] ?
                    Location.get(picklistItemMap["binLocation.id"]) : null

            BigDecimal quantityPicked = (picklistItemMap.quantityPicked != null && picklistItemMap.quantityPicked != "") ?
                    new BigDecimal(picklistItemMap.quantityPicked) : null

            createOrUpdatePicklistItem(orderItem, picklistItem, inventoryItem, binLocation,
                    quantityPicked?.intValueExact())
        }
    }

    InventoryLevel getInventoryLevelForItem(Product product, Location internalLocation, Location warehouse) {
        return InventoryLevel.createCriteria().get {
            eq("product", product)
            eq("internalLocation", internalLocation)
            eq("inventory", warehouse.inventory)
            order("lastUpdated", "desc")
        }
    }
}
