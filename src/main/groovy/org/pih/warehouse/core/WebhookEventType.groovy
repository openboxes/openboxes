package org.pih.warehouse.core

enum WebhookEventType {
    REQUISITION_CREATED('requisition.created'),
    REQUISITION_ISSUED('requisition.issued'),
    SHIPMENT_RECEIVED('shipment.received'),
    REQUISITION_STAGED('requisition.staged'),
    REQUISITION_ITEM_STAGED('requisitionItem.staged'),
    PICK_STARTED('pick.started'),
    PICK_COMPLETED('pick.completed'),
    ADJUSTMENT_CREATED('adjustment.created'),
    CYCLE_COUNT_COMPLETED('cycleCount.completed'),
    PICK_SHORTAGE('pick.shortage');

    String name

    WebhookEventType(String name) {
        this.name = name
    }
}
