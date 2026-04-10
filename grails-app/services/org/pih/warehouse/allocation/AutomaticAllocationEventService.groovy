package org.pih.warehouse.allocation

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.pih.warehouse.jobs.AutomaticAllocationJob
import org.pih.warehouse.requisition.Requisition
import org.springframework.context.ApplicationListener

@Transactional
class AutomaticAllocationEventService implements ApplicationListener<AutomaticAllocationEvent> {

    GrailsApplication grailsApplication

    @Override
    void onApplicationEvent(AutomaticAllocationEvent event) {
        if (!Holders.config.openboxes.jobs.automaticAllocationJob.enabled) {
            log.info"Automatic allocation job is disabled"
            return
        }

        log.info "Application event $event has been published! " + event.properties
        Requisition requisition = Requisition.get(event.source)
        if (!requisition) {
            log.warn "Requisition with id ${event.source} not found, cannot apply automatic allocation"
            return
        }

        if (requisition.autoAllocationEnabled) {
            def delayInMilliseconds =
                    Integer.valueOf(grailsApplication.config.openboxes.jobs.automaticAllocationJob.delayInMilliseconds) ?: 0
            Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
            log.info "Triggering automatic allocation job with ${delayInMilliseconds} ms delay"
            AutomaticAllocationJob.schedule(runAt, [requisitionId: event.source])
        }
    }
}
