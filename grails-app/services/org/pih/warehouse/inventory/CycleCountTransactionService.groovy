package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Product

/**
 * Responsible for creating and persisting the Transactions that result from a cycle count.
 */
@Transactional
class CycleCountTransactionService {

    CycleCountProductAvailabilityService cycleCountProductAvailabilityService
    ProductAvailabilityService productAvailabilityService
    ProductInventoryTransactionService productInventoryTransactionService
    TransactionIdentifierService transactionIdentifierService

    /**
     * Given a completed cycle count, will create and persist the resulting product inventory transaction,
     * as well as the credit/debit adjustment transaction if there are discrepancies.
     *
     * @param cycleCount the cycle count to create the transactions from
     * @param itemQuantityOnHandIsUpToDate true if the cycle count items already have accurate QoH (and so don't
     *                                     need to be refreshed).
     */
    List<Transaction> createTransactions(CycleCount cycleCount, boolean itemQuantityOnHandIsUpToDate=false) {
        List<Transaction> transactions = []
        Date transactionDate = new Date()

        List<Transaction> productInventoryTransactions = createProductInventoryTransactions(cycleCount, transactionDate)
        transactions.addAll(productInventoryTransactions)

        Transaction adjustmentTransaction = createAdjustmentTransaction(
                cycleCount, transactionDate, itemQuantityOnHandIsUpToDate)
        if (adjustmentTransaction) {
            transactions.add(adjustmentTransaction)
        }

        return transactions
    }

    private Transaction createAdjustmentTransaction(
            CycleCount cycleCount, Date transactionDate, boolean itemQuantityOnHandIsUpToDate) {

        // We need to compare the quantity counted against the most up to date QoH in product availability.
        // However, if the cycle count items already have an up to date QoH (which will be the case if
        // refreshQuantityOnHand == true when submitting the count), we don't need to re-compute it.
        if (!itemQuantityOnHandIsUpToDate) {
            // While the quantity that we end up with for each [product + bin location + lot number] will always be
            // the same as the quantityCounted, if this refresh results in the QoH in the cycle count items changing,
            // the quantityVariance will also change, as will the quantity of the adjustment transaction.
            // Ex: QoH before == 10, quantity counted == 10 -> adjustment quantity and quantityVariance == 0
            //     QoH after  == 20, quantity counted == 10 -> adjustment quantity and quantityVariance == -10
            // This means the quantity adjustment that we end up with will be different than the quantityVariance that
            // was displayed to the user during the count.
            cycleCountProductAvailabilityService.refreshProductAvailability(cycleCount)
        }

        // We only care about the cycle count items from the most recent count.
        List<TransactionEntry> entries = []
        for (CycleCountItem cycleCountItem : cycleCount.itemsOfMostRecentCount) {
            if (cycleCountItem.quantityCounted == null) {
                continue
            }

            Integer quantityVariance = cycleCountItem.quantityVariance
            if (quantityVariance == null || quantityVariance == 0) {
                continue
            }

            // We create the entries first to avoid needlessly creating the transaction if there are no discrepancies.
            TransactionEntry transactionEntry = new TransactionEntry(
                    // If quantityVariance > 0, we're making a credit/positive adjustment.
                    // If quantityVariance < 0, we're making a debit/negative adjustment.
                    quantity: quantityVariance,
                    binLocation: cycleCountItem.location,
                    inventoryItem: cycleCountItem.inventoryItem,
                    product: cycleCountItem.product,
            )
            entries.add(transactionEntry)
        }

        if (!entries) {
            return null
        }

        // Now that we know there is at least one discrepancy, create the transaction itself and map it to the entries.
        TransactionType transactionType = TransactionType.read(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
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

    private List<Transaction> createProductInventoryTransactions(CycleCount cycleCount, Date transactionDate) {
        List<Transaction> transactions = []

        // A cycle count can count multiple products. Each product needs their own product inventory transaction.
        for (Product product : cycleCount.products) {
            // Creates a "snapshot style" product inventory transaction. We do this because we want any changes in
            // quantity to be represented by a separate adjustment transaction. It's important to note that the quantity
            // values for this transaction will be a pure copy of QoH in product availability, NOT the quantityOnHand of
            // the cycle count items.
            Transaction transaction = productInventoryTransactionService.createTransaction(
                    cycleCount.facility,
                    product,
                    ProductInventorySnapshotSource.CYCLE_COUNT,
                    cycleCount,
                    transactionDate)

            transactions.add(transaction)
        }

        return transactions
    }
}
