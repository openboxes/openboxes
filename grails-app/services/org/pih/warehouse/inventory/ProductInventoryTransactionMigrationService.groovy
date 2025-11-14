package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.ConfigService

/**
 * Responsible for managing product inventory transactions for the old product inventory transaction migration
 * to the new Inventory Baseline and Adjustment pair
 */
@Transactional
class ProductInventoryTransactionMigrationService extends ProductInventoryTransactionService<Transaction> {

    ConfigService configService

    @Override
    void setSourceObject(Transaction transaction, Transaction sourceObject) {
        transaction.cycleCount = sourceObject.cycleCount
    }

    @Override
    protected boolean baselineTransactionsEnabled() {
        return configService.getProperty("openboxes.transactions.inventoryBaseline.migration.enabled", Boolean)
    }
}
