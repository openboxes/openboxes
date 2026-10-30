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
import org.pih.warehouse.inventory.ProductAndBinKey
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
        List<Requirement> requirements = getRequirementsToReplenish(facility, activityCodes)
        if (!requirements) {
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
                replenishmentItems: requirements.collect { Requirement requirement ->
                    new ReplenishmentItem(
                            product: requirement.product,
                            location: facility,
                            binLocation: requirement.binLocation,
                            // Provisional, set to what can be picked when allocating
                            quantity: requirement.quantityNeeded as Integer,
                    )
                },
        )

        Order order = createOrUpdateOrderFromReplenishment(replenishment)
        allocateAutomaticReplenishment(order, requirements, activityCodes)

        // Nobody reviews the picks of an automatic replenishment, so it must only ask for stock that was allocated
        if (!order.orderItems.any { OrderItem orderItem -> getQuantityAllocated(orderItem) > 0 }) {
            log.warn "No stock could be allocated for automatic replenishment ${order.orderNumber} in ${facility.name}, it was not created"
            order.delete(flush: true)
            return null
        }
        removeUnallocatedQuantities(order)

        log.info "Generated automatic replenishment ${order.orderNumber} with ${order.orderItems.size()} items in ${facility.name}"
        return order
    }

    /**
     * Drops the lines that got no picks and reduces the quantity of partially allocated lines to what was allocated.
     * PicklistService#getSuggestedItems can stop before allocating the whole quantity requested.
     */
    private void removeUnallocatedQuantities(Order order) {
        order.orderItems.toList().each { OrderItem orderItem ->
            Integer quantityAllocated = getQuantityAllocated(orderItem)
            if (quantityAllocated == 0) {
                log.warn "No stock could be allocated to replenish ${orderItem.product?.productCode} in ${orderItem.destinationBinLocation?.name}, " +
                        "dropping it from automatic replenishment ${order.orderNumber}"
                deleteReplenishmentItem(orderItem.id)
            } else if (quantityAllocated < orderItem.quantity) {
                log.warn "Only ${quantityAllocated} of ${orderItem.quantity} could be allocated to replenish ${orderItem.product?.productCode} " +
                        "in ${orderItem.destinationBinLocation?.name}, reducing it in automatic replenishment ${order.orderNumber}"
                orderItem.quantity = quantityAllocated
            }
        }
    }

    private static Integer getQuantityAllocated(OrderItem orderItem) {
        return orderItem.picklistItems?.sum { PicklistItem picklistItem -> picklistItem.quantity } ?: 0
    }

    /**
     * Returns the requirements of the facility's target bins that are below their minimum level, need stock and are
     * not already on an open replenishment.
     */
    List<Requirement> getRequirementsToReplenish(Location facility, ActivityCode[] targetActivityCodes) {
        Set<ProductAndBinKey> locationsWithOpenReplenishments = getLocationsWithOpenReplenishments(facility)
        return getRequirements(facility, InventoryLevelStatus.BELOW_MINIMUM).findAll { Requirement requirement ->
            isTargetLocation(requirement.binLocation, targetActivityCodes) &&
                    !locationsWithOpenReplenishments.contains(new ProductAndBinKey(requirement.product?.id, requirement.binLocation?.id)) &&
                    (requirement.quantityNeeded as Integer) > 0
        }
    }

    /**
     * Sets the quantity of each line to what can be picked for it and creates its picks, from stock outside target
     * locations. Bins of the same product share that stock, so the emptiest bins are allocated first and each line
     * only sees what the lines before it left.
     */
    private void allocateAutomaticReplenishment(Order order, List<Requirement> requirements, ActivityCode[] targetActivityCodes) {
        Map<String, List<AvailableItem>> sourceItemsByProduct = getSourceItemsByProduct(
                order.origin, requirements.collect { it.product?.id }.unique(), targetActivityCodes)
        Map<ProductAndBinKey, OrderItem> orderItemsByProductAndBin = order.orderItems.collectEntries { OrderItem orderItem ->
            [(new ProductAndBinKey(orderItem.product?.id, orderItem.destinationBinLocation?.id)): orderItem]
        }

        // Stable sort, equally full bins keep their zone and bin order
        requirements.sort(false) { Requirement a, Requirement b -> getFillRatio(a) <=> getFillRatio(b) }.each { Requirement requirement ->
            OrderItem orderItem = orderItemsByProductAndBin[new ProductAndBinKey(requirement.product?.id, requirement.binLocation?.id)]
            List<AvailableItem> sourceItems = sourceItemsByProduct[requirement.product?.id] ?: []

            // The quantity and the picks come from the same available items, so they can't disagree
            List<AvailableItem> availableItems = picklistService.filterAvailableItems(sourceItems, orderItem)
            BigDecimal quantityPickable = picklistService.getAutoPickableItems(availableItems)
                    .sum { AvailableItem availableItem -> availableItem.quantityAvailable } as BigDecimal
            orderItem.quantity = Math.min(requirement.quantityNeeded as Integer, quantityPickable?.intValue() ?: 0)

            picklistService.createPicklist(orderItem, availableItems).each { SuggestedItem suggestedItem ->
                AvailableItem sourceItem = sourceItems.find { AvailableItem availableItem ->
                    availableItem.inventoryItem == suggestedItem.inventoryItem && availableItem.binLocation == suggestedItem.binLocation
                }
                if (sourceItem) {
                    sourceItem.quantityAvailable -= suggestedItem.quantityPicked
                }
            }
        }
    }

    /**
     * Returns, by product id, the stock that target locations can be replenished from: everything outside target
     * locations, fetched in a single query.
     */
    private Map<String, List<AvailableItem>> getSourceItemsByProduct(Location facility, List<String> productIds, ActivityCode[] targetActivityCodes) {
        // An empty list would return the availability of every product in the facility
        if (!productIds) {
            return [:]
        }

        return productAvailabilityService.getAvailableBinLocations(facility, productIds).findAll { AvailableItem availableItem ->
            !isTargetLocation(availableItem.binLocation, targetActivityCodes)
        }.groupBy { AvailableItem availableItem ->
            availableItem.inventoryItem?.product?.id
        } as Map<String, List<AvailableItem>>
    }

    /**
     * Returns the product and destination bin pairs that are already on replenishments of the facility that have not
     * been completed or canceled yet.
     *
     * Only replenishment lists (manual or automatic) count, told apart from regular stock transfers (also
     * TRANSFER_ORDER) by the replenishment number prefix: a pending stock transfer into a bin does not stop it
     * from being replenished. Open replenishments numbered with a previously configured prefix are not recognized.
     */
    Set<ProductAndBinKey> getLocationsWithOpenReplenishments(Location facility) {
        List<ProductAndBinKey> productAndBinKeys = OrderItem.executeQuery("""
            select new org.pih.warehouse.inventory.ProductAndBinKey(oi.product.id, oi.destinationBinLocation.id)
            from OrderItem oi
            where oi.order.orderType.code = :orderTypeCode
            and oi.order.origin = :facility
            and oi.order.status in (:statuses)
            and oi.order.orderNumber like :replenishmentNumberPattern
            and oi.destinationBinLocation is not null
            and (oi.orderItemStatusCode is null or oi.orderItemStatusCode <> :canceled)
        """, [
                orderTypeCode             : OrderTypeCode.TRANSFER_ORDER.name(),
                facility                  : facility,
                statuses                  : [OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.PLACED],
                // The prefix is not escaped, it must not contain the LIKE wildcards % or _
                replenishmentNumberPattern: replenishmentNumberPrefix + '%',
                canceled                  : OrderItemStatusCode.CANCELED,
        ])
        return productAndBinKeys as Set<ProductAndBinKey>
    }

    String getReplenishmentNumberPrefix() {
        return grailsApplication.config.getProperty(
                "openboxes.stockTransfer.binReplenishment.prefix", String, Constants.REPLENISHMENT_PREFIX)
    }

    String generateReplenishmentNumber() {
        String replenishmentNumber = orderIdentifierService.generate(null,
                IdentifierGeneratorContext.builder().prefix(replenishmentNumberPrefix).build())
        if (!replenishmentNumber) {
            throw new IllegalStateException("Unable to generate a unique replenishment number")
        }
        return replenishmentNumber
    }

    private static boolean isTargetLocation(Location binLocation, ActivityCode[] targetActivityCodes) {
        return binLocation?.supportsAny(targetActivityCodes) ?: false
    }

    /**
     * How full the bin is relative to its minimum level, lowest for the bins closest to running empty.
     */
    private static BigDecimal getFillRatio(Requirement requirement) {
        Integer quantityInBin = requirement.quantityInBin ?: 0
        Integer minQuantity = requirement.minQuantity ?: 0
        return minQuantity > 0 ? quantityInBin / minQuantity : quantityInBin as BigDecimal
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
        return parseActivityCodes(grailsApplication.config.getProperty(
                "openboxes.jobs.automaticReplenishmentJob.targetActivityCodes", List, []), "configuration")
    }

    /**
     * Converts the given activity code names, skipping (with a warning) the ones that are not recognized.
     */
    List<ActivityCode> parseActivityCodes(List<String> activityCodes, String source) {
        return (activityCodes ?: []).findResults { String activityCode ->
            try {
                return ActivityCode.valueOf(activityCode)
            } catch (IllegalArgumentException e) {
                log.warn("Unrecognized target activity code for automaticReplenishmentJob '${activityCode}' in ${source}. Ignoring.")
            }
        }
    }
}
