package org.pih.warehouse.inventory

import org.pih.warehouse.jobs.PutawayLocationReslottingJob
import org.springframework.context.ApplicationListener

class ReslottingEventService implements ApplicationListener<ReslottingEvent> {

    @Override
    void onApplicationEvent(ReslottingEvent event) {
        log.info "Application event $event has been published! " + event.properties

        PutawayLocationReslottingJob.triggerNow([inventoryLevelId: event.source])
    }
}
