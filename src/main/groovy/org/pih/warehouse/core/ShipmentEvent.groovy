package org.pih.warehouse.core

import org.springframework.context.ApplicationEvent

class ShipmentEvent extends ApplicationEvent {
    WebhookEventType eventType

    ShipmentEvent(String shipmentId, WebhookEventType eventType) {
        super(shipmentId)
        this.eventType = eventType
    }
}
