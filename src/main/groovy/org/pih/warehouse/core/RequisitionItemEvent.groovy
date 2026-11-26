package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class RequisitionItemEvent extends ApplicationEvent {
    WebhookEventType eventType

    RequisitionItemEvent(String requisitionItemId, WebhookEventType eventType) {
        super(requisitionItemId)
        this.eventType = eventType
    }
}
