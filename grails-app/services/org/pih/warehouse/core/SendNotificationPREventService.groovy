package org.pih.warehouse.core


import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.springframework.context.ApplicationListener

@Transactional
class SendNotificationPREventService implements ApplicationListener<SendRequisitionNotificationEvent> {
    WebhookPublisherService webhookPublisherService

    @Override
    void onApplicationEvent(SendRequisitionNotificationEvent event) {
        log.info "Application event $event has been published! " + event.properties
        Requisition requisition = Requisition.get(event.source)
        if (!requisition) {
            log.warn "Requisition with id ${event.source} not found, cannot send notification ${event.eventType?.name}"
            return
        }

        webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
    }
}
