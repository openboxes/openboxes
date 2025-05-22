package org.pih.warehouse.inventory

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants

import grails.gorm.transactions.Transactional

@Transactional
class RecordStockProductInventoryTransactionService extends ProductInventoryTransactionService<RecordInventoryCommand> {

    InventoryService inventoryService

    Transaction createAdjustmentTransaction(
            RecordInventoryCommand recordInventoryCommand,
            Date transactionDate
    ) {
        Transaction transaction = new Transaction(recordInventoryCommand.properties)
        transaction.transactionType = TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        transaction.inventory = recordInventoryCommand.inventory
        transaction.comment = recordInventoryCommand.comment
        transaction.transactionNumber = inventoryService.generateTransactionNumber(transaction)
        transaction.transactionDate = transactionDate
        return transaction
    }

    TransactionEntry createTransactionEntry(
            RecordInventoryRowCommand recordInventoryRowCommand,
            InventoryItem inventoryItem,
            Map<String, AvailableItem> availableItems
    ) {
        String key = ProductAvailabilityService.constructAvailableItemKey(recordInventoryRowCommand.binLocation, inventoryItem)
        int quantityOnHand = availableItems.get(key)?.quantityOnHand ?: 0
        int adjustmentQuantity = recordInventoryRowCommand.newQuantity - quantityOnHand
        if (adjustmentQuantity == 0) {
            return null
        }
        TransactionEntry transactionEntry = new TransactionEntry(recordInventoryRowCommand.properties)
        transactionEntry.quantity = adjustmentQuantity
        transactionEntry.product = inventoryItem?.product
        transactionEntry.inventoryItem = inventoryItem
        transactionEntry.comments = recordInventoryRowCommand.comment
        return transactionEntry
    }

    @Override
    void setSourceObject(Transaction transaction, RecordInventoryCommand transactionSource) {
        // Record stock has no source object.
    }
}
