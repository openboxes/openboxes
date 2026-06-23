package org.pih.warehouse.inventory

import org.pih.warehouse.jobs.PutawayLocationReslottingJob
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionSynchronizationManager

class ReslottingEventService {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onReslottingEvent(ReslottingEvent event) {
        log.info "Application event $event has been published! " + event.properties

        log.debug "isActualTransactionActive = ${TransactionSynchronizationManager.isActualTransactionActive()}"
        log.debug "isSynchronizationActive = ${TransactionSynchronizationManager.isSynchronizationActive()}"

        PutawayLocationReslottingJob.triggerNow([inventoryLevelId: event.source])
    }
}
