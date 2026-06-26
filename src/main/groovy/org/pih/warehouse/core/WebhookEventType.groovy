package org.pih.warehouse.core

enum WebhookEventType {
    REQUISITION_CREATED('requisition.created'),
    REQUISITION_ISSUED('requisition.issued'),
    SHIPMENT_RECEIVED('shipment.received'),
    REQUISITION_STAGED('requisition.staged'),
    PICK_STARTED('pick.started'),
    PICK_COMPLETED('pick.completed'),
    ADJUSTMENT_CREATED('adjustment.created'),
    CYCLE_COUNT_COMPLETED('cycleCount.completed');

    String name

    WebhookEventType(String name) {
        this.name = name
    }
}
