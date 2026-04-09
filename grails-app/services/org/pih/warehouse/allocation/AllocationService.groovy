package org.pih.warehouse.allocation

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus

@Transactional
class AllocationService {

    StockMovementService stockMovementService
    ProductAvailabilityService productAvailabilityService
    GrailsApplication grailsApplication

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

    AllocationDetailsDto allocate(String requisitionItemId, AllocationMode mode, List<AllocationDto> allocations, List<AllocationStrategy> strategies = []) {
        RequisitionItem requisitionItem = RequisitionItem.get(requisitionItemId)
        if (!requisitionItem) {
            throw new IllegalArgumentException("Requisition item not found")
        }

        if (mode == AllocationMode.AUTO) {
            Integer quantityRequired = requisitionItem.calculateQuantityRequired()
            List<SuggestedItem> suggestedItems = getAutoSuggestedItems(requisitionItem, quantityRequired, strategies)

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
            suggestedItems = getAutoSuggestedItems(requisitionItem, quantityRequired, request.allocationStrategies)
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
            requisition.status = RequisitionStatus.VERIFYING
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
            List<AllocationStrategy> allocationStrategyList = list
            request = new AllocationRequest(quantityRequired: quantityRequired, requisitionItem: requisitionItem, allocationMode: allocationMode, allocationStrategies: allocationStrategyList)
        } else if (allocationMode == AllocationMode.MANUAL) {
            List<AvailableItem> allocationItemList = list
            request = new AllocationRequest(quantityRequired: quantityRequired, requisitionItem: requisitionItem, allocationMode: allocationMode, availableItems: allocationItemList)
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $allocationMode")
        }
        return allocate(request)
    }

    List<AllocationResult> allocate(Requisition requisition, AllocationMode allocationMode, List<AllocationStrategy> allocationStrategyList) {
        List<AllocationResult> results = []
        requisition?.requisitionItems?.each { requisitionItem ->
            AllocationRequest allocationRequest = new AllocationRequest(requisitionItem: requisitionItem, allocationMode: allocationMode, allocationStrategies: allocationStrategyList)
            AllocationResult singleResult = allocate(allocationRequest)
            results.add(singleResult)
        }
        return results
    }

    private List<SuggestedItem> getAutoSuggestedItems(RequisitionItem requisitionItem, Integer quantityRequired, List<AllocationStrategy> strategies, List<AvailableItem> excludeList = []) {
        Location location = requisitionItem.requisition.origin
        List<AvailableItem> allAvailableItems = stockMovementService.getAvailableItems(location, requisitionItem, false)
        List<AvailableItem> filteredItems = applyStrategies(location, allAvailableItems, strategies)
        List<AvailableItem> includedItems = filteredItems.findAll { !excludeList.contains(it) }

        boolean isBackordered = requisitionItem.isBackordered()
        if (isBackordered) {
            quantityRequired = requisitionItem.quantityBackordered
        }
        Integer quantityAvailable = includedItems.sum { it.quantityAvailable } ?: 0
        if (quantityAvailable < quantityRequired) {
            boolean partialAllocationAllowed = requisitionItem.requisition.partialAllocationAllowed
            if (isBackordered && partialAllocationAllowed) {
                return []
            }
            throw new IllegalArgumentException("Insufficient stock. Required: ${quantityRequired}, Available: ${quantityAvailable}")
        }

        return stockMovementService.getSuggestedItems(includedItems, quantityRequired)
    }

    private List<AvailableItem> applyStrategies(Location facility, List<AvailableItem> availableItems, List<AllocationStrategy> strategies) {
        if (!strategies || strategies.isEmpty()) {
            strategies = grailsApplication.config.openboxes.order.allocation.strategies
        }
        if (!strategies || strategies.isEmpty()) {
            return availableItems
        }

        List<AvailableItem> displayItems = availableItems.findAll { it.binLocation?.isDisplay() }
        List<AvailableItem> warehouseItems = availableItems.findAll { !it.binLocation?.isDisplay() }
        Set<Location> preferredBinLocations = getPreferredBinLocations(facility, warehouseItems?.find()?.inventoryItem?.product)
        List<AvailableItem> preferredItems = warehouseItems?.findAll {preferredBinLocations.contains(it.binLocation) }
        List<AvailableItem> remainingItems = (warehouseItems?: []) - (preferredItems?: [])
        List<AvailableItem> result = []

        strategies.each { strategy ->
            switch (strategy) {
                case AllocationStrategy.DISPLAY_FIRST:
                    result.addAll(displayItems)
                    result.addAll(preferredItems)
                    result.addAll(remainingItems)
                    break

                case AllocationStrategy.WAREHOUSE_FIRST:
                    result.addAll(preferredItems)
                    result.addAll(remainingItems)
                    result.addAll(displayItems)
                    break

                case AllocationStrategy.WAREHOUSE_ONLY:
                    result.addAll(preferredItems)
                    result.addAll(remainingItems)
                    break

                case AllocationStrategy.FEFO:
                    result = productAvailabilityService.sortAvailableItems(result)
                    break
            }
        }

        return result
    }

    private Set<Location> getPreferredBinLocations(Location facility, Product product) {
        if (!product) {
            return []
        }

        Set<InventoryLevel> inventoryLevels = facility?.inventory?.configuredProducts?.findAll { it.product == product && it.preferredBinLocation != null }
        return inventoryLevels?.collect { it.preferredBinLocation }
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
