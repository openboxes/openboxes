package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import java.time.Instant

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

