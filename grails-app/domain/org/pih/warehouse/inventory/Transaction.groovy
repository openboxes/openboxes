package org.pih.warehouse.inventory;

import java.util.Date;

import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.core.User

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

    Warehouse source	    		
    Warehouse destination					    		 
	Date transactionDate	    		// Date entered into the warehouse
    TransactionType transactionType 	// Detailed transaction type (e.g. Order, Transfer, Stock Count)
	
	// Auditing fields
	Boolean confirmed = Boolean.FALSE;	// Transactions need to be confirmed by a supervisor
	User confirmedBy
	User createdBy
	Date dateCreated
	Date lastUpdated
	Date dateConfirmed
	
    // Association mapping
    static hasMany = [ transactionEntries : TransactionEntry ]
    static belongsTo = [ inventory : Inventory ]

    // Constraints 
    static constraints = {
	    transactionDate(nullable:false)
	    transactionType(nullable:true)
	    source(nullable:true)
	    destination(nullable:true)
		createdBy(nullable:true)
		confirmed(nullable:true)
		confirmedBy(nullable:true)
		dateConfirmed(nullable:true)
    }
}
