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

    static List<CycleCountStatus> listInProgress() {
        return [COUNTING, COUNTED, READY_TO_REVIEW]
    }

    static List<CycleCountStatus> listRecounting() {
        return [COUNTED, INVESTIGATING]
    }
}
