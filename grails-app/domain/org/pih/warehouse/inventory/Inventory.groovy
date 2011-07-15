package org.pih.warehouse.inventory;

import java.util.Date;


class Inventory implements java.io.Serializable {

    // Core data elements
    Warehouse warehouse		// we could assume that a warehouse has an inventory
    
	// Auditing
	Date dateCreated;
	Date lastUpdated;
		
    // Association mapping
    static belongsTo = [ warehouse: Warehouse ]
    static hasMany = [ configuredProducts : InventoryLevel ]

    // Show use warehouse name
    String toString() { return "${warehouse.name}"; }

	static mapping = { 
		cache true
	}
	
    // Constraints
    static constraints = {
		warehouse(nullable:false)
    }
	
}
