package org.pih.warehouse.inventory;

import java.util.Date;

import org.pih.warehouse.auth.AuthService;
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User
import org.pih.warehouse.core.Constants

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
class Transaction implements Comparable, Serializable {

	def beforeInsert = {
		createdBy = AuthService.currentUser.get()
	}
	def beforeUpdate ={
		//lastUpdatedBy = AuthService.currentUser.get
	}
	
	def localizationService
	String id
    Location source	    		
    Location destination					    		 
	Date transactionDate	    		// Date entered into the warehouse
	Shipment outgoingShipment			// Outgoing shipment associated with a transfer out transasction
	Shipment incomingShipment			// Incoming shipment associated with a transfer in transasction
    TransactionType transactionType 	// Detailed transaction type (e.g. Order, Transfer, Stock Count)
	String comment
	
	
	// Auditing fields
	Boolean confirmed = Boolean.FALSE;	// Transactions need to be confirmed by a supervisor
	User confirmedBy
	User createdBy
	Date dateCreated
	Date lastUpdated
	Date dateConfirmed
	List transactionEntries;
	
    // Association mapping
    static hasMany = [ transactionEntries : TransactionEntry ]
    static belongsTo = [ inventory : Inventory ]

	static mapping = { 
		id generator: 'uuid'
	}
	
	// Transient attributs
	static transients = ['locationizationService']
	
    // Constraints 
    static constraints = {
	    transactionType(nullable:false)
		createdBy(nullable:true)
		outgoingShipment(nullable:true)
		incomingShipment(nullable:true)
		confirmed(nullable:true)
		confirmedBy(nullable:true)
		dateConfirmed(nullable:true)
		comment(nullable:true)
		transactionDate(nullable:false, 
			validator: { value -> value < new Date() })  // transaction date cannot be in the future
		
		source(nullable:true, 
			validator: { value, obj-> 
							if (value && obj.destination) { return false }   // transaction cannot have both a source and a destination
							if (value && obj.inventory?.warehouse == value) { return false }   // source warehouse can't be the same as transaction warehouse
							if (obj.transactionType?.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID && !value) { return false } // transfer in transaction must have source
							return true 
						})
	    
		destination(nullable:true, 
			validator: { value, obj-> 
							if (value && obj.source) { return false }  // transaction cannot have both a source and a destination
							if (value && obj.inventory?.warehouse == value) { return false } // destination warehouse can't be the same as transaction warehouse
							if (obj.transactionType?.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID && !value) { return false } // transfer out transaction must have destination
							return true 
						})
    }
	
	String transactionNumber() {
		return (id) ? "T" + String.valueOf(id).padLeft(6, "0")  : "(new transaction)";
	}
	
	String label() { 
		String label = "";
		label += localizationService.formatMetadata(transactionType)		
		label += (inventory) ? " - " + localizationService.formatMetadata(inventory) : ""
		//label += (destination) ? " - " + localizationService.formatMetadata(destination) : ""
		//label += (source) ? " - " + localizationService.formatMetadata(source) : ""
		label += (transactionDate) ? " - " + localizationService.formatDate(transactionDate) : ""
		return label
	}
	
    
    /**
	 * Sort by transaction date, and then by date created
	 * (Note that sorting of transaction entries, and therefore the whole inventory process, relies on this
	 *  so don't make changes lightly!)
	 */
	int compareTo(obj) { 
		def compare = transactionDate <=> obj.transactionDate		
		if (compare == 0) {
			compare = dateCreated <=> obj.dateCreated
		}
		return compare
	}
}
