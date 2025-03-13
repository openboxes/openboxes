package org.pih.warehouse.inventory

enum CycleCountItemStatus {
    READY_TO_COUNT,
    COUNTING,
    COUNTED,
    INVESTIGATING,
    READY_TO_REVIEW,
    REVIEWED,
    APPROVED,
    REJECTED,
    CANCELED

    static List<CycleCountItemStatus> isCompleted() {
        return [APPROVED, REVIEWED, REJECTED, CANCELED, READY_TO_REVIEW]
    }


}
