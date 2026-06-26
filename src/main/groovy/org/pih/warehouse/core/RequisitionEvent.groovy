package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class RequisitionEvent extends ApplicationEvent {
    WebhookEventType eventType

    RequisitionEvent(String requisitionId, WebhookEventType eventType) {
        super(requisitionId)
        this.eventType = eventType
    }
}
