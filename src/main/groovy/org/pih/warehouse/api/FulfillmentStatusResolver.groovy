package org.pih.warehouse.api

import org.pih.warehouse.api.PickTaskStatus

/**
 * Resolves the FulfillmentSummaryStatus based on stock movement line items.
 * This logic is specific to Outbound Stock Movements to track the order lifecycle.
 */
class FulfillmentStatusResolver {

    /**
     * Determines the fulfillment summary status based on the state of line items.
     *
     * @param items List of stock movement items to evaluate
     * @return The calculated FulfillmentSummaryStatus
     */
    static FulfillmentSummaryStatus resolve(List<StockMovementItem> items) {
        if (!items) {
            return FulfillmentSummaryStatus.CREATED
        }

        // 1. CANCELLED - All items are fully canceled
        if (items.every { isFullyCanceled(it) }) {
            return FulfillmentSummaryStatus.CANCELLED
        }

        // 2. ISSUED - All items are either fully canceled or fully issued
        if (items.every { isFullyCanceled(it) || isFullyIssued(it) }) {
            return FulfillmentSummaryStatus.ISSUED
        }

        // 3. BACK_ORDER - Some items shipped but others are unallocated
        boolean anyShipped = items.any { hasAnyShipped(it) }
        boolean anyUnallocatedActive = items.any {
            !isFullyCanceled(it) &&
                    !isFullyIssued(it) &&
                    !hasAllocation(it)
        }

        if (anyShipped && anyUnallocatedActive) {
            return FulfillmentSummaryStatus.BACK_ORDER
        }

        // 4. STAGED - All items are either fully canceled or staged
        if (items.every { isFullyCanceled(it) || isStaged(it) }) {
            return FulfillmentSummaryStatus.STAGED
        }

        // 5. PICKED - All items are either fully canceled or fully picked
        if (items.every { isFullyCanceled(it) || isFullyPicked(it) }) {
            return FulfillmentSummaryStatus.PICKED
        }

        // 6. PICKING - At least one item has allocation/picking started
        if (items.any { hasAllocation(it) }) {
            return FulfillmentSummaryStatus.PICKING
        }

        // 7. CREATED - Default state
        return FulfillmentSummaryStatus.CREATED
    }

    // Helper methods for status calculation

    private static BigDecimal netRequired(StockMovementItem item) {
        return item.getQuantityRequired()
    }

    private static boolean isFullyCanceled(StockMovementItem item) {
        return netRequired(item) <= BigDecimal.ZERO
    }

    private static boolean isFullyIssued(StockMovementItem item) {
        return !isFullyCanceled(item) &&
                (item.quantityShipped ?: BigDecimal.ZERO) >= netRequired(item)
    }

    private static boolean isFullyPicked(StockMovementItem item) {
        return !isFullyCanceled(item) &&
                (item.quantityPicked ?: BigDecimal.ZERO) >= netRequired(item)
    }

    private static boolean isPicked(StockMovementItem item) {
        return !isFullyCanceled(item) &&
                (item.quantityPicked ?: BigDecimal.ZERO) <= netRequired(item)
    }

    private static boolean isStaged(StockMovementItem item) {
        if (isFullyCanceled(item)) {
            return false
        }

        def picklistItems = item.requisitionItem?.retrievePicklistItems()

        if (!picklistItems) {
            return false
        }

        return picklistItems.every { it?.status == PickTaskStatus.STAGED.name() }
    }

    private static boolean hasAllocation(StockMovementItem item) {
        return !isFullyCanceled(item) &&
                ((item.quantityAllocated ?: BigDecimal.ZERO) > BigDecimal.ZERO || isPicked(item))
    }

    private static boolean hasAnyShipped(StockMovementItem item) {
        return (item.quantityShipped ?: BigDecimal.ZERO) > BigDecimal.ZERO
    }
}
