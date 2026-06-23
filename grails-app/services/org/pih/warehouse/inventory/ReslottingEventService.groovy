package org.pih.warehouse.inventory

import org.pih.warehouse.jobs.PutawayLocationReslottingJob
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

class ReslottingEventService {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onReslottingEvent(ReslottingEvent event) {
        log.info "Application event $event has been published! " + event.properties

        PutawayLocationReslottingJob.triggerNow([inventoryLevelId: event.source])
    }
}
