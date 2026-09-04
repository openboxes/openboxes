package org.pih.warehouse.requisition

import grails.util.Holders
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

import org.pih.warehouse.core.RequisitionEvent
import org.pih.warehouse.core.WebhookEventType

/**
 * Derives the webhook-facing {@link RequisitionEvent} from a persisted requisition status change, so that the
 * webhook trigger lives in one place instead of being published by hand at each transition site.
 *
 * Only statuses that map to a {@link WebhookEventType} produce an event. REQUISITION_CREATED is deliberately
 * not handled here - it is an insert event rather than a transition (a requisition inserted directly at a
 * later status still counts as created), and stays in Requisition#afterInsert.
 *
 * Runs in the BEFORE_COMMIT phase so that the RequisitionEvent it publishes is registered against the still
 * open transaction, and {@link org.pih.warehouse.core.RequisitionEventService} receives it in its usual
 * AFTER_COMMIT phase. Publishing from AFTER_COMMIT instead would register a synchronization on a transaction
 * that has already finished notifying its listeners.
 */
class RequisitionStatusWebhookEventService {

    /**
     * The webhook event a requisition status transition should be published as, or null for a status that
     * isn't exposed over webhooks.
     */
    private static WebhookEventType toWebhookEventType(RequisitionStatus status) {
        switch (status) {
            case RequisitionStatus.STAGED:
                return WebhookEventType.REQUISITION_STAGED
            case RequisitionStatus.ISSUED:
                return WebhookEventType.REQUISITION_ISSUED
            default:
                return null
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onRequisitionStatusChanged(RequisitionStatusChangedEvent event) {
        WebhookEventType webhookEventType = toWebhookEventType(event.newStatus)
        if (!webhookEventType) {
            return
        }

        log.info "Publishing ${webhookEventType.name} for ${event}"
        Holders.grailsApplication.mainContext.publishEvent(
                new RequisitionEvent(event.requisitionId, webhookEventType))
    }
}
