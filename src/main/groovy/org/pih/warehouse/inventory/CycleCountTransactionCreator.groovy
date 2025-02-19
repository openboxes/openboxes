package org.pih.warehouse.inventory

import grails.validation.ValidationException
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Product

/**
 * Component responsible for creating and persisting the Transactions that result from a cycle count.
 */
@Component
class CycleCountTransactionCreator {

    ProductAvailabilityService productAvailabilityService
    ProductInventorySnapshotTaker productInventorySnapshotTaker
    TransactionIdentifierService transactionIdentifierService

    /**
     * Given a completed cycle count, will create and persist the resulting product inventory snapshot transaction,
     * as well as the credit/debit adjustment transaction if there are discrepancies.
     *
     * @param itemQuantityOnHandIsUpToDate
     */
    List<Transaction> createTransactions(CycleCount cycleCount, boolean itemQuantityOnHandIsUpToDate=false) {
        List<Transaction> transactions = []
        Date transactionDate = new Date()

        Transaction productInventoryTransaction = createProductInventoryTransaction(cycleCount, transactionDate)
        transactions.add(productInventoryTransaction)

        Transaction adjustmentTransaction = createAdjustmentTransaction(
                cycleCount, transactionDate, itemQuantityOnHandIsUpToDate)
        if (adjustmentTransaction) {
            transactions.add(adjustmentTransaction)
        }

        return transactions
    }

    private Transaction createAdjustmentTransaction(
            CycleCount cycleCount, Date transactionDate, boolean itemQuantityOnHandIsUpToDate) {
        // We only care about the cycle count items from the most recent count. We create the entries first to avoid
        // needlessly creating the transaction itself if there are no discrepancies.
        List<TransactionEntry> entries = []
        for (CycleCountItem cycleCountItem : cycleCount.itemsOfMostRecentCount) {
            if (cycleCountItem.quantityCounted == null) {
                continue
            }

            // We need to compare the quantity counted against the most up to date QoH in product availability.
            // However, if the cycle count items already have an up to date QoH (which will be the case if
            // refreshQuantityOnHand == true when submitting the count), we don't need to re-compute it.
            int actualQuantityOnHand
            if (itemQuantityOnHandIsUpToDate) {
                actualQuantityOnHand = cycleCountItem.quantityOnHand
            }
            // While the final QoH of the product for each [bin location + lot number] will always be the same value
            // as was counted, if the QoH in the cycle count items is not up to date, and we have to re-compute it from
            // product availability here, the amount of quantity adjustment/change might be different than what was
            // displayed to the user during the count.
            else {
                // TODO: This doesn't account for any new bins/lots that have been created since the count started! We
                //       need a full QoH fetch on the product, and to create new cycle count items for any new bins/lot
                //       numbers.
                Integer quantityFromProductAvailability = productAvailabilityService.getQuantityOnHandInBinLocation(
                        cycleCountItem.inventoryItem, cycleCountItem.location) as Integer
                actualQuantityOnHand = quantityFromProductAvailability ? quantityFromProductAvailability : 0
            }

            int discrepancyAmount = cycleCountItem.quantityCounted - actualQuantityOnHand
            if (discrepancyAmount == 0) {
                continue
            }

            TransactionEntry transactionEntry = new TransactionEntry(
                    // If discrepancyAmount > 0, we're making a credit/positive adjustment.
                    // If discrepancyAmount < 0, we're making a debit/negative adjustment.
                    quantity: discrepancyAmount,
                    binLocation: cycleCountItem.location,
                    inventoryItem: cycleCountItem.inventoryItem,
            )
            entries.add(transactionEntry)
        }

        if (!entries) {
            return null
        }

        // Now that we know there is at least one discrepancy, create the transaction itself and map it to the entries.
        TransactionType transactionType = TransactionType.read(Constants.CYCLE_COUNT_ADJUSTMENT_TRANSACTION_TYPE_ID)
        Transaction transaction = new Transaction(
                source: cycleCount.facility,
                inventory: cycleCount.facility.inventory,
                transactionDate: transactionDate,
                transactionType: transactionType,
                cycleCount: cycleCount,
        )
        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        for (TransactionEntry transactionEntry : entries) {
            transactionEntry.transaction = transaction
            transaction.addToTransactionEntries(transactionEntry)
        }

        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }
        return transaction
    }

    private Transaction createProductInventoryTransaction(CycleCount cycleCount, Date transactionDate) {
        // Currently, all items of a count are for the same product, so we can get the product by simply looking at
        // the first item. When this requirement changes, we'll need to pass in a list of products here.
        Product product = cycleCount.cycleCountItems[0].product

        // Creates a "snapshot style" product inventory transaction. We do this because we want any changes in
        // quantity to be represented by a separate adjustment transaction. It's important to note that the quantity
        // values for this transaction will be a pure copy of QoH in product availability, NOT the quantityOnHand of
        // the cycle count items.
        return productInventorySnapshotTaker.createTransaction(
                cycleCount.facility,
                product,
                ProductInventorySnapshotSource.CYCLE_COUNT,
                cycleCount,
                transactionDate)
    }
}
