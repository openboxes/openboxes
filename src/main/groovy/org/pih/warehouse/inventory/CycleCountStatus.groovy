package org.pih.warehouse.inventory

enum CycleCountStatus {
    REQUESTED,
    REVIEWED,
    COUNTING,
    COUNTED,
    INVESTIGATING,
    READY_TO_REVIEW,
    COMPLETED,
    CANCELED

    static List<CycleCountStatus> listInProgress() {
        return [COUNTING, COUNTED, READY_TO_REVIEW]
    }
}
