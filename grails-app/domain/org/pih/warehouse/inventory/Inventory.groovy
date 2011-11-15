package org.pih.warehouse.inventory;

import java.util.Date;

import org.pih.warehouse.core.Location;

class Inventory implements java.io.Serializable {

    // Core data elements
    Location warehouse		// we could assume that a warehouse has an inventory
    
	// Auditing
	Date dateCreated;
	Date lastUpdated;
		
    // Association mapping
    static belongsTo = [ warehouse: Location ]
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
