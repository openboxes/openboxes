package org.pih.warehouse.fulfillment

import java.util.Date;

import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.request.RequestItem;
import org.pih.warehouse.shipping.ShipmentItem;

class FulfillmentItem implements Serializable {

	String id
	
	// Attributes
	Integer quantity
	InventoryItem inventoryItem
	RequestItem requestItem 			
	
	// Audit fields
	Date dateCreated
	Date lastUpdated
	
	static mapping = {
		id generator: 'uuid'
	}
	
	// Bi-directional associations
	static belongsTo = [ fulfillment : Fulfillment ]
	
	// One-to-many associations
	static hasMany = [ shipmentItems : ShipmentItem ]
		
    static constraints = {
		requestItem(nullable:true)
		inventoryItem(nullable:true)
		quantity(nullable:true)
    }
	
	Integer quantityPacked() { 
		return shipmentItems?.sum { it.quantity }
	}
}
