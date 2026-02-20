package org.pih.warehouse.api

import org.pih.warehouse.api.PickTaskStatus

/**
 * Resolves the FulfillmentSummaryStatus based on stock movement line items.
 * This logic is specific to Outbound Stock Movements to track the order lifecycle.
 */
class FulfillmentStatusResolver {

    static FulfillmentSummaryStatus resolve(List<StockMovementItem> items) {
        if (!items) {
            return FulfillmentSummaryStatus.CREATED
        }

        // Pre-calculate shipped quantities for efficiency
        def shippedQuantities = items.collectEntries { [it.id, getQuantityShipped(it)] }

        // 1. CANCELLED - All items are fully canceled
        if (items.every { isCanceled(it) }) {
            return FulfillmentSummaryStatus.CANCELLED
        }

        // 2. ISSUED - All items are either fully canceled or fully issued
        if (items.every { isCanceled(it) || isFullyIssued(it, shippedQuantities[it.id]) }) {
            return FulfillmentSummaryStatus.ISSUED
        }

        // 3. PARTIALLY_ISSUED - Some items are issued but not all
        if (items.any { isPartiallyIssued(it, shippedQuantities[it.id]) }) {
            return FulfillmentSummaryStatus.PARTIALLY_ISSUED
        }

        // 4. BACK_ORDER - Some items shipped but others are unallocated
        boolean anyShipped = items.any { shippedQuantities[it.id] > 0 }
        boolean anyUnallocatedActive = items.any {
            !isCanceled(it) && !isFullyIssued(it, shippedQuantities[it.id]) && !hasAllocation(it)
        }
        if (anyShipped && anyUnallocatedActive) {
            return FulfillmentSummaryStatus.BACK_ORDER
        }

        // 5. STAGED - All items are either fully canceled or staged
        if (items.every { isCanceled(it) || isStaged(it) }) {
            return FulfillmentSummaryStatus.STAGED
        }

        // 6. PARTIALLY_STAGED - Some items are staged but not all
        if (items.any { isPartiallyStaged(it) }) {
            return FulfillmentSummaryStatus.PARTIALLY_STAGED
        }

        // 7. PICKED - All items are either fully canceled or fully picked
        if (items.every { isCanceled(it) || isFullyPicked(it) }) {
            return FulfillmentSummaryStatus.PICKED
        }

        // 8. PICKING - Any item is picked OR all items have allocation
        boolean anyPicked = items.any { !isCanceled(it) && isPicked(it) }
        boolean allHaveAllocation = items.every { isCanceled(it) || hasAllocation(it) }
        if (anyPicked || allHaveAllocation) {
            return FulfillmentSummaryStatus.PICKING
        }

        // 9. PARTIALLY_ALLOCATED - Some items have allocation but not all
        if (items.any { hasAllocation(it) }) {
            return FulfillmentSummaryStatus.PARTIALLY_ALLOCATED
        }

        // 10. CREATED - Default state
        return FulfillmentSummaryStatus.CREATED
    }

    // Helpers

    private static boolean isCanceled(StockMovementItem item) {
        return item.getQuantityRequired() <= 0
    }

    private static boolean isFullyIssued(StockMovementItem item, BigDecimal shipped) {
        return !isCanceled(item) && (shipped ?: 0) >= item.getQuantityRequired()
    }

    private static boolean isPartiallyIssued(StockMovementItem item, BigDecimal shipped) {
        if (isCanceled(item) || isFullyIssued(item, shipped)) {
            return false
        }
        BigDecimal qty = shipped ?: 0
        return qty > 0 && qty < item.getQuantityRequired()
    }

    private static boolean isFullyPicked(StockMovementItem item) {
        return !isCanceled(item) && (item.quantityPicked ?: 0) >= item.getQuantityRequired()
    }

    private static boolean isPicked(StockMovementItem item) {
        BigDecimal picked = item.quantityPicked ?: 0
        return !isCanceled(item) && picked > 0 && picked <= item.getQuantityRequired()
    }

    private static boolean isStaged(StockMovementItem item) {
        if (isCanceled(item)) {
            return false
        }
        def picklistItems = item.requisitionItem?.retrievePicklistItems()
        if (!picklistItems) {
            return false
        }
        return picklistItems.every { it?.status == PickTaskStatus.STAGED.name() }
    }

    private static boolean isPartiallyStaged(StockMovementItem item) {
        if (isCanceled(item) || isFullyPicked(item)) {
            return false
        }
        def picklistItems = item.requisitionItem?.retrievePicklistItems()
        if (!picklistItems) {
            return false
        }
        boolean anyStaged = picklistItems.any { it?.status == PickTaskStatus.STAGED.name() }
        boolean allStaged = picklistItems.every { it?.status == PickTaskStatus.STAGED.name() }
        return anyStaged && !allStaged
    }

    private static boolean hasAllocation(StockMovementItem item) {
        if (isCanceled(item)) {
            return false
        }
        BigDecimal allocated = item.quantityAllocated
                ?: item.requisitionItem?.calculateQuantityAllocated()
                ?: 0
        return allocated > 0 || isPicked(item)
    }

    private static BigDecimal getQuantityShipped(StockMovementItem item) {
        if (item.quantityShipped) {
            return item.quantityShipped
        }
        def requisitionItem = item.requisitionItem
        def shipment = requisitionItem?.requisition?.shipment
        if (!shipment) {
            return 0
        }
        def shipmentItems = shipment.shipmentItems?.findAll { it.requisitionItem?.id == requisitionItem?.id }
        return shipmentItems?.sum { it.quantity } ?: 0
    }
}
