package org.pih.warehouse.allocation

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticBackorderReallocationJob
import org.pih.warehouse.shipping.Shipment
import org.springframework.context.ApplicationListener

@Transactional
class AutomaticBackorderAllocationEventService implements ApplicationListener<AutomaticBackorderReallocationEvent> {

    GrailsApplication grailsApplication

    @Override
    void onApplicationEvent(AutomaticBackorderReallocationEvent event) {
        if (!Holders.config.openboxes.jobs.automaticAllocationJob.enabled) {
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
            def delayInMilliseconds =
                    Integer.valueOf(grailsApplication.config.openboxes.jobs.automaticAllocationJob.delayInMilliseconds) ?: 2000
            Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
            log.info "Triggering backorder re-allocation job with ${delayInMilliseconds} ms delay"
            AutomaticBackorderReallocationJob.schedule(runAt, [shipmentId: event.source])
        }
    }
}
