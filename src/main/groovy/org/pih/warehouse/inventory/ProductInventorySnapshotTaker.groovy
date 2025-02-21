package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.springframework.stereotype.Component

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

/**
 * Responsible for creating and persisting product inventory transactions. We call this a "snapshot taker" because
 * product inventory transactions saved via this class are functionally a copy of what the quantity on hand
 * (determined by product availability) was for each [bin location + lot number] combo of the product at the time.
 * Importantly, transactions created via this class should NOT result in any quantity changes/adjustments.
 */
@Component
@Transactional
class ProductInventorySnapshotTaker {

    ProductAvailabilityService productAvailabilityService
    TransactionIdentifierService transactionIdentifierService

    /**
     * Take a new product inventory "snapshot" transaction based on the current product availability.
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

        TransactionType transactionType = TransactionType.read(sourceType.transactionTypeId)

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
