package org.pih.warehouse.inventory

enum CycleCountItemStatus {
    READY_TO_COUNT,
    COUNTING,
    COUNTED,  // AKA Discrepancy Found
    INVESTIGATING,
    READY_TO_REVIEW,
    REVIEWED,
    APPROVED,
    REJECTED,
    CANCELED

    /**
     * @return true if the cycle count item is resolved and doesn't need more attention.
     */
    boolean isCompleted() {
        // TODO: Once we add the review and approval flow back in, remove READY_TO_REVIEW from this list.
        return this in [APPROVED, REVIEWED, REJECTED, CANCELED, READY_TO_REVIEW]
    }
}
