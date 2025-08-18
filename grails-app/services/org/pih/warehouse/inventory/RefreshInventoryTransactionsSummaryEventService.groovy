package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.Constants
import org.springframework.context.ApplicationListener

@Transactional
class RefreshInventoryTransactionsSummaryEventService implements ApplicationListener<RefreshInventoryTransactionsSummaryEvent> {

    InventoryTransactionSummaryService inventoryTransactionSummaryService

    @Override
    void onApplicationEvent(RefreshInventoryTransactionsSummaryEvent event) {
        if (event.transactionTypeId == Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID) {
            inventoryTransactionSummaryService.refreshProductInventorySummaryView(event)
            return
        }
        inventoryTransactionSummaryService.refreshInventoryMovementSummaryView(event)
    }

}
