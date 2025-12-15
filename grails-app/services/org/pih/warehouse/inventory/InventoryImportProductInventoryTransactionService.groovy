package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand

/**
 * Responsible for managing product inventory transactions for the Inventory Import feature.
 */
@Transactional
class InventoryImportProductInventoryTransactionService extends ProductInventoryTransactionService<ImportDataCommand> {

    ConfigService configService

    TransactionSource createMissingInventoryImportTransactionSource(Location location) {
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.INVENTORY_IMPORT,
                origin: location,
                destination: location,
                migrated: true
        )
        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }
        return transactionSource.save(flush: true)
    }

    TransactionSource createInventoryImportTransactionSource(ImportDataCommand importDataCommand) {
        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.INVENTORY_IMPORT,
                origin: importDataCommand.location,
                destination: importDataCommand.location,
                migrated: false,
        )
        if (!transactionSource.validate()) {
            throw new ValidationException("Invalid transaction source", transactionSource.errors)
        }
        return transactionSource.save()
    }

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        TransactionSource transactionSource = createInventoryImportTransactionSource(sourceObject)
        transaction.transactionSource = transactionSource
    }

    @Override
    protected boolean baselineTransactionsEnabled() {
        return configService.getProperty("openboxes.transactions.inventoryBaseline.inventoryImport.enabled", Boolean)
    }
}
