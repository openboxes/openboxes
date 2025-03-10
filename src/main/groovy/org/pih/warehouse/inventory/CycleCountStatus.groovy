package org.pih.warehouse.inventory

enum CycleCountStatus {
    REQUESTED,
    COUNTING,
    COUNTED,
    INVESTIGATING,
    READY_TO_REVIEW,
    REVIEWED,
    COMPLETED,
    CANCELED

    /**
     * @return true if the cycle count is fully closed out / resolved.
     */
    boolean isClosed() {
        return this in [COMPLETED, CANCELED]
    }

    static List<CycleCountStatus> listInProgress() {
        return [COUNTING, COUNTED, READY_TO_REVIEW]
    }

    static List<CycleCountStatus> listRecounting() {
        return [COUNTED, INVESTIGATING]
    }
}
