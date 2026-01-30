package org.pih.warehouse.api
import org.pih.warehouse.api.PickTaskStatus

enum FulfillmentSummaryStatus {
    // Received, but not yet allocated
    CREATED,
    // Allocation has started or is in progress
    PICKING,
    // Picking is complete for all items
    PICKED,
    // All items are in the final staging location
    STAGED,
    // The movement has been fully fulfilled/shipped
    ISSUED,
    // All items in the movement were cancelled
    CANCELLED,
    // Partial fulfillment occurred, remaining quantity is unavailable
    BACK_ORDER

    static FulfillmentSummaryStatus fromLineItems(
            List<StockMovementItem> items
    ) {

        if (!items) {
            return CREATED
        }

        def netRequired = { StockMovementItem item ->
            item.getQuantityRequired()
        }

        def isFullyCanceled = { StockMovementItem item ->
            netRequired(item) <= BigDecimal.ZERO
        }

        def isFullyIssued = { StockMovementItem item ->
            !isFullyCanceled(item) &&
                    (item.quantityShipped ?: BigDecimal.ZERO) >= netRequired(item)
        }

        def isFullyPicked = { StockMovementItem item ->
            !isFullyCanceled(item) &&
                    (item.quantityPicked ?: BigDecimal.ZERO) >= netRequired(item)
        }

        def isPicked = { StockMovementItem item ->
            !isFullyCanceled(item) &&
                    (item.quantityPicked ?: BigDecimal.ZERO) <= netRequired(item)
        }

        def isStaged = { StockMovementItem item ->
            if (isFullyCanceled(item)) {
                return false
            }

            def picklistItems = item.requisitionItem?.retrievePicklistItems()

            if (!picklistItems) {
                return false
            }

            return picklistItems.every { it?.status == PickTaskStatus.STAGED.name() }
        }

        def hasAllocation = { StockMovementItem item ->
            !isFullyCanceled(item) &&
                    ((item.quantityAllocated ?: BigDecimal.ZERO) > BigDecimal.ZERO || isPicked(item))
        }

        def hasAnyShipped = { StockMovementItem item ->
            (item.quantityShipped ?: BigDecimal.ZERO) > BigDecimal.ZERO
        }

        // 1. CANCELLED
        if (items.every { isFullyCanceled(it) }) {
            return CANCELLED
        }

        // 2. ISSUED
        if (items.every { isFullyCanceled(it) || isFullyIssued(it) }) {
            return ISSUED
        }

        // 3. BACK_ORDER
        boolean anyShipped = items.any { hasAnyShipped(it) }
        boolean anyUnallocatedActive = items.any {
            !isFullyCanceled(it) &&
                    !isFullyIssued(it) &&
                    !hasAllocation(it)
        }

        if (anyShipped && anyUnallocatedActive) {
            return BACK_ORDER
        }

        // 4. STAGED
        if (items.every { isFullyCanceled(it) || isStaged(it) }) {
            return STAGED
        }

        // 5. PICKED
        if (items.every { isFullyCanceled(it) || isFullyPicked(it) }) {
            return PICKED
        }

        // 6. PICKING
        if (items.any { hasAllocation(it) }) {
            return PICKING
        }

        // 7. CREATED
        return CREATED
    }
}
