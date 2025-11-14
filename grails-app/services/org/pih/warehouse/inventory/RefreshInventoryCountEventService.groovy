package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.Constants
import org.springframework.context.ApplicationListener

@Transactional
class RefreshInventoryCountEventService implements ApplicationListener<RefreshInventoryCountEvent> {

    InventoryCountService inventoryCountService

    @Override
    void onApplicationEvent(RefreshInventoryCountEvent event) {
        if (event.transactionTypeId == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID) {
            inventoryCountService.refreshAdjustmentCandidatesView(event.inventory, event.productIds, event.transactionId, event.transactionDate)
        }
        if (event.transactionTypeId == Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID) {
            inventoryCountService.refreshInventoryBaselineCandidatesView(event.inventory, event.productIds, event.transactionId, event.transactionDate)
        }
        // TODO: Implement an event action to delete from the helper tables. For now, not having it, doesn't break anything, but would be good to have
    }
}
