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

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class TransactionEntry implements Comparable, Serializable {
	
	String id
    Integer quantity
    Product product
	InventoryItem inventoryItem		// The inventory item being tracked
	Location binLocation			// Bin location of inventory item

	String comments					//
	
    static belongsTo = [ transaction : Transaction ]

	static mapping = { 
		id generator: 'uuid'		
	}
    static constraints = {
        product(nullable:true)
		inventoryItem(nullable:false)
		binLocation(nullable:true)
		quantity(nullable:false)
		comments(nullable:true, maxSize: 255)	
    }
    
    /**
     * Transient properties used as a hack so that we can use TransactionEntry as a command object and store lot number and product
     * TODO: find a better way to do this... or just remove this once we rework the current "create transaction" interface
     * the danger here is that legacy code we've missed will actually use and rely on the values of these now-transient properties
     */
    //static transients = ['lotNumber','product']
    //String lotNumber
    //Product product
    
    /**
    String getLotNumber() {
    	return inventoryItem?.lotNumber ?: lotNumber
    }
    
    String getProduct() {
    	return inventoryItem?.product ?: product
    }
    */
    
    
	/**
	 * Sort by the sort parameters of the parent transaction
	 */
	int compareTo(obj) { 
		transaction.compareTo(obj.transaction)
	}

    @Override
	int hashCode() {
		if (this.id != null) {
			return this.id.hashCode();
		}
		return super.hashCode();
	}

    @Override
	boolean equals(Object o) {
		if (o instanceof TransactionEntry) {
			TransactionEntry that = (TransactionEntry)o;
			return this.id == that.id;
		}
		return false;
	}
}
