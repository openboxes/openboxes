package org.pih.warehouse

class Inventory {

    // Core data elements
    Warehouse warehouse		// we could assume that a warehouse has an inventory
    Date lastInventoryDate	// last time an inventory was completed

    // Association mapping
    static hasMany = [ inventoryItems : InventoryItem ]
    static belongsTo = [ warehouse : Warehouse ]

    // Show use warehouse name
    String toString() { return "Inventory @ $warehouse.name"; }

    // Constraints
    static constraints = {
		lastInventoryDate(nullable:true)
		inventoryItems(nullable:true)
		warehouse(nullable:false)
    }
}
