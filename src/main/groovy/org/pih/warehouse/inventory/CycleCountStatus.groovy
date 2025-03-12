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
     * @return true if the cycle count is in a status where a count (not recount) is currently in progress.
     */
    boolean isCounting() {
        return this in [REQUESTED, COUNTING]
    }

    /**
     * @return true if the cycle count is in a status where a recount (not count) is currently in progress.
     */
    boolean isRecounting() {
        return this in [COUNTED, INVESTIGATING]
    }

    /**
     * @return true if the cycle count is fully closed out / resolved.
     */
    boolean isClosed() {
        return this in [COMPLETED, CANCELED]
    }
}
