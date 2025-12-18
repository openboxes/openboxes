package org.pih.warehouse.outboundOrder

import grails.gorm.transactions.Transactional
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.requisition.RequisitionItem

@Transactional
class OutboundOrderService {

    StockMovementService stockMovementService

    @Transactional(readOnly = true)
    StockMovement get(String id) {
        StockMovement outboundOrder = stockMovementService.getStockMovement(id)

        if (!outboundOrder) {
            throw new IllegalArgumentException("No outbound order found for id ${params.id}")
        }

        outboundOrder.lineItems.each { StockMovementItem item ->
            item.availableItems = stockMovementService.getAvailableItems(outboundOrder.origin, RequisitionItem.get(item.id), false)
        }

        return outboundOrder
    }

    void allocate(StockMovementItem orderItem, Map data = [:]) {
        String mode = data.mode as AllocationType
        if (!mode) {
            throw new IllegalStateException("mode parameter not provided")
        }

        if (mode.equalsIgnoreCase(AllocationType.AUTO.name())) {
            stockMovementService.createPicklist(orderItem, false)
        } else if (mode.equalsIgnoreCase(AllocationType.MANUAL.name())) {
            List<ItemToAllocate> allocations = data.allocations as List<ItemToAllocate>
            allocations.each { itemToAllocate ->
                stockMovementService.createOrUpdatePicklistItem(
                        orderItem.requisitionItem,
                        null,
                        InventoryItem.get(itemToAllocate.inventoryItemId),
                        Location.get(itemToAllocate.binLocationId),
                        0,
                        null,
                        null,
                        true,
                        itemToAllocate.quantity
                )
            }
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $mode")
        }
    }
}

class ItemToAllocate {
    InventoryItem inventoryItemId
    Location binLocationId
    Integer quantity
}

enum AllocationType {
    AUTO, MANUAL
}