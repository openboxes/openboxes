package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import java.time.Instant

import org.pih.warehouse.DateUtil
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.product.availability.AvailableItemKey
import org.pih.warehouse.product.Product

/**
 * Responsible for creating and persisting the Transactions that result from a cycle count.
 */
@Transactional
class CycleCountTransactionService {

    CycleCountProductAvailabilityService cycleCountProductAvailabilityService
    ProductAvailabilityService productAvailabilityService
    CycleCountProductInventoryTransactionService cycleCountProductInventoryTransactionService
    TransactionIdentifierService transactionIdentifierService
    InventoryService inventoryService

    /**
     * Given a completed cycle count, will create and persist the resulting product inventory transaction,
     * as well as the credit/debit adjustment transaction if there are discrepancies.
     *
     * @param cycleCount the cycle count to create the transactions from
     * @param countedProducts the products being counted (that we should create a baseline transaction for)
     * @param itemQuantityOnHandIsUpToDate true if the cycle count items already have accurate QoH (and so don't
     *                                     need to be refreshed).
     */
    List<Transaction> createTransactions(CycleCount cycleCount,
                                         List<Product> countedProducts,
                                         boolean itemQuantityOnHandIsUpToDate=false) {
        List<Transaction> transactions = []

        // Set the product inventory transaction date to be one second before any adjustment transactions. This
        // transaction date offset is necessary to guarantee that the transactions are applied in the correct order.
        Instant now = Instant.now()
        // -1 (instead of +1 to the adjustment transaction date) because transaction date can't be in the future.
        Date productInventoryTransactionDate = DateUtil.asDate(now.minusSeconds(1))
        Date adjustmentTransactionDate = DateUtil.asDate(now)

        List<Transaction> productInventoryTransactions = createProductInventoryTransactions(
                cycleCount, countedProducts, productInventoryTransactionDate)
        transactions.addAll(productInventoryTransactions)

        Transaction adjustmentTransaction = createAdjustmentTransaction(
                cycleCount, adjustmentTransactionDate, itemQuantityOnHandIsUpToDate)
        if (adjustmentTransaction) {
            transactions.add(adjustmentTransaction)
        }

        return transactions
    }

    private static String buildRecountComment(CycleCountItem cycleCountItem) {
        if (cycleCountItem.comment) {
            return "${cycleCountItem.discrepancyReasonCode ? "[${cycleCountItem.discrepancyReasonCode}]" : ''} ${cycleCountItem.comment}"
        }
        return null
    }

    private Transaction createAdjustmentTransaction(
            CycleCount cycleCount, Date transactionDate, boolean itemQuantityOnHandIsUpToDate) {

        // Reports and QoH calculations get messed up if two transactions for a product exist at the same exact time.
        if (inventoryService.hasTransactionEntriesOnDate(cycleCount.facility, transactionDate, cycleCount.products)) {
            throw new IllegalArgumentException("A transaction already exists at time ${transactionDate}")
        }

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
                    comments: buildRecountComment(cycleCountItem)
            )
            entries.add(transactionEntry)
        }

        if (!entries) {
            return null
        }

        // Now that we know there is at least one discrepancy, create the transaction itself and map it to the entries.
        TransactionType transactionType = TransactionType.read(Constants.ADJUSTMENT_CREDIT_TRANSACTION_TYPE_ID)
        Transaction transaction = new Transaction(
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

    private List<Transaction> createProductInventoryTransactions(CycleCount cycleCount,
                                                                 List<Product> countedProducts,
                                                                 Date transactionDate) {
        List<Transaction> transactions = []
        Map<AvailableItemKey, String> commentsPerCycleCountItem = new HashMap<>()
        cycleCount.cycleCountItems.each {
            // We want to add a comment to product inventory transaction if we know,
            // that no adjustment transaction would be created afterwards, that would contain the comment
            if (it.comment && !it.quantityVariance) {
                commentsPerCycleCountItem.put(new AvailableItemKey(it.location, it.inventoryItem), buildRecountComment(it))
            }
        }
        // A cycle count can count multiple products. Each product needs their own product inventory transaction.
        // We don't loop cycleCount.products because the refresh that happens before a count is submitted will remove
        // all cycle count items that have since had their QoH set to 0 (which can happen if transactions occur on
        // a product while its still being counted). If that happens to all items of a product, the cycle count no
        // longer holds an association to that product (cycle count item holds the relationship to  product). We still
        // want to create a baseline for those products (to record that a count happened), hence the need to loop on
        // the original list of counted products.
        for (Product product : countedProducts) {
            // Creates a "snapshot style" product inventory transaction. We do this because we want any changes in
            // quantity to be represented by a separate adjustment transaction. It's important to note that the quantity
            // values for this transaction will be a pure copy of QoH in product availability, NOT the quantityOnHand of
            // the cycle count items.
            Transaction transaction = cycleCountProductInventoryTransactionService.createInventoryBaselineTransaction(
                    cycleCount.facility,
                    cycleCount,
                    [product],
                    transactionDate,
                    null,
                    commentsPerCycleCountItem
                    )

            transactions.add(transaction)
        }

        return transactions
    }
}
