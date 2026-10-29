package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.Constants
import org.springframework.context.ApplicationListener

@Transactional
class RefreshInventoryCountEventService implements ApplicationListener<RefreshInventoryCountEvent> {

    InventoryCountService inventoryCountService

    @Override
    void onApplicationEvent(RefreshInventoryCountEvent event) {
        if (event.isDelete) {
            deleteInventoryCountCandidates(event)
            return
        }
        if (event.transactionTypeId == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID) {
            inventoryCountService.refreshAdjustmentCandidatesView(event.inventory, event.productIds, event.transactionId, event.transactionDate)
        }
        if (event.transactionTypeId == Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID) {
            inventoryCountService.refreshInventoryBaselineCandidatesView(event.inventory, event.productIds, event.transactionId, event.transactionDate)
        }
    }

    /**
     * The helper tables are filled in incrementally by the refresh methods above, so the rows of a deleted
     * transaction have to be removed from them, otherwise they keep reporting it as an inventory count of its
     * products.
     */
    private void deleteInventoryCountCandidates(RefreshInventoryCountEvent event) {
        if (event.transactionTypeId == Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID) {
            inventoryCountService.deleteAdjustmentCandidates(event.transactionId)
        }
        if (event.transactionTypeId == Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID) {
            inventoryCountService.deleteInventoryBaselineCandidates(event.transactionId)
        }
        // Deprecated transaction type, so its helper table is delete only - no refresh branch above matches it
        if (event.transactionTypeId == Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID) {
            inventoryCountService.deleteProductInventoryCandidates(event.transactionId)
        }
    }
}
