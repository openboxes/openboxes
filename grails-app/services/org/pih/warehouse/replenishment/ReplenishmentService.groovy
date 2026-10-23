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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.Replenishment
import org.pih.warehouse.api.ReplenishmentItem
import org.pih.warehouse.api.ReplenishmentPickPageItem
import org.pih.warehouse.api.ReplenishmentStatus
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.identification.IdentifierGeneratorContext
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryLevelStatus
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.Requirement
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.picklist.PicklistService
import org.pih.warehouse.product.Product

import javax.xml.bind.ValidationException

@Transactional
class ReplenishmentService {

    GrailsApplication grailsApplication
    AuthService authService
    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    PicklistService picklistService
    OrderIdentifierService orderIdentifierService

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

    /**
     * Creates a new replenishment list for the bins of the facility that support any of the target activity
     * codes and have fallen below their minimum level. Each line is filled up to the bin's maximum level and
     * allocated from bins that are not target locations themselves.
     *
     * @return the created replenishment order, or null if there was nothing to replenish
     */
    Order generateAutomaticReplenishment(Location facility, List<ActivityCode> targetActivityCodes) {
        if (!facility) {
            throw new IllegalArgumentException("Location is required")
        }
        if (!targetActivityCodes) {
            throw new IllegalArgumentException("At least one target activity code is required")
        }

        ActivityCode[] activityCodes = targetActivityCodes as ActivityCode[]
        List<ReplenishmentItem> replenishmentItems = getAutomaticReplenishmentItems(facility, activityCodes)
        if (!replenishmentItems) {
            log.info "No bins to replenish automatically in ${facility.name} for activities ${targetActivityCodes}"
            return null
        }

        Replenishment replenishment = new Replenishment(
                origin: facility,
                destination: facility,
                orderedBy: authService.systemUser,
                status: ReplenishmentStatus.PENDING,
                replenishmentNumber: generateReplenishmentNumber(),
                description: Constants.AUTOMATIC_REPLENISHMENT_DESCRIPTION,
                replenishmentItems: replenishmentItems,
        )

        Order order = createOrUpdateOrderFromReplenishment(replenishment)

        // Do not take stock from one target location to fill another
        picklistService.createPicklist(order) { AvailableItem availableItem ->
            !isTargetLocation(availableItem.binLocation, activityCodes)
        }

        log.info "Generated automatic replenishment ${order.orderNumber} with ${replenishmentItems.size()} items in ${facility.name}"
        return order
    }

    List<ReplenishmentItem> getAutomaticReplenishmentItems(Location facility, ActivityCode[] targetActivityCodes) {
        List<Requirement> requirements = getRequirements(facility, InventoryLevelStatus.BELOW_MINIMUM)
        Set<String> binsOnOpenReplenishments = getBinsOnOpenReplenishments(facility)

        return requirements.findAll { Requirement requirement ->
            isTargetLocation(requirement.binLocation, targetActivityCodes) &&
                    !binsOnOpenReplenishments.contains(getProductBinKey(requirement.product?.id, requirement.binLocation?.id))
        }.collect { Requirement requirement ->
            Integer quantityPickable = getQuantityPickable(facility, requirement.product, requirement.binLocation, targetActivityCodes)
            new ReplenishmentItem(
                    product: requirement.product,
                    location: facility,
                    binLocation: requirement.binLocation,
                    quantity: Math.min(requirement.quantityNeeded as Integer, quantityPickable),
            )
        }.findAll { ReplenishmentItem replenishmentItem -> replenishmentItem.quantity > 0 }
    }

    /**
     * Returns the product and destination bin pairs (see getProductBinKey) that are already on replenishments
     * of the facility that have not been completed or canceled yet.
     */
    Set<String> getBinsOnOpenReplenishments(Location facility) {
        List<Object[]> rows = OrderItem.executeQuery("""
            select oi.product.id, oi.destinationBinLocation.id
            from OrderItem oi
            where oi.order.orderType.code = :orderTypeCode
            and oi.order.origin = :facility
            and oi.order.status in (:statuses)
            and oi.destinationBinLocation is not null
            and (oi.orderItemStatusCode is null or oi.orderItemStatusCode <> :canceled)
        """, [
                orderTypeCode: OrderTypeCode.TRANSFER_ORDER.name(),
                facility     : facility,
                statuses     : [OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.PLACED],
                canceled     : OrderItemStatusCode.CANCELED,
        ])
        return rows.collect { Object[] row -> getProductBinKey(row[0] as String, row[1] as String) } as Set<String>
    }

    /**
     * Returns the quantity that can be automatically picked to replenish the bin, excluding stock that is
     * in the bin itself or in other target locations.
     */
    Integer getQuantityPickable(Location facility, Product product, Location binLocation, ActivityCode[] targetActivityCodes) {
        List<AvailableItem> availableItems = productAvailabilityService.getAvailableBinLocations(facility, product?.id)
        BigDecimal quantityPickable = availableItems.findAll { AvailableItem availableItem ->
            availableItem.binLocation != binLocation &&
                    availableItem.pickable &&
                    availableItem.quantityAvailable > 0 &&
                    !isTargetLocation(availableItem.binLocation, targetActivityCodes)
        }.sum { AvailableItem availableItem -> availableItem.quantityAvailable } as BigDecimal
        return quantityPickable?.intValue() ?: 0
    }

    String generateReplenishmentNumber() {
        String prefix = grailsApplication.config.getProperty(
                "openboxes.stockTransfer.binReplenishment.prefix", String, Constants.REPLENISHMENT_PREFIX)
        String replenishmentNumber = orderIdentifierService.generate(null,
                IdentifierGeneratorContext.builder().prefix(prefix).build())
        if (!replenishmentNumber) {
            throw new IllegalStateException("Unable to generate a unique replenishment number")
        }
        return replenishmentNumber
    }

    private static boolean isTargetLocation(Location binLocation, ActivityCode[] targetActivityCodes) {
        return binLocation?.supportsAny(targetActivityCodes) ?: false
    }

    private static String getProductBinKey(String productId, String binLocationId) {
        return "${productId}:${binLocationId}"
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
        if (replenishment.description) {
            order.description = replenishment.description
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
        if (order.hasErrors() || !order.save(failOnError: true)) {
            throw new ValidationException("Invalid order", order.errors)
        }
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
        ) ?: 0

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

    List<ActivityCode> getTargetActivityCodes() {
        return grailsApplication.config.getProperty(
                "openboxes.jobs.automaticReplenishmentJob.targetActivityCodes", List, [])
                .findResults { String activityCode ->
                    try {
                        return ActivityCode.valueOf(activityCode)
                    } catch (IllegalArgumentException e) {
                        log.warn("Unrecognized target activity code for automaticReplenishmentJob '${activityCode}'. Ignoring.")
                    }
                }
    }
}
