package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;


/**
 * Represents products that are usually stocked by this location.
 */
class InventoryItem {

	Product product;		    	// Specific product that we're tracking
	String serialNumber;			// Serial number of the product 
	InventoryLot inventoryLot;		// The product lot
    //Inventory inventory	    	// Provides a link back to the parent inventory
    //WarehouseBin warehouseBin	    // Warehouse storage location
    
    Integer quantityOnHand;			// Current quantity - this is just a cached value based on a calculation
    //Integer quantityLow;			// Should reorder product when quanity falls below this value
    //Integer quantityIdeal;	    	// Should warn user when the quantity is below this value
    
    // Suppliers 
    //Supplier preferredSupplier;
    //Supplier alternateSupplier;    
    
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
