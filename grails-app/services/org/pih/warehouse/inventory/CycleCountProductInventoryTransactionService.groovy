package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

/**
 * Responsible for managing product inventory transactions for the Cycle Count feature.
 */
@Transactional
class CycleCountProductInventoryTransactionService extends ProductInventoryTransactionService<CycleCount> {

    @Override
    void setSourceObject(Transaction transaction, CycleCount sourceObject) {
        transaction.cycleCount = sourceObject
    }
}
