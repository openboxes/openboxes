package org.pih.warehouse.core


import org.pih.warehouse.requisition.Requisition
import org.springframework.context.ApplicationListener

class SendRequisitionNotificationEventService implements ApplicationListener<SendRequisitionNotificationEvent> {
    WebhookPublisherService webhookPublisherService

    @Override
    void onApplicationEvent(SendRequisitionNotificationEvent event) {
        log.info "Application event $event with event type ${event?.eventType?.name} has been published! " + event.properties
        Requisition requisition = Requisition.get(event.source)
        if (!requisition) {
            log.warn "Requisition with id ${event.source} not found, cannot send notification ${event.eventType?.name}"
            return
        }

        webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
    }
}
