package org.pih.warehouse.inventory;

import java.util.Date;


class Inventory implements java.io.Serializable {

    // Core data elements
    Warehouse warehouse		// we could assume that a warehouse has an inventory
    Date lastInventoryDate	// last time an inventory was completed
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	
    // Association mapping
    static belongsTo = [ warehouse: Warehouse ]
    static hasMany = [ inventoryItems: InventoryItem, inventoryLots: InventoryLot, inventoryLevels: InventoryLevel ]

    // Show use warehouse name
    String toString() { return "${warehouse.name}"; }

    // Constraints
    static constraints = {
		lastInventoryDate(nullable:true)
		warehouse(nullable:false)
    }
	
}
