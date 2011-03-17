package org.pih.warehouse.inventory

import java.util.Date;

import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;

/**
 * Represents products that are usually stocked by this location.
 */
class InventoryItem implements Serializable {
	
	Product product;		    			// Product that we're tracking
	String lotNumber;						// Lot information for a product  
	Date expirationDate;
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static transients = ['quantity']
	
	// Notice the unique constraint on lotNumber/product
    static constraints = {
		product(nullable:false)
		lotNumber(nullable:true, unique:['product'])
		expirationDate(nullable:true)
		
    }
	
	
	
	
}
