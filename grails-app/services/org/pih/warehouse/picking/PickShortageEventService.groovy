package org.pih.warehouse.picking

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class PickShortageEventService implements ApplicationListener<PickShortageEvent> {

    def notificationService

    @Override
    void onApplicationEvent(PickShortageEvent event) {
        log.info "Application event $event has been published!"
        notificationService.sendPickShortageNotification(event.picklistItem, event.quantityMovedToLostAndFound, event.lostAndFound)
    }
}
