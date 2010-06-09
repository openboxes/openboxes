package org.pih.warehouse

class ShipmentItem implements Comparable {

    Product product		    		// Specific product that we're tracking
    Integer quantity		    	// Quantity could be a class on its own
    
    ContainerType packageType		// The type of packaging that the contents are stored 
    								// within.  This is different from the container type  
    								// (which might be a pallet or shipping container), in  
    								// that this will likely be a box that the item is 
    								// actually contained within.

    Float grossWeight				// Weight of entire package
    Float unitWeight				// Weight per unit 
    
    static belongsTo = [ container : Container ] // + shipment : Shipment

    static constraints = {
		quantity(min:0, nullable:false)
		product(nullable:false)
		packageType(nullable:true)
		grossWeight(nullable:true)
		unitWeight(nullable:true)
		//container(nullable:false)
		
    }
    
    int compareTo(obj) { product.name.compareTo(obj.product.name) }
}
