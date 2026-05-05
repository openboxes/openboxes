package org.pih.warehouse.inventory

/**
 * FK ids (requisition / order) read off the Transaction row via HQL projection,
 * so we never materialise Requisition / Order entities (which would otherwise
 * trigger the eager one-to-one load on Picklist).
 *
 * Both maps are keyed by transaction id; entries are present only when the
 * transaction has the corresponding FK set.
 */
class TransactionRefIds {

    Map<String, String> requisitionIdByTransactionId = [:]
    Map<String, String> orderIdByTransactionId = [:]
}
