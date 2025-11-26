package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

/**
 * Responsible for creating baseline transactions when migrating old INVENTORY transactions.
 * (Not to be confused with the PRODUCT_INVENTORY migration done by ProductInventoryTransactionMigrationService.)
 */
@Transactional
class InventoryTransactionMigrationService extends ProductInventoryTransactionService<Object> {

    @Override
    void setSourceObject(Transaction transaction, Object sourceObject) {
        // Transaction migration has no source object.
    }
}
