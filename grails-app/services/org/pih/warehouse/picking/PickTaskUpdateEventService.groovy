package org.pih.warehouse.picking

import grails.gorm.transactions.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Transactional
class PickTaskUpdateEventService {

    def productAvailabilityService

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void onPickTaskUpdateEvent(PickTaskUpdateEvent event) {
        PickTask task = (PickTask) event.source
        productAvailabilityService.triggerRefreshProductAvailability(task?.facility?.id, [task?.product?.id], event?.forceRefresh)
    }
}
