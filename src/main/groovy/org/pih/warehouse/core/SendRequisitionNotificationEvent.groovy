package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class SendRequisitionNotificationEvent extends ApplicationEvent {
    NotificationEventType eventType

    SendRequisitionNotificationEvent(String requisitionId, NotificationEventType eventType) {
        super(requisitionId)
        this.eventType = eventType
    }

    public String toString() {
        return super.toString().replace("]", ", ") + "eventType=" + eventType + "]";
    }
}
