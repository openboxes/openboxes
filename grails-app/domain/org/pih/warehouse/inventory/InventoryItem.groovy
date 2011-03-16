package org.pih.warehouse.inventory

import java.util.Date;

import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;

/**
 * Represents products that are usually stocked by this location.
 */
class InventoryItem implements Serializable {
	
	Boolean active = Boolean.TRUE			// Actively managed
	String description;						// Description of the specific instance of a product that we're tracking
	Product product;		    			// Product that we're tracking
	String lotNumber;						// Lot information for a product  
	Date expirationDate;
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static transients = ['quantity']
	
	// Notice the unique constraint on lotNumber/product
    static constraints = {
		active(nullable:false)
		description(nullable:true, unique:['lotNumber','description'])
		product(nullable:false)
		lotNumber(nullable:true, unique:['product','description'])
		expirationDate(nullable:true)
    }
	
	Integer getQuantity() { 
		// PIMS-528 TransientObjectException: object references an unsaved transient instance - 
		// save the transient instance before flushing: org.pih.warehouse.product.Product
		//return TransactionEntry.findAllByProductAndLotNumber(product, lotNumber).inject(0) { count, item -> count + (item?.quantity ?: 0) }
		return 0;
		
	}
	
	
}
