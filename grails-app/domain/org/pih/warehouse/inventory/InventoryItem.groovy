package org.pih.warehouse.inventory

import java.util.Date;

import org.pih.warehouse.product.Product;
import org.pih.warehouse.inventory.Transaction;

/**
 * Represents an instance of a product, referenced by lot number
 * 
 * Note that an inventory item does not directly reference an inventory,
 * and in fact a single inventory item may be tied to multiple inventories
 * at the same time (if a lot is split between multiple warehouses)
 * 
 * Transaction Entries are tied to Inventory Items, and
 * these entries are used to calculate the quantity levels of inventory items
 * 
 * We may rename InventoryItem to ProductInstance, as this may 
 * be a clearer name
 */
class InventoryItem implements Serializable {
	
	String id
	
	Product product;		    			// Product that we're tracking
	String lotNumber;						// Lot information for a product  
	Date expirationDate;
	
	Integer quantityOnHand
	Integer quantityAvailableToPromise
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static transients = ['quantityOnHand', 'quantityAvailableToPromise', 'grailsApplication']

	static mapping = {
		id generator: 'uuid'
	}
	
	// Notice the unique constraint on lotNumber/product
    static constraints = {
		product(nullable:false)
		lotNumber(nullable:true, unique:['product'], maxSize:255)
		expirationDate(nullable:true)	
    }
		
	
	String toString() { return "${id}:${product}:${lotNumber}"; }
	
}
