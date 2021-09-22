/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.replenishment

import org.apache.commons.beanutils.BeanUtils
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.Replenishment
import org.pih.warehouse.api.ReplenishmentItem
import org.pih.warehouse.api.ReplenishmentPickPageItem
import org.pih.warehouse.api.ReplenishmentStatus
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryLevelStatus
import org.pih.warehouse.inventory.Requirement
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product

class ReplenishmentService {

    def locationService
    def inventoryService
    def productAvailabilityService
    def grailsApplication

    boolean transactional = true

    def getRequirements(Location location, InventoryLevelStatus inventoryLevelStatus) {
        def requirements = Requirement.createCriteria().list() {
            eq("location", location)
            eq("status", inventoryLevelStatus)
        }

        return requirements?.unique {[it.product, it.location, it.binLocation]}?.sort { a, b ->
            a.product.productCode <=> b.product.productCode ?:
                    a.binLocation.zone.name <=> b.binLocation.zone.name ?:
                            a.binLocation.name <=> b.binLocation.name ?:
                                    a.quantityInBin <=> b.quantityInBin
        }
    }

    Order createOrUpdateOrderFromReplenishment(Replenishment replenishment) {

        Order order = Order.get(replenishment.id)
        if (!order) {
            order = new Order()
        }

        OrderType orderType = OrderType.findByCode(OrderTypeCode.TRANSFER_ORDER.name())
        order.orderType = orderType
        order.status = OrderStatus.valueOf(replenishment.status.toString())
        if (!order.orderNumber) {
            order.orderNumber = replenishment.replenishmentNumber
        }
        order.orderedBy = replenishment.orderedBy
        order.dateOrdered = new Date()
        order.origin = replenishment.origin
        order.destination = replenishment.destination

        // Set auditing data on completion
        if (replenishment.status == ReplenishmentStatus.COMPLETED) {
            order.completedBy = replenishment.orderedBy
            order.dateCompleted = new Date()
        }

        // Generate name
        order.name = order.name ?: order.generateName()

        replenishment.replenishmentItems.toArray().each { ReplenishmentItem replenishmentItem ->

            OrderItem orderItem
            if (replenishmentItem.id) {
                orderItem = order.orderItems?.find { it.id == replenishmentItem.id }
            }

            if (!orderItem) {
                orderItem = new OrderItem()
                order.addToOrderItems(orderItem)
            }

            updateOrderItem(replenishmentItem, orderItem)
        }

        order.save(failOnError: true)
        return order
    }

    Order deleteReplenishmentItem(String id) {
        OrderItem orderItem = OrderItem.get(id)
        if (!orderItem) {
            throw new IllegalArgumentException("No replenishment item found with ID ${id}")
        }

        List<PicklistItem> picklistItems = PicklistItem.findAllByOrderItem(orderItem)
        if (picklistItems) {
            picklistItems.each { PicklistItem picklistItem ->
                picklistItem.disableRefresh = Boolean.TRUE
                picklistItem.picklist?.removeFromPicklistItems(picklistItem)
                picklistItem.orderItem?.removeFromPicklistItems(picklistItem)
                picklistItem.delete()
            }
        }

        Order order = orderItem.order
        order.removeFromOrderItems(orderItem)
        orderItem.delete()

        return order
    }

    OrderItem updateOrderItem(ReplenishmentItem replenishmentItem, OrderItem orderItem) {
        OrderItemStatusCode orderItemStatusCode = orderItem?.picklistItems ? OrderItemStatusCode.COMPLETED : OrderItemStatusCode.PENDING
        orderItem.orderItemStatusCode = orderItemStatusCode
        orderItem.product = replenishmentItem.product
        orderItem.inventoryItem = replenishmentItem.inventoryItem
        orderItem.quantity = replenishmentItem.quantity
        orderItem.originBinLocation = replenishmentItem.replenishmentLocation
        orderItem.destinationBinLocation = replenishmentItem.binLocation
        return orderItem
    }

    Order completeReplenishment(Replenishment replenishment) {
        validateReplenishment(replenishment)

        // Save the replenishment as an order
        Order order = createOrUpdateOrderFromReplenishment(replenishment)

        // Process pick list items
        processPicklistItems(replenishment)

        replenishment.replenishmentItems.each { ReplenishmentItem replenishmentItem ->
            TransferStockCommand command = new TransferStockCommand()
            command.location = replenishmentItem?.binLocation?.parentLocation?:replenishment?.origin
            command.binLocation = replenishmentItem.replenishmentLocation // origin
            command.inventoryItem = replenishmentItem.inventoryItem
            command.quantity = replenishmentItem.quantity
            command.otherLocation = replenishmentItem.location
            command.otherBinLocation = replenishmentItem.binLocation // destination
            command.order = order
            command.transferOut = Boolean.TRUE
            inventoryService.transferStock(command)
        }

        return order
    }

