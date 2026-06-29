package org.pih.warehouse.core

import org.pih.warehouse.inboundSortation.InboundSortationService
import org.pih.warehouse.shipping.Shipment
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class ShipmentEventService {
    WebhookPublisherService webhookPublisherService
    InboundSortationService inboundSortationService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onShipmentEvent(ShipmentEvent event) {
        log.info "Shipment event $event with event type ${event?.eventType?.name} has been published! " + event.properties

        // The AFTER_COMMIT phase runs after the original transaction has committed, so we need a
        // new transaction to write to the database.
        Shipment.withNewTransaction {
            // Load inside the new session — the AutomaticReceiptJob's withSystemUser session is still open,
            // so entities loaded outside this block would belong to a different session.
            Shipment shipment = Shipment.get(event.source)
            if (!shipment) {
                log.warn "Shipment with id ${event.source} not found, cannot send notification ${event.eventType?.name}"
                return
            }

            switch (event.eventType) {
                case WebhookEventType.SHIPMENT_RECEIVED:
                    log.info "Creating putaway tasks for receipt ${shipment.receipt}"
                    if (shipment.destination?.supports(ActivityCode.AUTOMATED_PUTAWAY_CREATION)) {
                        inboundSortationService.createPutawayOrdersFromReceipt(shipment.receipt)
                    }

                    // TODO: for now, we don't have to publish this. If needed in the future, just uncomment this block
//                    if (shipment.requisition) {
//                        webhookPublisherService.publishRequisitionEvent(shipment.requisition, event.eventType)
//                    }

                    // TODO: Refactor publishShippedEvent into publishShipmentEvent
                    // webhookPublisherService.publishShipmentEvent(requisition, < >)
                    break
            }
        }
    }
}
