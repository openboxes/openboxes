package org.pih.warehouse.inventory

import grails.core.GrailsApplication
import org.pih.warehouse.jobs.PutawayLocationReslottingJob
import org.springframework.context.ApplicationListener

class ReslottingEventService implements ApplicationListener<ReslottingEvent> {
    GrailsApplication grailsApplication

    void onApplicationEvent(ReslottingEvent event) {
        log.info "Application event $event has been published! " + event.properties

        def delayInMilliseconds =
                Integer.valueOf(grailsApplication.config.openboxes.jobs.putawayLocationReslottingJob.delayInMilliseconds) ?: 1000
        Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
        log.info "Triggering putaway location reslotting job with ${delayInMilliseconds} ms delay"
        PutawayLocationReslottingJob.schedule(runAt, [inventoryLevelId: event.source])
    }
}
