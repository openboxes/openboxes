package org.pih.warehouse.picking

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class PickTaskUpdateEventService implements ApplicationListener<PickTaskUpdateEvent> {

    def productAvailabilityService

    @Override
    void onApplicationEvent(PickTaskUpdateEvent event) {
        PickTask task = (PickTask) event.source
        productAvailabilityService.triggerRefreshProductAvailability(task?.facility?.id, [task.product.id], event?.forceRefresh)
    }
}
