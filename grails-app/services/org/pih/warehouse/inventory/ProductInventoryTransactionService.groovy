package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.product.availability.AvailableItemKey
import org.pih.warehouse.product.Product

/**
 * Responsible for managing product inventory transactions. To be extended by feature-specific implementations.
 */
@Transactional
abstract class ProductInventoryTransactionService<T> {

    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService
    TransactionIdentifierService transactionIdentifierService

    /**
     * Transactions will often have a source object (not to be confused with the "source" field) that is responsible
     * for the creation of the transaction. This method sets that source object on the given Transaction.
     */
    abstract void setSourceObject(Transaction transaction, T transactionSource)

    /**
     * Returns true if baseline transactions are enabled for the feature. Defaults to enabled.
     * To be overwritten by feature-specific implementations that provide their own feature-switch.
     */
    protected boolean baselineTransactionsEnabled() {
        return true
    }

    /**
     * Create a new "inventory baseline" product inventory transaction based on the current QoH in product availability.
     *
     * If you already have access to available items, it's better to use createInventoryBaselineTransactionForGivenStock
     * and pass them in to avoid needlessly re-computing computing QoH / available items (which can be slow).
     *
     * @param facility The Location to take the baseline snapshot at
     * @param sourceObject The source object that caused the Transaction. Ex: CycleCount, Order, Requisition...
     * @param products The Products to take the baseline snapshot for
     * @param transactionDate The datetime that the transaction should be marked with. If left blank will be
     *                        the current time.
     * @param comment An optional comment to associate with the transaction
     * @param transactionEntriesComments A map of transaction entry comments keyed on AvailableItemKey
     * @param validateTransactionDates An optional param to disable validation of transactions at the same time
     *                                 (Used when for some reason we want to allow multiple transactions at the
     *                                 same time). By default it's true.
     * @param disableRefresh An optional param to disable the automatic refresh of the product availability when
     *                       the transaction is created. By default it's false.
     * @return The Transaction that was created
     */
    Transaction createInventoryBaselineTransaction(
            Location facility,
            T sourceObject,
            Collection<Product> products,
            Date transactionDate=null,
            String comment=null,
            Map<AvailableItemKey, String> transactionEntriesComments = [:],
            Boolean validateTransactionDates = true,
            Boolean disableRefresh = false
    ) {

        List<AvailableItem> availableItems = productAvailabilityService.getAvailableItemsAtDate(
                facility, products, transactionDate)

        createInventoryBaselineTransactionForGivenStock(
                facility,
                sourceObject,
                products,
                availableItems,
                transactionDate,
                comment,
                transactionEntriesComments,
                validateTransactionDates,
                disableRefresh,
        )
    }

