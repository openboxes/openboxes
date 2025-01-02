package org.pih.warehouse.inventory

enum CycleCountStatus {
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
