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
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product

import javax.xml.bind.ValidationException

class ReplenishmentService {

    def locationService
    def inventoryService
    def productAvailabilityService
    def picklistService
    def grailsApplication

    boolean transactional = true

    def getRequirements(Location location, InventoryLevelStatus inventoryLevelStatus) {
        def requirements = Requirement.createCriteria().list() {
            if (inventoryLevelStatus == InventoryLevelStatus.BELOW_MAXIMUM) {
                'in'("status", inventoryLevelStatus.listReplenishmentOptions())
            } else {
                eq("status", inventoryLevelStatus)
            }
            eq("location", location)
            isNotNull("binLocation")
            gt("quantityAvailable", 0)
        }

        return requirements?.unique {[it.product, it.location, it.binLocation]}?.sort { a, b ->
            a.binLocation?.zone?.name?.toLowerCase() <=> b.binLocation?.zone?.name?.toLowerCase() ?:
                a.binLocation?.name?.toLowerCase() <=> b.binLocation?.name?.toLowerCase() ?:
                    a.product.name?.toLowerCase() <=> b.product.name?.toLowerCase()
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

        if (replenishment.status <= ReplenishmentStatus.PENDING) {
            validateRequirement(replenishment)
        } else {
            validateReplenishment(order)
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

        // Save the replenishment as an order
        Order order = createOrUpdateOrderFromReplenishment(replenishment)

        order?.picklist?.picklistItems?.each { PicklistItem picklistItem ->
            TransferStockCommand command = new TransferStockCommand()
            command.location = order?.origin
            command.binLocation = picklistItem?.binLocation // origin
            command.inventoryItem = picklistItem?.inventoryItem
            command.quantity = picklistItem?.quantity
            command.otherLocation = order?.origin
            command.otherBinLocation = picklistItem?.orderItem?.destinationBinLocation // destination
            command.order = order
            command.transferOut = Boolean.TRUE
            inventoryService.transferStock(command)
        }

        return order
    }

    void validateRequirement(Replenishment replenishment) {
        replenishment.replenishmentItems.toArray().each { ReplenishmentItem replenishmentItem ->
            validateRequirement(replenishment.origin, replenishmentItem)
        }
    }

    void validateRequirement(Location replenishingLocation, ReplenishmentItem item) {
        def qtyAvailable = productAvailabilityService.getQuantityAvailableToPromiseByProductNotInBin(replenishingLocation, item.binLocation, item.product)
        if (item.quantity > qtyAvailable) {
            throw new ValidationException("There is not available that quantity of the product with id: ${item.product.id}")
        }
    }

    void validateReplenishment(Order order) {
        order.orderItems.each { OrderItem orderItem ->
            validateReplenishmentItem(order.origin, orderItem)
        }
    }

    void validateReplenishmentItem(Location replenishingLocation, OrderItem orderItem) {
        if (orderItem.picklistItems) {
            orderItem.picklistItems.toArray().each { PicklistItem picklistItem ->
                validateQuantityAvailable(replenishingLocation, picklistItem, picklistItem.quantity)
            }
        }
    }

    void validateQuantityAvailable(Location replenishingLocation, PicklistItem picklistItem, Integer quantityPicked) {
        if (!replenishingLocation) {
            throw new IllegalArgumentException("Location is required")
        }

        Integer quantityAvailable = productAvailabilityService.getQuantityAvailableToPromise(
            replenishingLocation,
            picklistItem.binLocation,
            picklistItem.inventoryItem,
        )

        Integer quantityAvailableWithPicked = quantityAvailable + quantityPicked >= 0 ? quantityAvailable + quantityPicked : 0
        log.info "Quantity: ${quantityPicked} vs ${quantityAvailableWithPicked}"

        def product = picklistItem.inventoryItem.product
        if (quantityPicked > quantityAvailableWithPicked) {
            throw new IllegalStateException("Quantity available ${quantityAvailableWithPicked} is less than quantity to replenish ${quantityPicked} for product ${product.productCode} ${product.name}")
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
            replenishmentItem.picklistItems = picklistService.getPicklistItems(orderItem).findAll{ it.quantity > 0 }
            replenishmentItem.availableItems = picklistService.getAvailableItems(replenishmentItem.location, orderItem)
            replenishmentItem.suggestedItems = picklistService.getSuggestedItems(replenishmentItem.availableItems, replenishmentItem.quantity, replenishment.origin)
        }}

    ReplenishmentPickPageItem getPicklist(OrderItem orderItem) {

        if (!orderItem.picklistItems || (orderItem.picklistItems && orderItem.totalQuantityPicked() != orderItem.quantity &&
                !orderItem.picklistItems.reasonCode)) {
            picklistService.createPicklist(orderItem)
        }
        ReplenishmentPickPageItem pickPageItem = new ReplenishmentPickPageItem(orderItem: orderItem,
                picklistItems: orderItem.picklistItems)
        Location location = orderItem?.order?.origin

        List<AvailableItem> availableItems = picklistService.getAvailableItems(location, orderItem)
        Integer quantityRequired = orderItem?.quantity
        List<SuggestedItem> suggestedItems = picklistService.getSuggestedItems(availableItems, quantityRequired, orderItem?.order?.destination)
        pickPageItem.availableItems = availableItems
        pickPageItem.suggestedItems = suggestedItems

        return pickPageItem
    }
}
