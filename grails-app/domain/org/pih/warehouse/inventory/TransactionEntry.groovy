package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;

class TransactionEntry {
	
	Product product					// Optional
	String lotNumber				// Optional 
    Integer quantity				// Convention: negative number means OUT, positive number means IN
	InventoryItem inventoryItem		// The inventory item (or product being tracked)
	
    static belongsTo = [ transaction : Transaction ]

    static constraints = {
		//product(nullable:false)
		inventoryItem(nullable:false)		
		quantity(nullable:false)
		product(nullable:true)	
		lotNumber(nullable:true)
		
    }
}
