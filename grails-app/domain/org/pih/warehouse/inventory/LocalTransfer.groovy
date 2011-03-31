package org.pih.warehouse.inventory

import java.util.Date;

import org.pih.warehouse.core.Constants;


/**
 * This class is meant to link together two transactions--a Transfer Out and a Transfer In--that
 * represent a transfer of goods between two locally-managed warehouses.  This is for when we want to 
 * create a direct, immediate transfer, as opposed to transferring via a Shipment 
 * 
 * Use the following methods within InventorySevice to create, modify, and delete LocalTransfers:
 *  getLocalTrasnfer(Tranasction)
 *  isLocalTransfer(Transaction)
 *  isValidForLocalTransfer(Transaction)
 *  deleteLocalTransfer(Transaction)
 *  saveLocalTransfer(Transaction)
 *  
 * These methods make sure the two linked transactions stay in sync. See docs on these methods for more information.
 * 	
 */
class LocalTransfer {
    // Core data elements
    Transaction sourceTransaction
    Transaction destinationTransaction
    
	// Auditing
	Date dateCreated;
	Date lastUpdated;
		
    String toString() { return "source = ${sourceTransaction}, destination = ${destinationTransaction}" }
	
    // Constraints
    static constraints = {
		sourceTransaction(nullable:false, unique:true,
			validator: { transaction -> transaction.transactionType.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID } )
		
		destinationTransaction(nullable:false, unique:true,
			validator: { transaction -> transaction.transactionType.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID } )
    }
    
	static mapping = {
		sourceTransaction cascade: "all-delete-orphan"
		destinationTransaction cascade: "all-delete-orphan"
	}	
}
