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

    // Core data elements
    Integer id
    String direction		    // receiving (IN), shipping (OUT)
    Date transactionDate	    // date entered into the system
    TransactionType transactionType // detailed transaction type (similar to event)

    // Other elements to be supported in the future
    Inventory inventory		    // the inventory to which this is connected
    InventoryEvent inventoryEvent   // order, donation, transfer, correction, inventory
   
    // Core associations
    Warehouse localWarehouse	    // local warehouse
    Warehouse targetWarehouse	    // where the transaction is going to / coming from
    //List<TransactionEntry> transactionEntries	    // product-specific entries

    // Association mapping
    static hasMany = [ transactionEntries : TransactionEntry ]
    static belongsTo = [ localWarehouse : Warehouse ]

    // Constraints 
    static constraints = {
	    transactionDate(min:new Date(),nullable:false)
	    direction(nullable:true)
	    transactionType(nullable:true)
	    inventory(nullable:true)
	    inventoryEvent(nullable:true)
	    localWarehouse(nullable:false)
	    targetWarehouse(nullable:false)
	    transactionEntries(nullable:true)
    }
}
