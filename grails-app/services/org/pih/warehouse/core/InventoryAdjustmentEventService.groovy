package org.pih.warehouse.core

import org.pih.warehouse.jobs.PublishInventoryAdjustmentEventJob
import org.pih.warehouse.product.Product
import org.springframework.transaction.event.TransactionalEventListener

class InventoryAdjustmentEventService {

    @TransactionalEventListener
    void onInventoryAdjustmentEvent(InventoryAdjustmentEvent event) {
        log.info "Inventory adjustment event published: " +
                "products=${event?.products*.id} " +
                "facility=${event?.facility?.id} " +
                "baseline=${event?.baselineTransaction?.id} " +
                "adjustment=${event?.adjustmentTransaction?.id}"
        log.debug "Inventory adjustment event details: ${event?.properties}"

        // Each product should trigger its own notification job
        event?.products?.each { Product product ->
            PublishInventoryAdjustmentEventJob.triggerNow([productId              : product?.id,
                                                           facilityId             : event.facility?.id,
                                                           baselineTransactionId  : event.baselineTransaction?.id,
                                                           adjustmentTransactionId: event.adjustmentTransaction?.id
            ])
        }
    }
}
