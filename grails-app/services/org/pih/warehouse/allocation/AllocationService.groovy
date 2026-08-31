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
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
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
            List<SuggestedItem> suggestedItems = getAutoSuggestedItems(requisitionItem, quantityRequired, strategies)

            // No suggestion means allocation declined this line rather than failed to find stock - a
            // backordered line is left to the cross-dock release. Clearing the picklist would delete
            // whatever that release has already covered.
            if (suggestedItems) {
                stockMovementService.clearPicklist(requisitionItem)
                stockMovementService.allocateSuggestedItems(requisitionItem, suggestedItems, true)
            }
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
            suggestedItems = getAutoSuggestedItems(requisitionItem, quantityRequired,
                    request.allocationStrategies, [], request.crossDockRelease)
        } else if (mode == AllocationMode.MANUAL) {
            List<AvailableItem> manualItems = request.availableItems?.findAll { it.inventoryItem.product?.id == requisitionItem.product?.id }
            suggestedItems = stockMovementService.getSuggestedItems(manualItems, quantityRequired)
            Integer quantitySuggested = suggestedItems.sum { it.quantityAvailable } ?: 0
            if (quantitySuggested < quantityRequired) {
                List<SuggestedItem> remainingItems = getAutoSuggestedItems(requisitionItem, quantityRequired - quantitySuggested, null, suggestedItems)
                suggestedItems.addAll(remainingItems)
            }
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $mode")
        }

        if (saveAllocation) {
            // The cross-dock release only ever allocates the quantity still outstanding, so the
            // picklist is added to rather than rebuilt - clearing it would drop what an earlier
            // delivery already covered.
            if (!request.crossDockRelease) {
                stockMovementService.clearPicklist(requisitionItem)
            }
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
                if (!result.suggestedItems) {
                    return
                }
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

                // FIXME: Temporary allocation attempt check until we have a better attempt count solution for
                //  automatic allocation failures (a windowed count after https://openboxes.atlassian.net/browse/OBLS-929)
                Integer maxAllocationAttempts = grailsApplication.config.openboxes.jobs.automaticAllocationJob.maxAttempts ?: 3
                if (requisition.allocationAttemptCount >= maxAllocationAttempts) {
                    requisitionService.logRequisitionComment(requisition.id,
                            "Max attempts (${maxAllocationAttempts}) at automatic allocation for " +
                                "requisition ${requisition.requestNumber} reached, skipping")
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

    private List<SuggestedItem> getAutoSuggestedItems(RequisitionItem requisitionItem, Integer quantityRequired, List<AllocationSourceStrategy> strategies, List<AvailableItem> excludeList = [], Boolean crossDockRelease = false) {
        Location facility = requisitionItem.requisition.origin
        Product product = requisitionItem.product
        List<AvailableItem> allAvailableItems =
                stockMovementService.getAvailableItems(facility, requisitionItem, false, !crossDockRelease)

        boolean isBackordered = requisitionItem.isBackordered()
        if (isBackordered) {
            quantityRequired = requisitionItem.quantityBackordered
        }

        // A backordered line waits for its cross-dock delivery and is not covered from ordinary stock.
        // Until the cross-dock putaway has run there is nothing to allocate
        if (isBackordered && !crossDockRelease) {
            log.info("Requisition item ${requisitionItem.id} is backordered, skipping ordinary allocation")
            return []
        }

        List<AllocationSourceStrategy> resolvedStrategies = resolveStrategies(requisitionItem.requisition, strategies)
        RotationRule rotationRule = getConfiguredRotationRule()

        // Cross-dock release: take the cross-dock zone first and fall back to ordinary stock only for
        // the quantity no inbound covered
        if (crossDockRelease) {
            List<AvailableItem> ordered = crossDockFirst(orderByStrategy(
                    resolvedStrategies.first(), facility, product,
                    applyRotation(rotationRule, allAvailableItems)))
            return stockMovementService.getSuggestedItems(ordered, quantityRequired)
        }

        Integer bestQuantityAvailable = 0
        for (AllocationSourceStrategy strategy : resolvedStrategies) {
            List<AvailableItem> ordered = orderByStrategy(strategy, facility, product, applyRotation(rotationRule, allAvailableItems))
            List<AvailableItem> includedItems = ordered.findAll { !excludeList.contains(it) }
            Integer quantityAvailable = includedItems.sum { it.quantityAvailable } ?: 0
            bestQuantityAvailable = Math.max(bestQuantityAvailable, quantityAvailable)
            if (canSatisfy(includedItems, quantityRequired)) {
                return stockMovementService.getSuggestedItems(includedItems, quantityRequired)
            }
        }

        boolean partialAllocationAllowed = requisitionItem.requisition.partialAllocationAllowed
        if (isBackordered && partialAllocationAllowed) {
            return []
        }

        // TODO fallback order when nothing can supply the quantity

        throw new IllegalArgumentException("Insufficient stock for product ${product?.productCode} - ${product?.name} in order ${requisitionItem.requisition?.requestNumber}. Required quantity: ${quantityRequired}, Available quantity: ${bestQuantityAvailable}")
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

    private List<AvailableItem> crossDockFirst(List<AvailableItem> availableItems) {
        List<AvailableItem> crossDockItems = availableItems.findAll {
            it.binLocation?.supports(ActivityCode.CROSS_DOCKING)
        }
        return crossDockItems + (availableItems - crossDockItems)
    }

    private List<AvailableItem> orderByStrategy(AllocationSourceStrategy strategy, Location facility, Product product, List<AvailableItem> availableItems) {
        AllocationSourceStrategyHandler handler = allocationSourceStrategyHandlerResolver.handlerFor(strategy)
        if (handler) {
            return handler.order(facility, product, availableItems)
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
