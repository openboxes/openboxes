package org.pih.warehouse.inventory

import java.util.Date;

import org.pih.warehouse.product.Product;

class InventoryLevel {
	
	InventoryStatus status = InventoryStatus.SUPPORTED;
	//Boolean supported = Boolean.TRUE;
	Product product;	
	Integer minQuantity;			// Should warn user when quantity is below this value
	Integer reorderQuantity;		// Should reorder product when quantity falls below this value
	//Integer lowQuantity;			// Should alert user when quantity is below this value (emergency)
	//Integer idealQuantity;			// Should warn user when the quantity is below this value
	//Integer maxQuantity;			// Should warn user when quantity is above this value
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ inventory: Inventory ]
	
	static constraints = { 
		status(nullable:true)
		product(nullable:false)
		//supported(nullable:false)
		minQuantity(nullable:true, range: 0..2147483646)
		reorderQuantity(nullable:true, range: 0..2147483646)
		//lowQuantity(nullable:true)
		//idealQuantity(nullable:true)
		//maxQuantity(nullable:true)
	}
}
