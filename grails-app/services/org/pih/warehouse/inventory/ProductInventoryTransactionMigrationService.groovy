package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.importer.ImportDataCommand

/**
 * Responsible for managing product inventory transactions for the old product inventory transaction migration
 * to the new Inventory Baseline and Adjustment pair
 */
@Transactional
class ProductInventoryTransactionMigrationService extends ProductInventoryTransactionService<ImportDataCommand> {

    ConfigService configService

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        // Inventory Import has no source object.
    }

    @Override
    protected boolean baselineTransactionsEnabled() {
        return configService.getProperty("openboxes.transactions.inventoryBaseline.migration.enabled", Boolean)
    }
}
