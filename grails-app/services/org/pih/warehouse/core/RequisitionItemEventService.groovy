package org.pih.warehouse.core

import org.pih.warehouse.requisition.RequisitionItem
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class RequisitionItemEventService {
    WebhookPublisherService webhookPublisherService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onRequisitionItemEvent(RequisitionItemEvent event) {
        log.info "Requisition item event $event with event type ${event?.eventType?.name} has been published! " + event.properties
        RequisitionItem item = RequisitionItem.get(event.source)
        if (!item) {
            log.warn "Requisition item with id ${event.source} not found, cannot send notification ${event.eventType?.name}"
            return
        }

        switch (event.eventType) {
            case WebhookEventType.REQUISITION_ITEM_STAGED:
                webhookPublisherService.publishRequisitionItemEvent(item, event.eventType)
                break

            // Add more cases for other event types as needed
        }
    }
}
