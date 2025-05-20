package org.pih.warehouse.inventory

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

import grails.gorm.transactions.Transactional

import javax.xml.bind.ValidationException

@Transactional
class RecordStockProductInventoryTransactionService {

    InventoryService inventoryService
    TransactionIdentifierService transactionIdentifierService

    Transaction createInventoryBaselineTransaction(
            Location facility,
            Date transactionDate = new Date(),
            List<AvailableItem> items
    ) {
        TransactionType transactionType = TransactionType.read(Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID)

        Transaction transaction = new Transaction(
                source: facility,
                inventory: facility.inventory,
                transactionDate: transactionDate,
                transactionType: transactionType,
        )

        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        for (AvailableItem availableItem : items) {
            TransactionEntry transactionEntry = new TransactionEntry(
                    quantity: availableItem.quantityOnHand,
                    binLocation: availableItem.binLocation,
                    inventoryItem: availableItem.inventoryItem,
            )

            transaction.addToTransactionEntries(transactionEntry)
        }

        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }
        return transaction
    }

    List<AvailableItem> getAvailableItems(
            Inventory inventory,
            Product product
    ) {
        List<AvailableItem> availableItems = new ArrayList<>()
        List<TransactionEntry> transactionEntries = inventoryService.getTransactionEntriesByInventoryAndProduct(inventory, [product])
        List<BinLocationItem> binLocationItems = inventoryService.getQuantityByBinLocation(transactionEntries)

        for (BinLocationItem binLocationItem : binLocationItems) {
            AvailableItem availableItem = new AvailableItem(
                    quantityAvailable: binLocationItem.quantity,
                    binLocation: binLocationItem.binLocation,
                    inventoryItem: binLocationItem.inventoryItem,
            )
            availableItems.add(availableItem)
        }

        return availableItems
    }

    Integer getTransactionEntryQuantity(List<AvailableItem> availableItems, Integer newQuantity, InventoryItem inventoryItem) {
        AvailableItem availableItem = availableItems.find {
            it.inventoryItem.lotNumber == inventoryItem.lotNumber &&
            it.inventoryItem.expirationDate == inventoryItem.expirationDate &&
            it.inventoryItem.product.id == inventoryItem.product.id
        }

        return newQuantity - (availableItem?.quantityAvailable ?: 0)
    }

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
            List<AvailableItem> availableItems
    ) {
        TransactionEntry transactionEntry = new TransactionEntry(recordInventoryRowCommand.properties)
        transactionEntry.quantity = getTransactionEntryQuantity(availableItems, recordInventoryRowCommand.newQuantity, inventoryItem)
        transactionEntry.product = inventoryItem?.product
        transactionEntry.inventoryItem = inventoryItem
        transactionEntry.comments = recordInventoryRowCommand.comment
        return transactionEntry
    }
}
