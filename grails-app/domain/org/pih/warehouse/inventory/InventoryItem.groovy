/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory

import java.util.Date;
import java.util.Map;

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

    Integer quantity
	Integer quantityOnHand
	Integer quantityAvailableToPromise
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static transients = ['quantity', 'quantityOnHand', 'quantityAvailableToPromise', 'grailsApplication']

	static mapping = {
		id generator: 'uuid'
	}
	
	// Notice the unique constraint on lotNumber/product
    static constraints = {
		product(nullable:false)
		lotNumber(nullable:true, unique:['product'], maxSize:255)
		expirationDate(nullable:true)	
    }

    Map toJson() {
        [
            "inventoryItemId": id,
            "productId": product?.id,
            "productName": product?.name,
            "lotNumber":lotNumber,
            "expirationDate": expirationDate?.format("MM/dd/yyyy"),
            "quantityOnHand": quantity?: 0,
            "quantityATP": quantity?: 0       //todo: quantity available to promise will coming soon
        ]
    }

	
	String toString() { return "${lotNumber}:${expirationDate}"; }
	
}
