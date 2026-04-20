package org.pih.warehouse.core


import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.springframework.context.ApplicationListener

@Transactional
class SendNotificationPREventService implements ApplicationListener<SendNotificationPREvent> {

    WebhookPublisherService webhookPublisherService

    @Override
    void onApplicationEvent(SendNotificationPREvent event) {
        log.info "Application event $event has been published! " + event.properties
        Requisition requisition = Requisition.get(event.source)
        if (!requisition) {
            log.warn "Requisition with id ${event.source} not found, cannot send notofocation PR"
            return
        }

        webhookPublisherService.publishOrderConfirmation(requisition, WebhookNotificationComment.PR)
    }
}
