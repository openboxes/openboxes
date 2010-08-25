package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;

/**
 * We only track InventoryLot's in the warehouse.  An InventoryLot references
 * a Product and contains information about that lots expiration.  
 */
class InventoryLot {	
	
	String lotNumber				// lot number
	Date expirationDate				// expiration date	
	Product product					// Reference to the product
		
	// InventoryLot(s) should probably live on their own.  InventoryItem can reference InventoryLot, but should not
	// own an InventoryLot.
	//static belongsTo = [ inventoryItem : InventoryItem ];
	
    static constraints = {
		lotNumber(nullable:false)
		product(nullable:false)		
		expirationDate(nullable:true)
	}

    String toString() { return "$lotNumber"; }
    
}
