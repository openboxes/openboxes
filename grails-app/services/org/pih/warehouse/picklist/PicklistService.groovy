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
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class PicklistService {

    ProductAvailabilityService productAvailabilityService
    StockMovementService stockMovementService

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
        def binLocations = []

        if (picklist) {
            picklist.picklistItems.findAll {
                it.orderItem == orderItem
            }.toArray().each {
                it.disableRefresh = Boolean.TRUE
                picklist.removeFromPicklistItems(it)
                orderItem.removeFromPicklistItems(it)
                binLocations.add(it.binLocation?.id)
                it.delete()
            }
            picklist.save(flush: true)
        }

        productAvailabilityService.refreshProductsAvailability(orderItem?.order?.origin?.id, [orderItem?.product?.id], binLocations?.unique(), false)
    }

    // It expects to receive a picklist id
    void clearPicklist(String id) {
        Picklist picklist = Picklist.get(id)

        if (!picklist) {
            throw new ObjectNotFoundException(id, Picklist.class.toString())
        }
        // Store bin locations' id for product availability refresh
        List<String> binLocations = []

        Set<PicklistItem> picklistItems = picklist.picklistItems
        List<PicklistItem> itemsToRemove = []
        picklistItems.each { PicklistItem picklistItem ->
            picklistItem.disableRefresh = true
            itemsToRemove.add(picklistItem)
            binLocations.add(picklistItem.binLocation?.id)
        }
        itemsToRemove.each {
            picklist.removeFromPicklistItems(it)
            it.requisitionItem.autoAllocated = null
            it.delete(flush: true)
        }


        List<String> productIds = picklist?.requisition?.requisitionItems?.collect { it.product?.id }?.unique()
        productAvailabilityService.refreshProductsAvailability(
                picklist?.requisition?.origin?.id,
                productIds,
                binLocations.unique(),
                false)
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
            picklist.save(flush: true)
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

        productAvailabilityService.refreshProductsAvailability(orderItem?.order?.origin?.id, [inventoryItem?.product?.id], [binLocation?.id], false)
    }

    void updatePicklistItem(String picklistItemId, String productId, BigDecimal quantityPicked, String pickerId, String reasonCode) {
        PicklistItem picklistItem = PicklistItem.get(picklistItemId)

        Product product = Product.get(productId)
        if (product != picklistItem?.requisitionItem?.product) {
            picklistItem.errors.reject("Picked product ${product?.productCode} does not match picklist item ${picklistItem?.requisitionItem?.product?.productCode}")
        }

        // Initialize quantity picked
        if (!picklistItem.quantityPicked) {
            picklistItem.quantityPicked = 0
        }

        if (quantityPicked > picklistItem?.quantityRemaining) {
            picklistItem.errors.rejectValue("quantityPicked","Quantity picked (${quantityPicked}) cannot exceed quantity remaining (${picklistItem?.quantityRemaining})")
        }

        picklistItem.quantityPicked += quantityPicked
        picklistItem.datePicked = new Date()
        picklistItem.picker = User.load(pickerId)

        // Used to mark a pick item as shorted
        if (reasonCode) {
            picklistItem.reasonCode = reasonCode
        }

        // Save picklist item
        if (picklistItem.hasErrors() || !picklistItem.save(flush:true)) {
            throw new ValidationException("Unable to save picklist item", picklistItem.errors)
        }

        // Checking whether we need to perform automatic reallocation after shortage
        if (picklistItem.shortage) {
            Location currentLocation = picklistItem?.requisitionItem?.requisition?.origin
            if (currentLocation?.supports(ActivityCode.PICKING_STRATEGY_AUTOMATIC_REALLOCATION)) {
                log.info "Automatically generate new picklist item for shortage: ${picklistItem?.toJson()}"
                stockMovementService.createPicklist(picklistItem, Boolean.TRUE)
            }
        }

        // Check if the picklist has been completed
        String requisitionId = picklistItem?.picklist?.requisition?.id
        if (requisitionId) {
            // TODO: To be implemented if needed
            // grailsApplication.mainContext.publishEvent(new RefreshPicklistStatusEvent(requisitionId))
        }
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
        List<AvailableItem> availableItems = productAvailabilityService.getAvailableBinLocations(location, orderItem.product?.id)
        def picklistItems = getPicklistItems(orderItem)

        availableItems = availableItems.findAll { it.inventoryItem != orderItem.inventoryItem && it.binLocation != orderItem.destinationBinLocation }
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

    void revertPick(String requisitionItemId) {
        RequisitionItem requisitionItem = RequisitionItem.get(requisitionItemId)

        if (!requisitionItem) {
            throw new ObjectNotFoundException(requisitionItemId, RequisitionItem.class.toString())
        }

        Set<PicklistItem> picklistItemsToRemove = []
        List<String> binLocations = []

        requisitionItem?.picklistItems?.each { PicklistItem picklistItem ->
            picklistItem.disableRefresh = true
            picklistItemsToRemove.add(picklistItem)
            binLocations.add(picklistItem.binLocation?.id)
        }

        picklistItemsToRemove.each {
            it.disableRefresh = true
            it.picklist?.removeFromPicklistItems(it)
            requisitionItem?.removeFromPicklistItems(it)
            it.delete(flush: true)
        }

        productAvailabilityService.refreshProductsAvailability(
                requisitionItem?.requisition?.origin?.id,
                [requisitionItem?.product?.id],
                binLocations,
                false
        )
    }
}
