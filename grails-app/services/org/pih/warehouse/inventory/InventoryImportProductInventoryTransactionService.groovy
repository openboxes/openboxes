package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

import org.pih.warehouse.importer.ImportDataCommand

/**
 * Responsible for managing product inventory transactions for the Inventory Import feature.
 */
@Transactional
class InventoryImportProductInventoryTransactionService extends ProductInventoryTransactionService<ImportDataCommand> {

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        // Inventory Import has no source object.
    }
}
