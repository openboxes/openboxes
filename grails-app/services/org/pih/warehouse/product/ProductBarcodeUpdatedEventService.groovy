package org.pih.warehouse.product

import grails.gorm.transactions.Transactional
import org.springframework.context.ApplicationListener

@Transactional
class ProductBarcodeUpdatedEventService implements ApplicationListener<ProductBarcodeUpdatedEvent> {

    def notificationService

    @Override
    void onApplicationEvent(ProductBarcodeUpdatedEvent event) {
        log.info "Application event $event has been published!"
        Product product = (Product) event.source
        notificationService.sendProductBarcodeUpdatedNotification(product, event.oldUpc, event.newUpc)
    }
}
