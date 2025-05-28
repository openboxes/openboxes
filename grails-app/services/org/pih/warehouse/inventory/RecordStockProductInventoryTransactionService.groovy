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
        Transaction transaction = new Transaction(
                transactionType: TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID),
                inventory: recordInventoryCommand.inventory,
                comment: recordInventoryCommand.comment,
                transactionDate: transactionDate
        )
        transaction.transactionNumber = inventoryService.generateTransactionNumber(transaction)
        return transaction
    }

    TransactionEntry createAdjustmentTransactionEntry(
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
        TransactionEntry transactionEntry = new TransactionEntry(
                quantity: adjustmentQuantity,
                product: inventoryItem?.product,
                inventoryItem: inventoryItem,
                comments: recordInventoryRowCommand.comment,
                binLocation: recordInventoryRowCommand.binLocation,

        )
        return transactionEntry
    }

    @Override
    void setSourceObject(Transaction transaction, RecordInventoryCommand transactionSource) {
        // Record stock has no source object.
    }
}
