package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.requisition.RequisitionItem

@Transactional
class AllocationService {

    StockMovementService stockMovementService

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
                        id: pickListItem.id,
                        inventoryItemId: pickListItem.inventoryItem?.id,
                        binLocationId: pickListItem.binLocation?.id,
                        quantity: pickListItem.quantity
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
            autoAllocate(requisitionItem, strategies)
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
                    picklistItem = existingPickListItems.find { it.id == pickListItemId}
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

    private void autoAllocate(RequisitionItem requisitionItem, List<AllocationStrategy> strategies) {
        Location location = requisitionItem.requisition.origin
        List<AvailableItem> allAvailableItems = stockMovementService.getAvailableItems(location, requisitionItem, false)
        List<AvailableItem> filteredItems = applyStrategies(allAvailableItems, strategies)

        Integer quantityRequired = requisitionItem.calculateQuantityRequired()
        Integer quantityAvailable = filteredItems.sum { it.quantityAvailable } ?: 0
        if (quantityAvailable < quantityRequired) {
            throw new IllegalArgumentException("Insufficient stock. Required: ${quantityRequired}, Available: ${quantityAvailable}")
        }

        List<SuggestedItem> suggestedItems = stockMovementService.getSuggestedItems(filteredItems, quantityRequired)
        suggestedItems.each { it.quantityPicked = 0 }

        stockMovementService.clearPicklist(requisitionItem)
        stockMovementService.allocateSuggestedItems(requisitionItem, suggestedItems, true)
    }

    private List<AvailableItem> applyStrategies(List<AvailableItem> items, List<AllocationStrategy> strategies) {
        if (!strategies || strategies.isEmpty()) {
            return items
        }

        List<AvailableItem> result = new ArrayList<>(items)

        strategies.each { strategy ->
            switch (strategy) {
                case AllocationStrategy.DISPLAY_FIRST:
                    result = result.findAll { it.binLocation?.isDisplay() }
                    break

                case AllocationStrategy.WAREHOUSE_FIRST:
                    result = result.findAll { !it.binLocation?.isDisplay() }
                    break
            }
        }

        return result
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
