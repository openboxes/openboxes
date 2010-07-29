package org.pih.warehouse.shipping

import org.pih.warehouse.core.Organization;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.user.Contact;


class ShipmentItem implements Comparable {

    Product product		    		// Specific product that we're tracking
    Integer quantity		    	// Quantity could be a class on its own
    String serialNumber				// Serial number of the particular item
	//Contact recipient				// Recipient of an item
	String recipient 
	
	//ContainerType packageType		// The type of packaging that this item is stored 
    								// within.  This is different from the container type  
    								// (which might be a pallet or shipping container), in  
    								// that this will likely be a box that the item is 
    								// actually contained within.

    Float grossWeight				// Weight of entire package
    Float unitWeight				// Weight per unit 
    
	
	Boolean donation = false		// Donation information
	Organization donor				// Organization that donated the goods
	
    static belongsTo = [ container : Container ] // + shipment : Shipment

    static constraints = {
		quantity(min:0, nullable:false)
		product(nullable:false)
		grossWeight(nullable:true)
		unitWeight(nullable:true)
		serialNumber(nullable:true)
		recipient(nullable:true)
		donation(nullable:true)
		donor(nullable:true)
		
    }
    
    int compareTo(obj) { product.name.compareTo(obj.product.name) }
}
