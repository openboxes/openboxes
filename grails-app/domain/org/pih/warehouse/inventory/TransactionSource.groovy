package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import java.time.Instant

/**
 * Maps transactions to the source entity that created them. Serves multiple purposes:
 *
 * 1. Grouping together transactions. Transactions that were created from the same action will share the same
 *    transaction source.
 *
 * 2. Logging the history of transaction-creating actions. Not all transaction actions will result in a transaction
 *    being created (ex: a cycle count that has no discrepancies/adjustments and baseline transactions are disabled).
 *    This table creates a historical record that maps the action to the source entity that triggered the action,
 *    so that we know when a specific action occurred, even when no transaction was created.
 */
class TransactionSource {

    String id
    TransactionAction transactionAction

    Shipment shipment
    Requisition requisition
    Receipt receipt
    Order order
    CycleCount cycleCount
    Location origin
    Location destination

    Instant dateCreated
    Instant lastUpdated

    User createdBy
    User updatedBy

    static mapping = {
        id generator: 'uuid'
        version false
    }

    static constraints = {
        shipment(nullable: true)
        requisition(nullable: true)
        receipt(nullable: true)
        order(nullable: true)
        cycleCount(nullable: true)
        origin(nullable: true)
        destination(nullable: true)
    }

    List<Transaction> getAssociatedTransactions() {
        return Transaction.findAllByTransactionSource(this)
    }
}

