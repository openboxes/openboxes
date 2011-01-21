package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;

class TransactionEntry {
	
	Product product					// Optional
	String lotNumber				// Optional 
    Integer quantity				// Convention: negative number means OUT, positive number means IN
	InventoryItem inventoryItem		// The inventory item (or product being tracked)
	String comments					// 
	
    static belongsTo = [ transaction : Transaction ]

    static constraints = {		
		inventoryItem(nullable:false)		
		product(nullable:false)	
		lotNumber(nullable:true)
		quantity(nullable:false)
		comments(nullable:true)	
    }
}
