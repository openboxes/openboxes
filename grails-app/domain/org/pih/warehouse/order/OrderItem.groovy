package org.pih.warehouse.order

import java.util.Date;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class OrderItem implements Serializable {
	
	String description	
	Category category
	Product product
	InventoryItem inventoryItem
	Integer quantity
	User requestedBy	// the person who actually requested the item
	
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	static transients = [ "orderItemType" ]
	
	static belongsTo = [ order : Order ]
	
	static hasMany = [ orderShipments : OrderShipment ]

    static constraints = {
    	description(nullable:true)
		category(nullable:true)
		product(nullable:true)
		inventoryItem(nullable:true)
		requestedBy(nullable:true)
		quantity(nullable:false, min:1)
	}

	
	String getOrderItemType() { 
		return (product)?"Product":(category)?"Category":"Unclassified"
	}

	Integer quantityFulfilled() { 
		try { 
			def shipmentItems = shipmentItems()
			return shipmentItems ? shipmentItems.sum { it.quantity } : 0 
			//return orderShipments ? orderShipments.sum { it?.shipmentItem?.quantity } : 0
		} catch (Exception e) { log.error "Error calculating quantity fulfilled", e }
		return 0;
	}
	
	Boolean isComplete() { 
		return quantityFulfilled() >= quantity;
	}
	
	Boolean isPending() { 
		return !isComplete()
	}
	
	def shipmentItems() {
		def shipmentItems = []
		try { 
			shipmentItems = orderShipments.collect{ ShipmentItem.get(it?.shipmentItem?.id) } 
		} catch (Exception e) { 
			log.error "Error getting shipment items", e 
		} 
		return shipmentItems;
	}
	
	def shipments() { 
		def shipments = []
		try { 
			shipments = orderShipments.collect { Shipment.get(it?.shipmentItem?.shipment?.id) } 
		} catch (Exception e) { 
			log.error "Error getting shipment", e 
		} 
		return shipments;
	}
		
	/*
	List addToShipmentItems(ShipmentItem shipmentItem) {
		OrderShipment.link(this, shipmentItem)
		return shipmentItems()
	}

	List removeFromShipmentItems(ShipmentItem shipmentItem) {
		OrderShipment.unlink(this, shipmentItem)
		return shipmentItems()
	}
	*/
	
}
