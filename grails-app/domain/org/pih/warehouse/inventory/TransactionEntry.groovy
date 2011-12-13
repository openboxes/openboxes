package org.pih.warehouse.inventory;

import org.pih.warehouse.product.Product;

class TransactionEntry implements Comparable, Serializable {
	
	String id
    Integer quantity				
	InventoryItem inventoryItem		// The inventory item being tracked
	String comments					// 
	
    static belongsTo = [ transaction : Transaction ]

	static mapping = { 
		id generator: 'uuid'		
	}
    static constraints = {		
		inventoryItem(nullable:false)		
		quantity(nullable:false, range: 0..2147483646)
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
	
	int hashcode() {
		if (this.id != null) {
			return this.id.hashCode();
		}
		return super.hashCode();
	}
	
	boolean equals(Object o) {
		if (o instanceof TransactionEntry) {
			TransactionEntry that = (TransactionEntry)o;
			return this.id == that.id;
		}
		return false;
	}
}
