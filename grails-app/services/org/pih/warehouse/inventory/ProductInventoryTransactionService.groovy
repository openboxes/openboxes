package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

/**
 * Responsible for managing product inventory transactions.
 */
@Transactional
class ProductInventoryTransactionService {

    ProductAvailabilityService productAvailabilityService
    TransactionIdentifierService transactionIdentifierService

    /**
     * Create a new product inventory transaction based on the current QoH in product availability.
     *
     * We refer to this transaction as a product inventory "snapshot" because it's functionally a copy of what the
     * quantity on hand (determined by product availability) was for each [bin location + lot number] of the product at
     * the time. As such, transactions created via this method should NOT result in any quantity changes/adjustments.
     *
     * @param facility The Location to take the product inventory snapshot at
     * @param product The Product to take the product inventory snapshot for
     * @param sourceType The feature triggering the product inventory snapshot
     * @param source The source object to be associated with the Transaction, such as CycleCount, Order, Requisition...
     * @param transactionDate The datetime that the transaction should be marked with
     * @return The Transaction that was created
     */
    Transaction createTransaction(
            Location facility,
            Product product,
            ProductInventorySnapshotSource sourceType,
            Object source,
            Date transactionDate=new Date()) {

        TransactionType transactionType = TransactionType.read(Constants.PRODUCT_INVENTORY_TRANSACTION_TYPE_ID)

        Transaction transaction = new Transaction(
                source: facility,
                inventory: facility.inventory,
                transactionDate: transactionDate,
                transactionType: transactionType,
        )

        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        switch (sourceType) {
            case ProductInventorySnapshotSource.CYCLE_COUNT:
                transaction.cycleCount = source as CycleCount
                break
        }

        // Create a transaction entry for every [bin location + lot number] pair that the product currently has.
        // We don't need to include zero quantity items. Excluding them from the transaction achieves the same result.
        List<AvailableItem> availableItems = productAvailabilityService.getAvailableItems(
                facility, [product.id], false, true)

        for (AvailableItem availableItem : availableItems) {
            TransactionEntry transactionEntry = new TransactionEntry(
                    quantity: availableItem.quantityOnHand,
                    binLocation: availableItem.binLocation,
                    inventoryItem: availableItem.inventoryItem,
                    transaction: transaction,
            )
            transaction.addToTransactionEntries(transactionEntry)
        }

        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }
        return transaction
    }
}
