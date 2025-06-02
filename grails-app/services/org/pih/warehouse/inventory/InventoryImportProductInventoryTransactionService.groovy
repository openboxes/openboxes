package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.importer.ImportDataCommand

/**
 * Responsible for managing product inventory transactions for the Inventory Import feature.
 */
@Transactional
class InventoryImportProductInventoryTransactionService extends ProductInventoryTransactionService<ImportDataCommand> {

    ConfigService configService

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        // Inventory Import has no source object.
    }

    @Override
    protected boolean baselineTransactionsEnabled() {
        return configService.getProperty("openboxes.transactions.inventoryBaseline.inventoryImport.enabled", Boolean)
    }
}
