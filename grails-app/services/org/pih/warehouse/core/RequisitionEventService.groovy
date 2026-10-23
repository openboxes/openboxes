package org.pih.warehouse.core

import grails.core.GrailsApplication
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.jobs.AutomaticReplenishmentJob
import org.pih.warehouse.requisition.Requisition
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class RequisitionEventService {
    GrailsApplication grailsApplication
    WebhookPublisherService webhookPublisherService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onRequisitionEvent(RequisitionEvent event) {
        log.info "Requisition event $event with event type ${event?.eventType?.name} has been published! " + event.properties
        Requisition requisition = Requisition.get(event.source)
        if (!requisition) {
            log.warn "Requisition with id ${event.source} not found, cannot send notification ${event.eventType?.name}"
            return
        }

        // TODO: Should be moved to the RequisitionStatusChangedEventService and should be tied to state transition
        switch (event.eventType) {
            case WebhookEventType.REQUISITION_STAGED:
                // workaround to delay AutomaticIssuanceJob 1 second after RefreshProductAvailabilityJob; it needs product refresh to complete first
                def delayInMilliseconds = Integer.valueOf(grailsApplication.config.openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds) + 1000 ?: 0
                Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
                log.info "Triggering automaticIssuanceJob job with ${delayInMilliseconds} ms delay"
                AutomaticIssuanceJob.schedule(runAt, [requisitionId: requisition.id])
                webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
                break
            case WebhookEventType.PICK_COMPLETED:
                // Used internally only, pick.completed is not sent as a webhook
                scheduleAutomaticReplenishment(requisition)
                break
            case WebhookEventType.REQUISITION_CREATED:
                webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
                break
        }
    }

    void scheduleAutomaticReplenishment(Requisition requisition) {
        // Delay the job until after RefreshProductAvailabilityJob, so it sees the quantities left in the bins after picking
        Integer refreshDelayInMilliseconds = grailsApplication.config.getProperty(
                "openboxes.jobs.refreshProductAvailabilityJob.delayInMilliseconds", Integer, 0)
        Integer delayInMilliseconds = grailsApplication.config.getProperty(
                "openboxes.jobs.automaticReplenishmentJob.delayInMilliseconds", Integer, 0)
        Date runAt = new Date(System.currentTimeMillis() + refreshDelayInMilliseconds + delayInMilliseconds)
        log.info "Triggering automaticReplenishmentJob for ${requisition.origin} with ${refreshDelayInMilliseconds + delayInMilliseconds} ms delay"
        // No target activity codes: the job uses the configured ones
        AutomaticReplenishmentJob.schedule(runAt, [facilityId: requisition.origin.id])
    }
}
