package org.pih.warehouse.shipping

import java.util.Date;

import org.pih.warehouse.core.Organization;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.core.Person;


class ShipmentItem implements Comparable {

	String lotNumber				// Loose coupling to the inventory lot 
    Product product		    		// Specific product that we're tracking
		
    Integer quantity		    	// Quantity could be a class on its own
    String serialNumber				// Serial number of a particular product (optional)				
	Person recipient 				// Recipient of an item
	Organization donor				// Organization that donated the goods
	
	//ContainerType packageType		// The type of packaging that this item is stored 
    								// within.  This is different from the container type  
    								// (which might be a pallet or shipping container), in  
    								// that this will likely be a box that the item is 
    								// actually contained within.

	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;

	
    static belongsTo = [ container : Container ] // + shipment : Shipment

    static constraints = {
		quantity(min:0, nullable:false)
		product(nullable:false)
		serialNumber(nullable:true)
		recipient(nullable:true)
		donor(nullable:true)
		
    }
    
    int compareTo(obj) { product.name.compareTo(obj.product.name) }
}
