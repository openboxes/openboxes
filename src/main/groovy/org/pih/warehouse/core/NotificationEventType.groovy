package org.pih.warehouse.core

enum NotificationEventType {
    REQUISITION_CREATED('requisition.created'),
    REQUISITION_ISSUED('requisition.issued'),
    PICK_STARTED('pick.started'),
    PICK_COMPLETED('pick.completed'),
    ADJUSTMENT_CREATED('adjustment.created'),
    CYCLE_COUNT_COMPLETED('cycleCount.completed');

    String name

    NotificationEventType(String name) {
        this.name = name
    }
}
