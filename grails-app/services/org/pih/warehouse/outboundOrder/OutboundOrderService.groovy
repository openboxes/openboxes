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
            item.allocationStatus = RequisitionItem.get(item.id).getAllocationStatus()
            item.quantityAllocated = RequisitionItem.get(item.id).calculateQuantityAllocated()
        }

        return outboundOrder
    }

    void allocate(StockMovementItem orderItem, Map data = [:]) {
        AllocationType mode = data.mode as AllocationType
        if (!mode) {
            throw new IllegalStateException("mode parameter not provided")
        }

        if (mode == AllocationType.AUTO) {
            stockMovementService.createPicklist(orderItem, false)
        } else if (mode == AllocationType.MANUAL) {
            List<Allocation> allocations = data.allocations as List<Allocation>
            allocations.each { allocation ->
                stockMovementService.createOrUpdatePicklistItem(
                        orderItem.requisitionItem,
                        null,
                        InventoryItem.load(allocation.inventoryItemId),
                        Location.load(allocation.binLocationId),
                        0,
                        null,
                        null,
                        true,
                        allocation.quantity
                )
            }
        } else {
            throw new UnsupportedOperationException("Unsupported mode: $mode")
        }
    }
}

class Allocation {
    String inventoryItemId
    String binLocationId
    Integer quantity
}

enum AllocationType {
    AUTO, MANUAL
}