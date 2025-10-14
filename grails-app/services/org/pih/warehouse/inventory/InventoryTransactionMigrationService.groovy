package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

import org.pih.warehouse.importer.ImportDataCommand

/**
 * Responsible for managing product inventory transactions for the old product inventory transaction migration
 * to the new Inventory Baseline and Adjustment pair
 */
@Transactional
class InventoryTransactionMigrationService extends ProductInventoryTransactionService<ImportDataCommand> {

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        // Transaction migration has no source object.
    }
}
