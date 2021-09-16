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
import org.pih.warehouse.api.Replenishment
import org.pih.warehouse.api.ReplenishmentItem
import org.pih.warehouse.api.ReplenishmentStatus
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

class ReplenishmentService {

    def locationService
    def inventoryService
    def productAvailabilityService
    def grailsApplication

    boolean transactional = true

    def getRequirements(Location location, InventoryLevelStatus inventoryLevelStatus) {
        return Requirement.createCriteria().list() {
            eq("location", location)
            eq("status", inventoryLevelStatus)
            product {
                order("productCode", "asc")
                order("name", "asc")
            }
            binLocation {
                order("zone", "asc")
                order("name", "asc")
            }
            order("quantityInBin", "asc")
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

            // TODO: OBAM-169: if new order item, then create a picklist from suggested items
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

    def refreshQuantities(Replenishment replenishment) {
        replenishment?.replenishmentItems?.each { ReplenishmentItem replenishmentItem ->
            replenishmentItem.quantityInBin = productAvailabilityService.getQuantityOnHandInBinLocation(replenishmentItem.inventoryItem, replenishmentItem.binLocation)
            replenishmentItem.totalQuantityOnHand = productAvailabilityService.getQuantityOnHand(replenishmentItem.product, replenishmentItem.location)
            def inventoryLevel = InventoryLevel.findByProductAndInternalLocation(replenishmentItem.product,replenishmentItem.binLocation)
            replenishmentItem.minQuantity = inventoryLevel.minQuantity
            replenishmentItem.maxQuantity = inventoryLevel.maxQuantity
        }
    }
}
