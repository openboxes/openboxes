package org.pih.warehouse

class Inventory {

    // Core data elements
    Warehouse warehouse		// we could assume that a warehouse has an inventory
    Date lastInventoryDate	// last time an inventory was completed

    // Core associations
    //List<InventoryLineItem> inventoryLineItems	// products in inventory

    // Association mapping
    static hasMany = [ inventoryLineItems : InventoryLineItem ]
    static belongsTo = [ warehouse : Warehouse ]

    // Show use warehouse name
    String toString() { return "Inventory @ $warehouse.name"; }

    // Constraints
    static constraints = {
	lastInventoryDate(nullable:true)
	inventoryLineItems(nullable:true)
	warehouse(nullable:false)
    }
}
