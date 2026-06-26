package org.pih.warehouse.core

import grails.core.GrailsApplication
import org.pih.warehouse.inboundSortation.InboundSortationService
import org.pih.warehouse.jobs.AutomaticIssuanceJob
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class RequisitionEventService {
    GrailsApplication grailsApplication
    WebhookPublisherService webhookPublisherService
    InboundSortationService inboundSortationService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onRequisitionEvent(RequisitionEvent event) {
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
                webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
                break
            case WebhookEventType.SHIPMENT_RECEIVED:
                // The AFTER_COMMIT phase runs after the original transaction has committed, so we need a
                // new transaction for the putaway creation to write to the database.
                Requisition.withNewTransaction {
                    List<Shipment> shipments = Shipment.findAllByRequisition(requisition)
                    shipments.each { Shipment shipment ->
                        log.info "Creating putaway tasks for receipt ${shipment.receipt}"
                        if (shipment.destination?.supports(ActivityCode.AUTOMATED_PUTAWAY_CREATION)) {
                            inboundSortationService.createPutawayOrdersFromReceipt(shipment.receipt)
                        }
                    }
                }
                webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
                break
            case WebhookEventType.REQUISITION_CREATED:
                webhookPublisherService.publishRequisitionEvent(requisition, event.eventType)
                break
        }
    }
}
