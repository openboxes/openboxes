package org.pih.warehouse.receiving

import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticReceiptJob
import org.pih.warehouse.shipping.Shipment
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Transactional
class AutomaticReceiptEventService {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onAutomaticReceiptEvent(AutomaticReceiptEvent event) {
        if (!Holders.config.openboxes.jobs.automaticReceiptJob.enabled) {
            log.info"Automatic receipt creation job is disabled"
            return
        }

        log.info "Application event $event has been published! " + event.properties
        Shipment shipment = Shipment.get(event.source)
        if (!shipment) {
            log.warn "Shipment with id ${event.source} not found, cannot create automatic receipt"
            return
        }

        if (shipment.hasShipped() && !shipment.isFullyReceived()) {
            log.info "Triggering automatic receipt job"
            AutomaticReceiptJob.triggerNow([shipmentId: event.source])
        }
    }
}
