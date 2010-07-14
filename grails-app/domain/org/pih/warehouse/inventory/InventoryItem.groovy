package org.pih.warehouse.inventory

import org.pih.warehouse.Product;


/**
 * Represents products that are usually stocked by this location.
 */
class InventoryItem {

    Product product;		    // Specific product that we're tracking
    //ProductLot productLot;	    // It's probably more appropriate to store by LOT

    //Inventory inventory	    // Provides a link back to the parent inventory
    //WarehouseBin warehouseBin	    // storage location
    //InventoryLocation 
    String binLocation;		    

    
    Integer quantity;		    // Current quantity - this is just a cached value based on a calculation
    Integer reorderQuantity;	// Should reorder product when quanity falls below this value
    Integer idealQuantity;	    // Should warn user when the quantity is below this value

    
    // Suppliers 
    //Supplier preferredSupplier;
    //Supplier alternateSupplier;
    
    
    // TODO Cannot have a reference to product for some reason
    static belongsTo = [ inventory : Inventory ];

    // TODO Add suppliers to inventory item so we know who to reorder from
    //static hasMany = [ suppliers : Supplier]
    
                         
    
                         
                         
                         
    static constraints = {
		quantity(min:0, nullable:false)
		reorderQuantity(min:0, nullable:false)
		idealQuantity(min:0, nullable:false)
		inventory(nullable:true)
    }
}
