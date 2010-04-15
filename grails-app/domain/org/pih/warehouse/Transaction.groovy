package org.pih.warehouse

/**
 *  Represents a unit of work completed within a single warehouse.  A
 *  transaction can be incoming/outgoing and must have a source and
 *  destination.
 *
 *  All warehouse events (like shipments, deliveries) are managed through
 *  transactions.  All warehouse eventshave two transactions (an incoming
 *  transaction for the destination warehouse and an outgoing transaction
 *  for the source warehouse.
 *
 *  A transaction consists of multiple transaction items (which are
 *  just individual products, that are added to or subtracted from
 *  the inventory).  For every warehouse event, a warehouse
 *  will be represented in two transactions (incoming and outgoing).
 *  
 */
class Transaction {

    Integer id
    Date transactionDate	    // date entered into the system
    WarehouseEvent event	    // order, donation, transfer, correction
    String direction		    // receiving (IN), shipping (OUT)

    
    Warehouse source		    // where the transaction is coming from
    Warehouse destination	    // where the transaction is going

    static constraints = {
    }
}
