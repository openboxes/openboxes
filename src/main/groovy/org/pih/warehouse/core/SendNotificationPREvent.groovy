package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class SendNotificationPREvent extends ApplicationEvent {

    SendNotificationPREvent(String requisitionId) {
        super(requisitionId)
    }
}
