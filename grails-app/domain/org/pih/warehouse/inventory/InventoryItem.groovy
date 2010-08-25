package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;


/**
 * Represents products that are usually stocked by this location.
 */
class InventoryItem {
	
	String serialNumber;			// Serial number of the product 
	Product product;		    	// Specific product that we're tracking
	InventoryLot inventoryLot;		// The product lot
    
    Integer quantityOnHand;			// Current quantity - this is just a cached value based on a calculation
    //Integer quantityLow;			// Should reorder product when quanity falls below this value
    //Integer quantityIdeal;	    	// Should warn user when the quantity is below this value
        
    // TODO Cannot have a reference to product for some reason
    static belongsTo = [ inventory : Inventory ];

    // TODO Add suppliers to inventory item so we know who to reorder from
    //static hasMany = [ suppliers : Supplier]
                         
    static constraints = {
		product(nullable:true)
		serialNumber(nullable:true)
		inventoryLot(nullable:true)
		quantityOnHand(min:0, nullable:false)
    }
}
