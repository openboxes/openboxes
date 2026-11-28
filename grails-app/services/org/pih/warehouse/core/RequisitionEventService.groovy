package org.pih.warehouse.core

import grails.core.GrailsApplication
import grails.plugins.quartz.GrailsJobClassConstants
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.jobs.AutomaticReplenishmentJob
import org.pih.warehouse.requisition.Requisition
import org.quartz.JobDataMap
import org.quartz.JobKey
import org.quartz.ObjectAlreadyExistsException
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class RequisitionEventService {
    GrailsApplication grailsApplication
    WebhookPublisherService webhookPublisherService
    Scheduler quartzScheduler

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
                // workaround to delay AutomaticIssuanceJob after RefreshProductAvailabilityJob; it needs product refresh to complete first
                // RefreshProductAvailabilityJob is triggered with 5000 ms delay and AutomaticIssuanceJob by default 6000 ms delay
                def delayInMilliseconds = Integer.valueOf(grailsApplication.config.openboxes.jobs.automaticIssuanceJob.delayInMilliseconds)
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
        String facilityId = requisition.origin.id

        // One trigger per facility, so that a burst of pick completions results in a single run after the last one
        TriggerKey triggerKey = TriggerKey.triggerKey("AutomaticReplenishmentJob-${facilityId}", AutomaticReplenishmentJob.name)
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(JobKey.jobKey(AutomaticReplenishmentJob.name, GrailsJobClassConstants.DEFAULT_GROUP))
                // No target activity codes: the job uses the configured ones
                .usingJobData(new JobDataMap([facilityId: facilityId]))
                .startAt(runAt)
                .build()

        log.info "Triggering automaticReplenishmentJob for ${requisition.origin} with ${refreshDelayInMilliseconds + delayInMilliseconds} ms delay, " +
                "replacing its pending run if there is one"
        // Replace the pending run of the facility, or schedule one if there is none
        if (!quartzScheduler.rescheduleJob(triggerKey, trigger)) {
            try {
                quartzScheduler.scheduleJob(trigger)
            } catch (ObjectAlreadyExistsException e) {
                // Scheduled by a concurrent pick completion in the meantime
                quartzScheduler.rescheduleJob(triggerKey, trigger)
            }
        }
    }
}
