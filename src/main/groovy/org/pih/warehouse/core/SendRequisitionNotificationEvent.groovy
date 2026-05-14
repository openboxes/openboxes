package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class SendRequisitionNotificationEvent extends ApplicationEvent {
    WebhookEventType eventType

    SendRequisitionNotificationEvent(String requisitionId, WebhookEventType eventType) {
        super(requisitionId)
        this.eventType = eventType
    }
}
