package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.core.ConfigService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.product.availability.AvailableItemKey
import org.pih.warehouse.product.Product

/**
 * Responsible for managing product inventory transactions for the old product inventory transaction migration
 * to the new Inventory Baseline and Adjustment pair
 */
@Transactional
class ProductInventoryTransactionMigrationService extends ProductInventoryTransactionService<ImportDataCommand> {

    ConfigService configService

    @Override
    void setSourceObject(Transaction transaction, ImportDataCommand sourceObject) {
        // Transaction migration has no source object.
    }

    @Override
    protected boolean baselineTransactionsEnabled() {
        return configService.getProperty("openboxes.transactions.inventoryBaseline.migration.enabled", Boolean)
    }

    // TODO: Revert this code in favour of the solution in https://github.com/openboxes/openboxes/pull/5517.
    //       This method is mostly a copy of the method in ProductInventoryTransactionService with the changes from PR
    //       5517. It is meant to be a temporary fix to OBPIH-7514 for the 0.9.5-hotfix3 release so that we can make
    //       as small of a change as possible for the hotfix (we don't want to impact the other features that create
    //       baselines). PR 5517 is targeted for 0.9.6 and so once 0.9.5-hotfix3 is completed, we should revert this.
    Transaction createBaselineTransactionForMigration(
            Location facility,
            ImportDataCommand sourceObject,
            List<Product> products,
            Collection<AvailableItem> availableItems,
            Date transactionDate=null,
            String comment=null,
            Map<AvailableItemKey, String> transactionEntriesComments = [:],
            validateTransactionDates = true,
            disableRefresh = false
    ) {

        if (!baselineTransactionsEnabled()) {
            return null
        }

        TransactionType transactionType = TransactionType.read(Constants.INVENTORY_BASELINE_TRANSACTION_TYPE_ID)

        // We'd have weird behaviour if we allowed two transactions to exist at the same exact time (precision at the
        // database level is to the second) so fail if there's already a transaction on the items for the given date.
        Date actualTransactionDate = transactionDate ?: new Date()
        if (validateTransactionDates && inventoryService.hasTransactionEntriesOnDate(facility, actualTransactionDate, products)) {
            throw new IllegalArgumentException("A transaction already exists at time ${actualTransactionDate}")
        }

        Transaction transaction = new Transaction(
                inventory: facility.inventory,
                transactionDate: actualTransactionDate,
                transactionType: transactionType,
                comment: comment,
                disableRefresh: disableRefresh
        )

        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        setSourceObject(transaction, sourceObject)

        for (Product product in products) {
            // If the product does not have any stock at the time of the baseline, we still want to take a "snapshot"
            // of the product so add an entry representing zero stock for the products in the default lot and bin.
            List<AvailableItem> availableItemsForProduct = availableItems?.findAll { it?.inventoryItem?.product == product }
            if (!availableItemsForProduct) {
                InventoryItem defaultInventoryItem = inventoryService.findOrCreateInventoryItem(product, null, null)
                TransactionEntry transactionEntry = new TransactionEntry(
                        quantity: 0,
                        product: product,
                        binLocation: null,
                        inventoryItem: defaultInventoryItem,
                        transaction: transaction,
                        comments: transactionEntriesComments?.get(new AvailableItemKey(null, defaultInventoryItem)),
                )
                transaction.addToTransactionEntries(transactionEntry)
                continue
            }

            // Otherwise we do have stock for the product at that time so use it to build the transaction entries.
            for (AvailableItem availableItem in availableItemsForProduct) {
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
        }
        if (!transaction.save()) {
            throw new ValidationException("Invalid transaction", transaction.errors)
        }
        return transaction
    }
}
