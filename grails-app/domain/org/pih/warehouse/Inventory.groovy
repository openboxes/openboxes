package org.pih.warehouse

class Inventory {

    // Core data elements
    Date lastInventoryDate	// last time an inventory was completed
    Warehouse warehouse		// we could assume that a warehouse has an inventory

    // Core associations
    List<InventoryLineItem> inventoryLineItems	// products in inventory

    // Association mapping
    static hasMany = [ inventoryLineItems : InventoryLineItem ]
    static belongsTo = [ Warehouse ]

    // Show use warehouse name
    String toString() { return "Inventory $id"; }

    // Constraints
    static constraints = {
	lastInventoryDate(nullable:true)
	inventoryLineItems(nullable:true)
	warehouse(nullable:false)
    }
}
