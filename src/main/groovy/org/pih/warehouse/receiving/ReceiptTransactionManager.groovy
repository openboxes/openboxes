package org.pih.warehouse.receiving

import grails.validation.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.api.receiving.v2.ReceiptV2Service
import org.pih.warehouse.core.Constants
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryItemManager
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionAction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.inventory.TransactionSource
import org.pih.warehouse.inventory.TransactionType
import org.pih.warehouse.shipping.Shipment

/**
 * Manages the lifecycle (recording and rolling back) of the stock {@link Transaction} of a {@link Receipt}, and of
 * the {@link TransactionSource} recording the action that produced it.
 *
 * Writes within the transaction of the caller, so it is the calling endpoint that decides what a failure rolls
 * back (see {@link ReceiptV2Service#completeReceipt}).
 */
@Component
class ReceiptTransactionManager {

    private final TransactionIdentifierService transactionIdentifierService
    private final InventoryItemManager inventoryItemManager

    ReceiptTransactionManager(@Autowired final TransactionIdentifierService transactionIdentifierService,
                              @Autowired final InventoryItemManager inventoryItemManager) {
        this.transactionIdentifierService = transactionIdentifierService
        this.inventoryItemManager = inventoryItemManager
    }

    /**
     * Records the inbound stock transaction of a completed receipt: one entry per line that received something,
     * crediting the received quantities to the destination's inventory. Lines completed with nothing received get
     * no entry.
     */
    Transaction createInboundTransaction(Receipt receipt) {
        Shipment shipment = receipt.shipment
        if (!shipment.destination?.inventory) {
            throw new IllegalStateException(
                    "Destination ${shipment.destination?.name} must have an inventory in order to receive stock")
        }

        Transaction transaction = new Transaction(
                transactionType: TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID),
                incomingShipment: shipment,
                requisition: shipment.requisition,
                receipt: receipt,
                source: shipment.origin,
                destination: null,
                inventory: shipment.destination.inventory,
                transactionDate: receipt.actualDeliveryDate,
                transactionSource: createTransactionSource(receipt),
        )
        transaction.transactionNumber = transactionIdentifierService.generate(transaction)

        // Do not include not touched items in the transaction entries. Since we create original items for
        // every line, in any partial receipt we would create an entry even if we don't really receive an item
        Set<ReceiptItem> receivedItems = receipt.receiptItems.findAll { ReceiptItem receiptItem ->
            (receiptItem.quantityReceived ?: 0) > 0
        }

        receivedItems.each { ReceiptItem receiptItem ->
            // Receipt items created via the v2 endpoints always carry an inventory item, but fall back to resolving
            // one from the lot fields to support receipts against legacy shipment items that never had one.
            InventoryItem inventoryItem = receiptItem.inventoryItem ?: inventoryItemManager.getOrCreateInventoryItem(
                    receiptItem.product, receiptItem.lotNumber, receiptItem.expirationDate)

            transaction.addToTransactionEntries(new TransactionEntry(
                    quantity: receiptItem.quantityReceived,
                    binLocation: receiptItem.binLocation,
                    inventoryItem: inventoryItem,
            ))
        }

        // Block the refresh of the product availability table (to be triggered at the end of the request)
        transaction.disableRefresh = Boolean.TRUE

        if (!transaction.save(flush: true)) {
            throw new ValidationException(
                    "Failed to receive shipment due to error while saving transaction", transaction.errors)
        }

        // Associate the incoming transaction with the shipment
        shipment.addToIncomingTransactions(transaction)
        shipment.disableRefresh = false
        if (!shipment.save(flush: true)) {
            throw new ValidationException("Shipment is invalid", shipment.errors)
        }

        return transaction
    }

    /**
     * Records the action that produced the inbound transaction of a completed receipt (see {@link TransactionSource}):
     * the receipt and the shipment (with its requisition) it was received against, between the two locations the
     * stock moved.
     */
    private TransactionSource createTransactionSource(Receipt receipt) {
        Shipment shipment = receipt.shipment

        TransactionSource transactionSource = new TransactionSource(
                transactionAction: TransactionAction.RECEIPT,
                receipt: receipt,
                shipment: shipment,
                requisition: shipment.requisition,
                origin: shipment.origin,
                destination: shipment.destination,
        )

        if (!transactionSource.save()) {
            throw new ValidationException("Transaction source is invalid", transactionSource.errors)
        }

        return transactionSource
    }

    /**
     * Deletes the transaction sources of the given receipts, for those that have one (see
     * {@link #createTransactionSource}). Must be called before deleting a receipt, for the same reason as
     * {@link ReceiptV2Service#deleteMarkersForReceipts}: a transaction source's foreign key blocks the deletion of
     * the receipt it points at.
     */
    void deleteTransactionSourcesForReceipts(Collection<Receipt> receipts) {
        if (!receipts) {
            return
        }

        List<TransactionSource> transactionSources = TransactionSource.findAllByReceiptInList(receipts.toList())
        if (!transactionSources) {
            return
        }

        // Not likely: the rollback flows we use delete the transaction first. Just in case one gets here with it
        // still alive, let go of the reference - its foreign key would block the delete below.
        List<Transaction> stampedTransactions = Transaction.findAllByTransactionSourceInList(transactionSources)
        for (Transaction transaction : stampedTransactions) {
            transaction.transactionSource = null
        }

        for (TransactionSource transactionSource : transactionSources) {
            transactionSource.delete()
        }
    }

    /**
     * Deletes the transaction source of the given receipt, if it has one. See
     * {@link #deleteTransactionSourcesForReceipts} for when to call it.
     */
    void deleteTransactionSourceForReceipt(Receipt receipt) {
        deleteTransactionSourcesForReceipts([receipt])
    }
}
