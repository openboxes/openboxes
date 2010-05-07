package org.pih.warehouse

class InventoryLineItem {

    Product product		    // Specific product that we're tracking
    //Inventory inventory	    // Provides a link back to the parent inventory

    Integer quantity		    // Quantity could be a class on its own
    Integer reorderQuantity;	    // Should reorder product when quanity falls below this value
    Integer idealQuantity;	    // Should warn user when the quantity is below this value

    // TODO Cannot have a reference to product for some reason
    //static hasOne = [ product : Product ]
    static belongsTo = [ inventory : Inventory ]

    String binLocation		    // Location within warehouse ("bin" needs its own entity)

    
    // Other important information to be added soon
    //Batch batch	    // It's probably more appropriate to store by LOT
    //WarehouseBin warehouseBin	    // storage location
   

    static constraints = {
	quantity(min:0, nullable:false)
	reorderQuantity(min:0, nullable:false)
	idealQuantity(min:0, nullable:false)
	inventory(nullable:true)
    }
}
