package org.pih.warehouse.picking

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class PickTaskPickedEventService implements ApplicationListener<PickTaskPickedEvent> {

    def productAvailabilityService

    @Override
    void onApplicationEvent(PickTaskPickedEvent event) {
        PickTask task = (PickTask) event.source
        log.info "Pick task ${task.id}, product: ${task.product}, quantity: ${task.quantityPicked} picked by ${task.pickedBy}"
        productAvailabilityService.triggerRefreshProductAvailability(task?.facility?.id, [task.product.id], event?.forceRefresh)
    }
}
