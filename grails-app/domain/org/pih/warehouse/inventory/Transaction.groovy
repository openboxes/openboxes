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

    String direction		    		// DEBIT, CREDIT
    Date transactionDate	    		// date entered into the system
    TransactionType transactionType 	// detailed transaction type (similar to event)
    Inventory inventory		    		// the inventory to which this is connected

	// Needs some more thought (origin, destination)    
    Warehouse localWarehouse	    	// where the transaction is 
    Warehouse targetWarehouse	    	// where the transaction is going to / coming from

    // Association mapping
    static hasMany = [ transactionEntries : TransactionEntry ]
    static belongsTo = [ localWarehouse : Warehouse ]

    // Constraints 
    static constraints = {
	    transactionDate(min:new Date(),nullable:false)
	    direction(nullable:true)
	    transactionType(nullable:true)
	    inventory(nullable:true)	    
	    localWarehouse(nullable:false)
	    targetWarehouse(nullable:false)
    }
}
