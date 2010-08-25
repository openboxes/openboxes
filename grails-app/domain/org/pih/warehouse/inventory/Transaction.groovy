package org.pih.warehouse.inventory;

import org.pih.warehouse.inventory.Inventory

/**
 *  Represents a unit of work completed within a single warehouse.  A
 *  transaction can be incoming/outgoing and must have a source and
 *  destination.
 *
 *  All warehouse events (like shipments, deliveries) are managed through
 *  transactions.  All warehouse events have two transactions (an incoming
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

    Date transactionDate	    		// Date entered into the warehouse
    TransactionType transactionType 	// Detailed transaction type (e.g. Order, Transfer, Stock Count) 
    Inventory inventory		    		// The inventory to which this is connected
    Warehouse thisWarehouse	    		// The local warehouse that the transaction is 
    Warehouse targetWarehouse	    	// where the transaction is going to / coming from

    // Association mapping
    static hasMany = [ transactionEntries : TransactionEntry ]
    static belongsTo = [ thisWarehouse : Warehouse ]

    // Constraints 
    static constraints = {
	    transactionDate(min:new Date(),nullable:false)
	    transactionType(nullable:true)
	    inventory(nullable:true)	    
	    thisWarehouse(nullable:false)
	    targetWarehouse(nullable:false)
    }
}
