package org.pih.warehouse.inventory

enum CycleCountRequestStatus {
    CREATED,
    REQUESTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED

    @Override
    String toString() {
        return name()
    }
}
