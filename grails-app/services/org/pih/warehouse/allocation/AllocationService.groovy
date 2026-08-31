/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.allocation

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.CycleCountService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionService
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class AllocationService {

    StockMovementService stockMovementService
    GrailsApplication grailsApplication
    AuthService authService
    RequisitionService requisitionService
    ProductAvailabilityService productAvailabilityService
    AllocationFallbackService allocationFallbackService
    CycleCountService cycleCountService
    InventoryService inventoryService

    AllocationSourceStrategyHandlerResolver allocationSourceStrategyHandlerResolver = new AllocationSourceStrategyHandlerResolver()
    RotationStrategyResolver rotationStrategyResolver = new RotationStrategyResolver()

    @Transactional(readOnly = true)
    StockMovement getOutboundOrder(String id) {
        StockMovement outboundOrder = stockMovementService.getStockMovement(id)

        if (!outboundOrder) {
            throw new IllegalArgumentException("No outbound order found for id ${id}")
        }

        outboundOrder.lineItems.each { StockMovementItem item ->
            RequisitionItem requisitionItem = RequisitionItem.get(item.id)
            item.availableItems = stockMovementService.getAvailableItems(outboundOrder.origin, requisitionItem, false)

            def picklistItems = PicklistItem.findAllByRequisitionItem(requisitionItem)
            item.allocations = picklistItems.collect { pickListItem ->
                [
                        id             : pickListItem.id,
                        inventoryItemId: pickListItem.inventoryItem?.id,
                        binLocationId  : pickListItem.binLocation?.id,
                        quantity       : pickListItem.quantity
                ]
            }

            item.allocationStatus = requisitionItem.getAllocationStatus()
            item.quantityAllocated = requisitionItem.calculateQuantityAllocated()
        }

        return outboundOrder
    }

    AllocationDetailsDto allocate(String requisitionItemId, AllocationMode mode, List<AllocationDto> allocations, List<AllocationSourceStrategy> strategies = []) {
        RequisitionItem requisitionItem = RequisitionItem.get(requisitionItemId)
        if (!requisitionItem) {
            throw new IllegalArgumentException("Requisition item not found")
        }

        if (mode == AllocationMode.AUTO) {
            Integer quantityRequired = requisitionItem.calculateQuantityRequired()
            List<SuggestedItem> suggestedItems = getAutoSuggestedItems(requisitionItem, quantityRequired, strategies, [], mode)

            stockMovementService.clearPicklist(requisitionItem)
            stockMovementService.allocateSuggestedItems(requisitionItem, suggestedItems, true)
        } else if (mode == AllocationMode.MANUAL) {
            List<PicklistItem> existingPickListItems = PicklistItem.findAllByRequisitionItem(requisitionItem)
            Set<String> processedPickIds = []
            allocations.each { allocation ->
                String pickListItemId = allocation.id
                String inventoryItemId = allocation.inventoryItemId
                String binLocationId = allocation.binLocationId
                Integer newQuantity = allocation.quantity

                PicklistItem picklistItem = null
                if (pickListItemId) {
                    picklistItem = existingPickListItems.find { it.id == pickListItemId }
                }

                if (!picklistItem) {
                    picklistItem = existingPickListItems.find {
                        it.inventoryItem.id == inventoryItemId &&
                                it.binLocation?.id == binLocationId
                    }
                }

                if (newQuantity > 0) {
                    stockMovementService.createOrUpdatePicklistItem(
                            requisitionItem,
                            picklistItem,
                            InventoryItem.load(inventoryItemId),
                            Location.load(binLocationId),
                            0,
                            null,
                            null,
                            true,
                            newQuantity
                    )
                }

                if (picklistItem) {
                    processedPickIds.add(picklistItem.id)
                }
            }

            existingPickListItems.each { existing ->
                if (!processedPickIds.contains(existing.id)) {
                    existing.picklist.removeFromPicklistItems(existing)
                    existing.delete()
                }
            }
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $mode")
        }

        return buildAllocationDetailsDto(requisitionItem)
    }

    AllocationResult allocate(AllocationRequest request, boolean saveAllocation = true) {
        AllocationMode mode = request.allocationMode
        RequisitionItem requisitionItem = request.requisitionItem
        Integer quantityRequired = request.quantityRequired ?: requisitionItem.calculateQuantityRequired()
        List<SuggestedItem> suggestedItems
        if (mode == AllocationMode.AUTO) {
            suggestedItems = getAutoSuggestedItems(requisitionItem, quantityRequired, request.allocationStrategies, [], mode)
        } else if (mode == AllocationMode.MANUAL) {
            List<AvailableItem> manualItems = request.availableItems?.findAll { it.inventoryItem.product?.id == requisitionItem.product?.id }
            suggestedItems = stockMovementService.getSuggestedItems(manualItems, quantityRequired)
            Integer quantitySuggested = suggestedItems.sum { it.quantityAvailable } ?: 0
            if (quantitySuggested < quantityRequired) {
                List<SuggestedItem> remainingItems = getAutoSuggestedItems(requisitionItem, quantityRequired - quantitySuggested, null, suggestedItems, mode)
                suggestedItems.addAll(remainingItems)
            }
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $mode")
        }

        if (saveAllocation) {
            stockMovementService.clearPicklist(requisitionItem)
            stockMovementService.allocateSuggestedItems(requisitionItem, suggestedItems, mode == AllocationMode.AUTO)
        }
        return new AllocationResult(allocationRequest: request, suggestedItems: suggestedItems)
    }

    Boolean deallocate(Requisition requisition) {
        validateNothingPicked(requisition)

        boolean result = true
        requisition.requisitionItems.each { requisitionItem ->
            result &= deallocate(requisitionItem)
        }
        if (result) {
            // Set requisition status back to VERIFYING
            requisition.status = RequisitionStatus.CREATED
            requisition.save()
        }
        return result
    }

    Boolean deallocate(RequisitionItem requisitionItem) {
        validateNothingPicked(requisitionItem)
        stockMovementService.clearPicklist(requisitionItem)
        requisitionItem.autoAllocated = null
        return true
    }

    void validateNothingPicked(Requisition requisition) {
        if (requisition?.picklist?.picklistItems?.any {it.status == "PICKED" || it.status == "STAGED"} ) {
            throw new ValidationException("Requisition has picked items")
        }
    }

    void validateNothingPicked(RequisitionItem requisitionItem) {
        if (requisitionItem?.picklistItems?.any {it.status == "PICKED" || it.status == "STAGED"} ) {
            throw new ValidationException("Requisition has picked items")
        }
    }

    AllocationResult allocate(RequisitionItem requisitionItem, Integer quantityRequired, AllocationMode allocationMode, List list) {
        AllocationRequest request
        if (allocationMode == AllocationMode.AUTO) {
            List<AllocationSourceStrategy> allocationSourceStrategyList = list
            request = new AllocationRequest(quantityRequired: quantityRequired, requisitionItem: requisitionItem, allocationMode: allocationMode, allocationStrategies: allocationSourceStrategyList)
        } else if (allocationMode == AllocationMode.MANUAL) {
            List<AvailableItem> allocationItemList = list
            request = new AllocationRequest(quantityRequired: quantityRequired, requisitionItem: requisitionItem, allocationMode: allocationMode, availableItems: allocationItemList)
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $allocationMode")
        }
        return allocate(request)
    }

    List<AllocationResult> allocate(Requisition requisition, AllocationMode allocationMode, List<AllocationSourceStrategy> allocationSourceStrategyList) {
        try {
            List<AllocationResult> results = requisition?.requisitionItems?.collect { requisitionItem ->
                AllocationRequest allocationRequest = new AllocationRequest(requisitionItem: requisitionItem, allocationMode: allocationMode, allocationStrategies: allocationSourceStrategyList)
                allocate(allocationRequest, false)
            } ?: []

            results.each { AllocationResult result ->
                RequisitionItem requisitionItem = result.allocationRequest.requisitionItem
                stockMovementService.clearPicklist(requisitionItem)
                stockMovementService.allocateSuggestedItems(requisitionItem, result.suggestedItems, allocationMode == AllocationMode.AUTO)
            }
            return results
        } catch (Exception e) {
            String message = "${Constants.ALLOCATION_FAILED} ${e.message ?: 'Unknown error'}"
            // TODO: For testing purposes leaving both Comment and EventLog in place.
            requisitionService.logRequisitionComment(requisition?.id, message)
            requisitionService.logRequisitionEvent(requisition?.id, message)
            throw e
        }
    }

    void allocateRequisition(String requisitionId) {
        try {
            authService.withSystemUser {
                Requisition requisition = Requisition.get(requisitionId)
                if (!requisition) {
                    log.warn("Requisition ${requisitionId} not found, skipping")
                    return
                }

                if (!requisition.isEligibleForAutomaticAllocation()) {
                    log.debug("Requisition ${requisitionId} is not eligible for automatic allocation, skipping")
                    return
                }

                log.info("Automatic allocation for requisition ${requisition.requestNumber} (${requisition.id}) ...")
                allocate(requisition, AllocationMode.AUTO, [])

                if (requisition.autoIssuanceRequested) {
                    try {
                        stockMovementService.issueRequisition(requisition)
                        // TODO this is sync refresh as a temporary workaround for async refresh after transaction creation
                        //  it should be implemented in better way, ticket for it - OBLS-937
                        productAvailabilityService.refreshProductsAvailability(
                                requisition.origin?.id, requisition.requisitionItems*.product*.id, false)
                    } catch (Exception e) {
                        requisitionService.logRequisitionEvent(requisition.id, "${Constants.ISSUANCE_FAILED} ${e.message ?: 'Unknown error'}")
                        throw e
                    }
                } else {
                    stockMovementService.updateRequisitionStatus(requisitionId, RequisitionStatus.PICKING)
                }
            }
        } catch (Exception e) {
            log.error("Error processing requisition ${requisitionId}", e)
        }
    }

    private List<SuggestedItem> getAutoSuggestedItems(RequisitionItem requisitionItem, Integer quantityRequired, List<AllocationSourceStrategy> strategies, List<AvailableItem> excludeList = [], AllocationMode allocationMode = null) {
        Location facility = requisitionItem.requisition.origin
        Product product = requisitionItem.product
        List<AvailableItem> allAvailableItems = stockMovementService.getAvailableItems(facility, requisitionItem, false)

        allAvailableItems = allAvailableItems.findAll { !it.binLocation?.isNegativeInventoryFallbackLocation() }

        boolean isBackordered = requisitionItem.isBackordered()
        if (isBackordered) {
            quantityRequired = requisitionItem.quantityBackordered
        }

        List<AllocationSourceStrategy> resolvedStrategies = resolveStrategies(requisitionItem.requisition, strategies)
        RotationRule rotationRule = getConfiguredRotationRule()

        Integer bestQuantityAvailable = 0
        List<AvailableItem> bestItems = []
        AllocationSourceStrategy bestStrategy = null
        for (AllocationSourceStrategy strategy : resolvedStrategies) {
            List<AvailableItem> ordered = orderByStrategy(strategy, facility, product, applyRotation(rotationRule, allAvailableItems))
            List<AvailableItem> includedItems = ordered.findAll {
                !excludeList.contains(it) && it.quantityAvailable > 0 && it.pickable
            }
            Integer quantityAvailable = includedItems.sum { it.quantityAvailable } ?: 0
            if (bestStrategy == null || quantityAvailable > bestQuantityAvailable) {
                bestQuantityAvailable = quantityAvailable
                bestItems = includedItems
                bestStrategy = strategy
            }
            if (canSatisfy(includedItems, quantityRequired)) {
                return stockMovementService.getSuggestedItems(includedItems, quantityRequired)
            }
        }

        boolean partialAllocationAllowed = requisitionItem.requisition.partialAllocationAllowed
        if (isBackordered && partialAllocationAllowed) {
            return []
        }

        // No location holds enough, so rather than abandon the order we take whatever stock exists and record
        // the rest against a location permitted to go negative or the facility's fallback location
        if (isFallbackApplicable(allocationMode, facility)) {
            List<SuggestedItem> fallbackItems =
                    getFallbackSuggestedItems(requisitionItem, quantityRequired, bestStrategy, bestItems)
            if (fallbackItems != null) {
                return fallbackItems
            }
        }

        throw new IllegalArgumentException("Insufficient stock for product ${product?.productCode} - ${product?.name} in order ${requisitionItem.requisition?.requestNumber}. Required quantity: ${quantityRequired}, Available quantity: ${bestQuantityAvailable}")
    }

    /**
     * Checks if fallback allocation approach is allowed. It should be applicable for auto allocations and
     * for facilities that allow it
     */
    private static boolean isFallbackApplicable(AllocationMode allocationMode, Location facility) {
        if (allocationMode != AllocationMode.AUTO) {
            return false
        }

        return facility?.isNegativeInventoryEnabled()
    }

    /**
     * Picks whatever real stock exists first and only sends the remainder to a fallback location
     */
    private List<SuggestedItem> getFallbackSuggestedItems(RequisitionItem requisitionItem, Integer quantityRequired,
                                                          AllocationSourceStrategy strategy,
                                                          List<AvailableItem> bestItems) {
        Location facility = requisitionItem.requisition.origin
        Product product = requisitionItem.product

        List<SuggestedItem> suggestedItemsFromStock = stockMovementService.getSuggestedItems(bestItems, quantityRequired)
        Integer quantityFromStock = suggestedItemsFromStock.sum { it.quantityPicked } ?: 0
        Integer quantityShortfall = quantityRequired - quantityFromStock

        // Defensive only: every strategy already failed canSatisfy, so the shortfall is always positive here.
        if (quantityShortfall <= 0) {
            return suggestedItemsFromStock
        }

        AllocationFallbackResolution resolution = allocationFallbackService.resolve(facility, product, strategy)
        if (!resolution) {
            return null
        }

        log.warn("Allocation fallback for product ${product?.productCode} in order " +
                "${requisitionItem.requisition?.requestNumber}: ${resolution}, quantity ${quantityShortfall}")

        // Below steps leave a balance that no longer matches the bin, and a count is the only thing that
        // ever corrects it
        cycleCountService.getOrCreateCycleCountRequest(facility, product)

        String message
        if (resolution.step == AllocationStep.NEGATIVE_INVENTORY) {
            message = "Negative inventory: allocated quantity ${quantityShortfall} of product " +
                    "${product?.productCode} - ${product?.name} to ${resolution.binLocation?.name}, which is " +
                    "permitted to hold a negative quantity. Cycle count requested."
        } else {
            message = "Inventory shortfall: no location at ${facility?.name} could supply quantity " +
                    "${quantityShortfall} of product ${product?.productCode} - ${product?.name}. " +
                    "Allocated to ${resolution.binLocation?.name}. Cycle count requested."
        }

        requisitionService.addSystemComment(requisitionItem.requisition, message)
        requisitionService.addSystemEventLog(requisitionItem.requisition, message)

        InventoryItem inventoryItem = inventoryService.findOrCreateDefaultInventoryItem(product)

        // Every suggested item becomes its own picklist row, so a line already picking the same lot from the
        // same bin has to absorb the shortfall instead of gaining a duplicate alongside it.
        SuggestedItem existingItem = suggestedItemsFromStock.find {
            it.inventoryItem?.id == inventoryItem?.id && it.binLocation?.id == resolution.binLocation?.id
        }
        if (existingItem) {
            existingItem.quantityPicked = (existingItem.quantityPicked ?: 0) + quantityShortfall
            return suggestedItemsFromStock
        }

        SuggestedItem shortfallItem = new SuggestedItem(
                inventoryItem: inventoryItem,
                binLocation: resolution.binLocation,
                quantityAvailable: 0,
                quantityOnHand: 0,
                quantityRequested: quantityShortfall,
                quantityPicked: quantityShortfall
        )

        return suggestedItemsFromStock + [shortfallItem]
    }

    private List<AllocationSourceStrategy> resolveStrategies(Requisition requisition, List<AllocationSourceStrategy> explicit) {
        if (explicit) {
            return explicit
        }
        if (requisition?.allocationSourceStrategy) {
            return [requisition.allocationSourceStrategy]
        }
        return [getConfiguredSourceStrategy()]
    }

    private AllocationSourceStrategy getConfiguredSourceStrategy() {
        return (grailsApplication.config.openboxes.order.allocation.source ?: AllocationSourceStrategy.STORAGE_FIRST) as AllocationSourceStrategy
    }

    private RotationRule getConfiguredRotationRule() {
        return (grailsApplication.config.openboxes.order.allocation.rotation ?: RotationRule.FEFO) as RotationRule
    }

    private List<AvailableItem> orderByStrategy(AllocationSourceStrategy strategy, Location facility, Product product, List<AvailableItem> availableItems) {
        AllocationSourceStrategyHandler handler = allocationSourceStrategyHandlerResolver.handlerFor(strategy)
        if (handler) {
            return handler.orderAvailableItems(facility, product, availableItems)
        }

        log.warn("No allocation source strategy handler registered for ${strategy}, using natural order")
        return availableItems
    }

    private List<AvailableItem> applyRotation(RotationRule rotationRule, List<AvailableItem> sourceOrdered) {
        return rotationStrategyResolver.forRule(rotationRule).sort(sourceOrdered)
    }

    private static boolean canSatisfy(List<AvailableItem> orderedItems, Integer quantityRequired) {
        Integer quantityAvailable = orderedItems.sum { it.quantityAvailable } ?: 0
        return quantityAvailable >= quantityRequired
    }

    private AllocationDetailsDto buildAllocationDetailsDto(RequisitionItem requisitionItem) {
        List<PicklistItem> picklistItems = PicklistItem.findAllByRequisitionItem(requisitionItem)
        List<AllocationDto> allocationDtos = picklistItems.collect { picklistItem ->
            new AllocationDto(
                    id: picklistItem.id,
                    inventoryItemId: picklistItem.inventoryItem?.id,
                    binLocationId: picklistItem.binLocation?.id,
                    quantity: picklistItem.quantity
            )
        }

        Integer quantityAllocated = requisitionItem.calculateQuantityAllocated()

        return new AllocationDetailsDto(
                requisitionItemId: requisitionItem.id,
                quantityRequired: requisitionItem.quantity,
                quantityAllocated: quantityAllocated,
                quantityRemaining: Math.max(0, requisitionItem.quantity - quantityAllocated),
                status: requisitionItem.getAllocationStatus(),
                allocations: allocationDtos
        )
    }
}
