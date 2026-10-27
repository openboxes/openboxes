package org.pih.warehouse.allocation

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticBackorderReallocationJob
import org.pih.warehouse.shipping.Shipment
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Transactional
class AutomaticBackorderAllocationEventService {

    GrailsApplication grailsApplication

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onAutomaticBackorderReallocationEvent(AutomaticBackorderReallocationEvent event) {
        if (!Holders.config.openboxes.jobs.automaticBackorderReallocationJob.enabled) {
            log.info"Backorder re-allocation job is disabled"
            return
        }

        log.info "Application event $event has been published! " + event.properties
        Shipment shipment = Shipment.get(event.source)
        if (!shipment) {
            log.warn "Shipment with id ${event.source} not found, cannot try backorder re-allocation"
            return
        }

        if (shipment.isFullyReceived()) {
            // Delay the job so that the (delayed) RefreshProductAvailabilityJob completes before reallocation reads availability
            def delayInMilliseconds = Integer.valueOf(grailsApplication.config.openboxes.jobs.automaticBackorderReallocationJob.delayInMilliseconds)
            Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
            log.info "Triggering backorder re-allocation job with ${delayInMilliseconds} ms delay"
            AutomaticBackorderReallocationJob.schedule(runAt, [shipmentId: event.source])
        }
    }
}
