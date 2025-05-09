package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

/**
 * Responsible for managing product inventory transactions. To be extended by feature-specific implementations.
 */
@Transactional
abstract class ProductInventoryTransactionService<T> {

    ProductAvailabilityService productAvailabilityService
    TransactionIdentifierService transactionIdentifierService

    /**
     * Transactions will often have a source object (not to be confused with the "source" field) that is responsible
     * for the creation of the transaction. This method sets that source object on the given Transaction.
     */
    abstract void setSourceObject(Transaction transaction, T transactionSource)

    /**
     * Create a new product inventory transaction based on the current QoH in product availability.
     *
     * We refer to this transaction as a product inventory "snapshot" because it's functionally a copy of what the
     * quantity on hand (determined by product availability) was for each [bin location + lot number] of the product at
     * the time. As such, transactions created via this method should NOT result in any quantity changes/adjustments.
     *
     * @param facility The Location to take the product inventory snapshot at
     * @param product The Product to take the product inventory snapshot for
     * @param sourceObject The source object that caused the Transaction. Ex: CycleCount, Order, Requisition...
     * @param transactionDate The datetime that the transaction should be marked with
     * @return The Transaction that was created
     */
    Transaction createSnapshotTransaction(
            Location facility,
            Product product,
            T sourceObject,
            Date transactionDate=new Date()) {

        TransactionType transactionType = TransactionType.read(Constants.PRODUCT_INVENTORY_SNAPSHOT_TRANSACTION_TYPE_ID)

        Transaction transaction = new Transaction(
                source: facility,
                inventory: facility.inventory,
                transactionDate: transactionDate,
                transactionType: transactionType,
        )

        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        setSourceObject(transaction, sourceObject)

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
