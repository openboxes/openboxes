package org.pih.warehouse.inventory

enum CycleCountRequestStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED

    @Override
    String toString() {
        return name()
    }
}
