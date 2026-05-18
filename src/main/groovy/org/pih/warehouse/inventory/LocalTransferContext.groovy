package org.pih.warehouse.inventory

/**
 * Pre-resolved context used by the stock history loop so that it never has to
 * lazy-load LocalTransfer back-refs or Transaction.getOtherTransaction() per row.
 *
 * All maps are keyed by transaction id (String).
 */
class LocalTransferContext {

    /** For a given transaction id, the "other side" Transaction in the LocalTransfer pair. */
    Map<String, Transaction> otherTransactionById = [:]

    /** TransactionEntries keyed by transaction id (already filtered to the current product). */
    Map<String, List<TransactionEntry>> entriesByTransactionId = [:]

    /**
     * Source/destination transaction id + transactionType pairs, used by the GSP
     * isInternal branch. Pre-resolved so the view never has to call
     * transaction.localTransfer (which would lazy-load).
     */
    Map<String, LocalTransferInfo> localTransferInfoByTransactionId = [:]

    boolean isInternal(String transactionId) {
        otherTransactionById.containsKey(transactionId)
    }
}
