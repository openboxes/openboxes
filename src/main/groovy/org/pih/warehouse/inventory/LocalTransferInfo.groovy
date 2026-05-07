package org.pih.warehouse.inventory

/**
 * Pre-resolved scalars for the source / destination side of a LocalTransfer pair.
 * Surfaced on each stock history row whose transaction participates in a transfer
 * so the GSP renders the "from / to" links without lazy-loading the LocalTransfer.
 */
class LocalTransferInfo {

    String sourceTransactionId
    TransactionType sourceTransactionType

    String destinationTransactionId
    TransactionType destinationTransactionType
}
