package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

import org.pih.warehouse.importer.ImportDataCommand

/**
 * Responsible for creating baseline transactions when migrating old INVENTORY transactions.
 */
@Transactional
class InventoryTransactionMigrationService extends ProductInventoryTransactionService<ImportDataCommand> {

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        // Transaction migration has no source object.
    }
}
