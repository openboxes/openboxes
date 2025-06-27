package org.pih.warehouse.receiving

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticReceiptCreationJob
import org.pih.warehouse.shipping.Shipment
import org.springframework.context.ApplicationListener

@Transactional
class AutomaticReceiptEventService implements ApplicationListener<AutomaticReceiptEvent> {

    GrailsApplication grailsApplication
    def receiptService

    @Override
    void onApplicationEvent(AutomaticReceiptEvent event) {
        if (!Holders.config.openboxes.jobs.automaticReceiptCreationJob.enabled) {
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
            def delayInMilliseconds =
                    Integer.valueOf(grailsApplication.config.openboxes.jobs.automaticReceiptCreationJob.delayInMilliseconds) ?: 0
            Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
            log.info "Triggering automatic receipt job with ${delayInMilliseconds} ms delay"
            AutomaticReceiptCreationJob.schedule(runAt)
        }
    }
}