    void processPicklistItems(Replenishment replenishment) {
        replenishment.replenishmentItems.toArray().each { ReplenishmentItem oldReplenishmentItem ->

            if (oldReplenishmentItem.picklistItems) {
                // Iterate over picklist items and create new replenishment items for them
                // NOTE: The only fields we change from the ReplenishmentItem are the replenishment destination bin and quantity.
                oldReplenishmentItem.picklistItems.each { ReplenishmentItem picklistItem ->
                    ReplenishmentItem newReplenishmentItem = new ReplenishmentItem()
                    BeanUtils.copyProperties(newReplenishmentItem, oldReplenishmentItem)
                    newReplenishmentItem.quantity = picklistItem.quantity
                    newReplenishmentItem.location = picklistItem.location
                    newReplenishmentItem.binLocation = picklistItem.binLocation
                    replenishment.replenishmentItems.add(newReplenishmentItem)
                }

                // Remove the original replenishment item since it was replaced with the above
                replenishment.replenishmentItems.remove(oldReplenishmentItem)
            }
        }
    }

    void validateReplenishment(Replenishment replenishment) {
        replenishment.replenishmentItems.toArray().each { ReplenishmentItem replenishmentItem ->
            validateReplenishmentItem(replenishmentItem)
        }
    }

    void validateReplenishmentItem(ReplenishmentItem replenishmentItem) {
        def quantity = replenishmentItem.quantity

        if (replenishmentItem.picklistItems) {
            quantity = replenishmentItem.picklistItems.sum { it.quantity }
        }

        validateQuantityAvailable(replenishmentItem.replenishmentLocation, replenishmentItem.inventoryItem, quantity)
    }

    void validateQuantityAvailable(Location replenishmentLocation, InventoryItem inventoryItem, BigDecimal quantity) {

        if (!replenishmentLocation) {
            throw new IllegalArgumentException("Location is required")
        }

        Integer quantityAvailable = productAvailabilityService.getQuantityOnHandInBinLocation(inventoryItem, replenishmentLocation)
        log.info "Quantity: ${quantity} vs ${quantityAvailable}"

        if (quantityAvailable < 0) {
            throw new IllegalStateException("The inventory item is no longer available at the specified location ${replenishmentLocation}")
        }

        if (quantity > quantityAvailable) {
            throw new IllegalStateException("Quantity available ${quantityAvailable} is less than quantity to replenish ${quantity} for product ${inventoryItem.product.productCode} ${inventoryItem.product.name}")
        }
    }

    def fillCalculatedData(Replenishment replenishment) {
        replenishment?.replenishmentItems?.each { ReplenishmentItem replenishmentItem ->
            // Refresh Quantity On Hand
            def quantityInBin = productAvailabilityService.getQuantityOnHandInBinLocation(replenishmentItem.inventoryItem, replenishmentItem.binLocation) ?:0
            replenishmentItem.quantityInBin = quantityInBin
            replenishmentItem.totalQuantityOnHand = productAvailabilityService.getQuantityOnHand(replenishmentItem.product, replenishmentItem.location)
            def inventoryLevel = InventoryLevel.findByProductAndInternalLocation(replenishmentItem.product,replenishmentItem.binLocation)
            replenishmentItem.minQuantity = inventoryLevel.minQuantity?:0
            replenishmentItem.maxQuantity = inventoryLevel.maxQuantity?:0
            replenishmentItem.quantityNeeded = (inventoryLevel.maxQuantity?:0) - quantityInBin > 0 ?
                (inventoryLevel.maxQuantity?:0) - quantityInBin : 0

            // Get Picklist related data
            OrderItem orderItem = OrderItem.get(replenishmentItem.id)
            replenishmentItem.picklistItems = getPicklistItems(orderItem)
            replenishmentItem.availableItems = getAvailableItems(replenishmentItem.location, orderItem)
            replenishmentItem.suggestedItems = getSuggestedItems(replenishmentItem.availableItems, replenishmentItem.quantity, replenishment.origin)
        }}

    ReplenishmentPickPageItem getPicklist(OrderItem orderItem) {

        if (!orderItem.picklistItems || (orderItem.picklistItems && orderItem.totalQuantityPicked() != orderItem.quantity &&
                !orderItem.picklistItems.reasonCode)) {
            createPicklist(orderItem)
        }
        ReplenishmentPickPageItem pickPageItem = new ReplenishmentPickPageItem(orderItem: orderItem,
                picklistItems: orderItem.picklistItems)
        Location location = orderItem?.order?.origin

        List<AvailableItem> availableItems = getAvailableItems(location, orderItem)
        Integer quantityRequired = orderItem?.quantity
        List<SuggestedItem> suggestedItems = getSuggestedItems(availableItems, quantityRequired, orderItem?.order?.destination)
        pickPageItem.availableItems = availableItems
        pickPageItem.suggestedItems = suggestedItems

        return pickPageItem
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
        List<AvailableItem> autoPickableItems = availableItems?.findAll { it.quantityAvailable > 0 && it.autoPickable }

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
                        quantityOnHand: picklistItem.quantity
                )

                availableItems.add(availableItem)
            }

            availableItem.quantityAvailable += picklistItem.quantity
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
