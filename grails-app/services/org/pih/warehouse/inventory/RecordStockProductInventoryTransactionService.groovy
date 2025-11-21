package org.pih.warehouse.inventory

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.Constants

import grails.gorm.transactions.Transactional

import org.pih.warehouse.inventory.product.availability.AvailableItemKey
import org.pih.warehouse.inventory.product.availability.AvailableItemMap

@Transactional
class RecordStockProductInventoryTransactionService extends ProductInventoryTransactionService<RecordInventoryCommand> {

    ConfigService configService

    @Override
    protected boolean baselineTransactionsEnabled() {
        return configService.getProperty("openboxes.transactions.inventoryBaseline.recordStock.enabled", Boolean)
    }

    Transaction createAdjustmentTransaction(
            RecordInventoryCommand recordInventoryCommand,
            Date transactionDate
    ) {
        Transaction transaction = new Transaction(
                transactionType: TransactionType.get(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID),
                inventory: recordInventoryCommand.inventory,
                comment: recordInventoryCommand.comment,
                transactionDate: transactionDate,
                // Don't refresh product availability. That will get done manually at the end.
                disableRefresh: true,
        )
        transaction.transactionNumber = inventoryService.generateTransactionNumber(transaction)
        return transaction
    }

    TransactionEntry createAdjustmentTransactionEntry(
            RecordInventoryRowCommand recordInventoryRowCommand,
            InventoryItem inventoryItem,
            AvailableItemMap availableItems
    ) {
        Integer quantityOnHand = availableItems.get(recordInventoryRowCommand.binLocation, inventoryItem)?.quantityOnHand
        // When the row doesn't already have an existing quantity on hand on the date of creation, it means that it is a new
        // row. In that case, we should add the whole value from that row to the already existing corresponding line. If
        // The quantity in the already existing row is 5, and the new row has a quantity of 2. We should get one line with qty 7,
        // and the transaction entry should contain quantity 2. In case we are editing an already existing line (old quantity
        // greater than 0), we should set the new value for that line. The adjustment entry should be the difference between
        // the new value and the original value. When we had quantity 5, and then we changed it to 7, the row should have
        // quantity 7, but the adjustment should have quantity 2.
        int adjustmentQuantity = quantityOnHand == null
                ? recordInventoryRowCommand.newQuantity
                : recordInventoryRowCommand.newQuantity - quantityOnHand

        if (adjustmentQuantity == 0) {
            return null
        }

        return new TransactionEntry(
                quantity: adjustmentQuantity,
                product: inventoryItem?.product,
                inventoryItem: inventoryItem,
                comments: recordInventoryRowCommand.comment,
                binLocation: recordInventoryRowCommand.binLocation,
        )
    }

    List<TransactionEntry> createZeroingTransactionEntries(
            AvailableItemMap availableItems,
            List<AvailableItem> currentRecordStockItems
    ) {
        List<AvailableItemKey> recordStockKeys = currentRecordStockItems.collect {
            new AvailableItemKey(it.binLocation, it.inventoryItem)
        }

        return availableItems.map.findAll { key, item -> !(key in recordStockKeys)}
                .collect { new TransactionEntry(
                        quantity: -it.value.quantityOnHand,
                        product: it.value.inventoryItem.product,
                        binLocation: it.value.binLocation,
                        inventoryItem: it.value.inventoryItem,
                )}
    }


    @Override
    void setSourceObject(Transaction transaction, RecordInventoryCommand transactionSource) {
        // Record stock has no source object.
    }
}
