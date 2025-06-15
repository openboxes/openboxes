package org.pih.warehouse.inventory

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants

import grails.gorm.transactions.Transactional

@Transactional
class RecordStockProductInventoryTransactionService extends ProductInventoryTransactionService<RecordInventoryCommand> {

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
        // When the row has old qty set to 0, it means that it is a new row.
        // In that case, we should add the whole value from that row to
        // the already existing corresponding line. If the quantity in
        // already existing row is 5, and the new row has quantity 2,
        // We should get one line with qty 7, and the transaction entry
        // should contain quantity 2. In case we are editing already
        // existing line (old quantity greater than 0), we should set the
        // new value for that line. The adjustment entry should be a
        // difference between the new value and the original value.
        // When we had quantity 5, and then we changed it to 7,
        // The row should have quantity 7, but the adjustment should
        // have quantity 2.
        int adjustmentQuantity = !recordInventoryRowCommand.oldQuantity
            ? recordInventoryRowCommand.newQuantity
            : recordInventoryRowCommand.newQuantity - quantityOnHand
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
