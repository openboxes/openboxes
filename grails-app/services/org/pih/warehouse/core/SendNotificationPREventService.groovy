package org.pih.warehouse.core

import grails.core.GrailsApplication
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.requisition.Requisition
import org.springframework.context.ApplicationListener

class SendRequisitionNotificationEventService implements ApplicationListener<SendRequisitionNotificationEvent> {
    GrailsApplication grailsApplication
    WebhookPublisherService webhookPublisherService

    @Override
    void onApplicationEvent(SendRequisitionNotificationEvent event) {
        log.info "Application event $event with event type ${event?.eventType?.name} has been published! " + event.properties
        Requisition requisition = Requisition.get(event.source)
        if (!requisition) {
            log.warn "Requisition with id ${event.source} not found, cannot send notification ${event.eventType?.name}"
            return
        }

        switch (event.eventType) {
            case WebhookEventType.REQUISITION_STAGED:
                // workaround to delay AutomaticIssuanceJob 1 second after RefreshProductAvailabilityJob; it needs product refresh to complete first
                def delayInMilliseconds = Integer.valueOf(grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds) + 1000 ?: 0
                Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
                log.info "Triggering automaticIssuanceJob job with ${delayInMilliseconds} ms delay"
                AutomaticIssuanceJob.schedule(runAt, [requisitionId: requisition.id])
                // no break
            case WebhookEventType.REQUISITION_CREATED:
            case WebhookEventType.REQUISITION_ISSUED:
                webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
                break
        }
    }
}
