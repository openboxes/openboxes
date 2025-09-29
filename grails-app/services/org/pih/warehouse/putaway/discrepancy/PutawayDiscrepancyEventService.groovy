package org.pih.warehouse.putaway.discrepancy

import grails.gorm.transactions.Transactional
import org.pih.warehouse.putaway.PutawayTask
import org.springframework.context.ApplicationListener

@Transactional
class PutawayDiscrepancyEventService implements ApplicationListener<PutawayDiscrepancyEvent> {

    def notificationService

    @Override
    void onApplicationEvent(PutawayDiscrepancyEvent event) {
        log.info "Application event $event has been published!"
        PutawayTask putawayTask = (PutawayTask) event.source
        notificationService.sendPutawayDiscrepancyNotification(putawayTask)
    }
}
