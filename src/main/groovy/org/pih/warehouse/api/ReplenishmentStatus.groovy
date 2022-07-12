package org.pih.warehouse.api

enum ReplenishmentStatus {
    READY(0),
    PENDING(1),
    PLACED(2),
    APPROVED(3),
    COMPLETED(4),
    CANCELED(5)

    int sortOrder

    ReplenishmentStatus(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(ReplenishmentStatus a, ReplenishmentStatus b) {
        return a.sortOrder <=> b.sortOrder
    }
}