    /**
     * Create a new "inventory baseline" product inventory transaction based on the given product availability.
     *
     * We refer to this transaction as a "baseline" because it gives us two things:
     *
     * 1) It sets a new baseline for the stock of the product. Any transactions on the product that are backdated to
     *    before the most recent baseline will NOT have any effect on quantity on hand.
     *
     * 2) It acts as a "snapshot" of what the quantity on hand (determined by product availability) was for each
     *    [bin location + lot number] of the product at the time of the transaction. As such, transactions created via
     *    this method should NOT result in any quantity changes/adjustments. (However if transactions are backdated to
     *    before the baseline, the baseline will have an implied quantity adjustment.)
     *
     * @param facility The Location to take the baseline snapshot at
     * @param sourceObject The source object that caused the Transaction. Ex: CycleCount, Order, Requisition...
     * @param products The products to take a baseline snapshot for
     * @param availableItems The stock to take a baseline snapshot against.
     * @param transactionDate The datetime that the transaction should be marked with. If left blank will be
     *                        the current time.
     * @param comment An optional comment to associate with the transaction
     * @param transactionEntriesComments A map of transaction entry comments keyed on AvailableItemKey
     * @param validateTransactionDates An optional param to disable validation of transactions at the same time
     *                                 (Used when for some reason we want to allow multiple transactions at the
     *                                 same time). By default it's true.
     * @param disableRefresh An optional param to disable the automatic refresh of the product availability when
     *                       the transaction is created. By default it's false.
     * @return The Transaction that was created
     */
    Transaction createInventoryBaselineTransactionForGivenStock(
            Location facility,
            T sourceObject,
            Collection<Product> products,
            Collection<AvailableItem> availableItems,
            Date transactionDate=null,
            String comment=null,
            Map<AvailableItemKey, String> transactionEntriesComments = [:],
            boolean validateTransactionDates = true,
            boolean disableRefresh = false
    ) {

        if (!baselineTransactionsEnabled()) {
            return null
        }

        // We'd have weird behaviour if we allowed two transactions to exist at the same exact time (precision at the
        // database level is to the second) so fail if there's already a transaction on the items for the given date.
        Date actualTransactionDate = transactionDate ?: new Date()
        if (validateTransactionDates && inventoryService.hasTransactionEntriesOnDate(
                facility, actualTransactionDate, products as List<Product>)) {
            throw new IllegalArgumentException("A transaction already exists at time ${actualTransactionDate}")
        }

        // If there are no available items, there is no stock for the products. We still want to create a baseline
        // though so create one representing zero stock for the products in the default lot and bin.
        Transaction transaction
        if (!availableItems) {
            transaction = initEmptyBaselineTransaction(
                    facility,
                    sourceObject,
                    products,
                    actualTransactionDate,
                    comment,
                    transactionEntriesComments,
                    disableRefresh,
            )
        }
        // Otherwise we do have some stock for the products so create the baseline as normal.
        else {
            transaction = initNonEmptyBaselineTransaction(
                    facility,
                    sourceObject,
                    availableItems,
                    actualTransactionDate,
                    comment,
                    transactionEntriesComments,
                    disableRefresh,
            )
        }

        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }
        return transaction
    }

    /**
     * Initializes (but does not persist) an "empty" baseline transaction containing one entry for each of the given
     * products that sets zero stock/quantity in the default bin and lot.
     */
    private Transaction initEmptyBaselineTransaction(Location facility,
                                                     T sourceObject,
                                                     Collection<Product> products,
                                                     Date transactionDate,
                                                     String comment,
                                                     Map<AvailableItemKey, String> transactionEntriesComments,
                                                     boolean disableRefresh) {

        Transaction transaction = initTransaction(facility, sourceObject, transactionDate, comment, disableRefresh)

        for (Product product in products) {
            InventoryItem defaultInventoryItem = inventoryService.findOrCreateDefaultInventoryItem(product)
            TransactionEntry transactionEntry = new TransactionEntry(
                    quantity: 0,
                    product: product,
                    binLocation: null,
                    inventoryItem: defaultInventoryItem,
                    transaction: transaction,
                    comments: transactionEntriesComments?.get(new AvailableItemKey(null, defaultInventoryItem)),
            )
            transaction.addToTransactionEntries(transactionEntry)
        }

        return transaction
    }

    /**
     * Initializes (but does not persist) a baseline transaction containing one entry for each available item.
     */
    private Transaction initNonEmptyBaselineTransaction(Location facility,
                                                        T sourceObject,
                                                        Collection<AvailableItem> availableItems,
                                                        Date transactionDate,
                                                        String comment,
                                                        Map<AvailableItemKey, String> transactionEntriesComments,
                                                        boolean disableRefresh) {

        Transaction transaction = initTransaction(facility, sourceObject, transactionDate, comment, disableRefresh)

        for (AvailableItem availableItem : availableItems) {
            TransactionEntry transactionEntry = new TransactionEntry(
                    quantity: availableItem.quantityOnHand,
                    product: availableItem.inventoryItem.product,
                    binLocation: availableItem.binLocation,
                    inventoryItem: availableItem.inventoryItem,
                    transaction: transaction,
                    comments: transactionEntriesComments?.get(new AvailableItemKey(availableItem)),
            )
            transaction.addToTransactionEntries(transactionEntry)
        }

        return transaction
    }

    /**
     * Initializes (but does not persist) a Transaction instance for the baseline.
     */
    private Transaction initTransaction(Location facility,
                                        T sourceObject,
                                        Date transactionDate,
                                        String comment,
                                        boolean disableRefresh) {

        TransactionType transactionType = TransactionType.read(Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID)

        Transaction transaction = new Transaction(
                inventory: facility.inventory,
                transactionDate: transactionDate,
                transactionType: transactionType,
                comment: comment,
                disableRefresh: disableRefresh,
        )
        transaction.transactionNumber = transactionIdentifierService.generate(transaction)
        setSourceObject(transaction, sourceObject)

        return transaction
    }
}
