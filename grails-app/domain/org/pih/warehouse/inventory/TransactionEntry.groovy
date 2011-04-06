package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;

class TransactionEntry implements Comparable, Serializable {
	
	Product product					// Optional
	String lotNumber				// Optional 
    Integer quantity				
	InventoryItem inventoryItem		// The inventory item (or product being tracked)
	String comments					// 
	
    static belongsTo = [ transaction : Transaction ]

    static constraints = {		
		inventoryItem(nullable:false)		
		product(nullable:false)	
		lotNumber(nullable:true)
		quantity(nullable:false, min:0)
		comments(nullable:true)	
    }
	
	/**
	 * Sort by the sort parameters of the parent transaction
	 */
	int compareTo(obj) { 
		transaction.compareTo(obj.transaction)
	}
}
