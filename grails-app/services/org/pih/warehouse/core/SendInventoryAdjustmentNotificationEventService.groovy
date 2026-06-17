package org.pih.warehouse.core

import grails.util.Holders
import org.pih.warehouse.jobs.SendInventoryAdjustmentNotificationJob
import org.pih.warehouse.product.Product
import org.springframework.context.ApplicationListener

class SendInventoryAdjustmentNotificationEventService implements ApplicationListener<SendInventoryAdjustmentNotificationEvent> {

    @Override
    void onApplicationEvent(SendInventoryAdjustmentNotificationEvent event) {
        log.info "Inventory adjustment event published: products=${event?.products*.id} facility=${event?.facility?.id} baseline=${event?.baselineTransaction?.id} adjustment=${event?.adjustmentTransaction?.id}"
        log.debug "Inventory adjustment event details: ${event?.properties}"
        // Each product should trigger its own notification job
        event?.products?.each { Product product ->
            String delayConfig = Holders.config.openboxes.jobs.sendInventoryAdjustmentNotificationJob.delayInMilliseconds
            Integer delayInMilliseconds = delayConfig ? Integer.valueOf(delayConfig) : 0
            Date runAt = new Date(System.currentTimeMillis() + delayInMilliseconds)
            log.info "Triggering inventory adjustment notification job with ${delayInMilliseconds} ms delay"
            SendInventoryAdjustmentNotificationJob.schedule(runAt, [productId: product?.id,
                                                               facilityId: event.facility?.id,
                                                               baselineTransactionId: event.baselineTransaction?.id,
                                                               adjustmentTransactionId: event.adjustmentTransaction?.id
            ])
        }
    }
}
