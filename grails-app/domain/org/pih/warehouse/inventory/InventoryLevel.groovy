package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;

class InventoryLevel {
	
	Product product;	
	Integer minQuantity;			// Should warn user when quantity is below this value
	Integer lowQuantity;			// Should alert user when quantity is below this value (emergency)
	Integer reorderQuantity;		// Should reorder product when quanity falls below this value
	Integer idealQuantity;			// Should warn user when the quantity is below this value
	Integer maxQuantity;			// Should warn user when quantity is above this value
	
	static belongsTo = [ inventory: Inventory ]
	
	static constraints = { 
		minQuantity(nullable:true)
		lowQuantity(nullable:true)
		reorderQuantity(nullable:true)
		idealQuantity(nullable:true)
		maxQuantity(nullable:true)
	}
}
