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
	Float unitPrice
	
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
		unitPrice(nullable:true)
	}

	
	String getOrderItemType() { 
		return (product)?"Product":(category)?"Category":"Unclassified"
	}

	Integer quantityFulfilled() { 
		def quantity = 0;
		try { 
			def shipmentItems = shipmentItems()
			quantity = shipmentItems?.sum { it?.quantity }
		} 
		catch (Exception e) { log.error "Error calculating quantity fulfilled: " + e.message }
		return quantity ?: 0;
	}
	
	Integer quantityRemaining() { 
		return quantity - quantityFulfilled();
	}
	
	
	
	Boolean isPartiallyFulfilled() { 
		return quantityFulfilled() > 0 && quantityFulfilled() < quantity;
	}	
	
	Boolean isCompletelyFulfilled() { 
		return quantityFulfilled() >= quantity;
	}
	
	Boolean isPending() { 
		return !isCompletelyFulfilled()
	}
	
	/**
	 * Gets all shipment items related to this order item 
	 * (ignoring any orphaned shipment item references)
	 * 
	 * @return
	 */
	def shipmentItems() {
		def shipmentItems = []
		orderShipments?.each { 
			try { 
				def shipmentItem = ShipmentItem.get(it?.shipmentItem?.id) 
				if (shipmentItem) { 
					shipmentItems << shipmentItem;
				}
			} catch (Exception e) {
				log.error "Error getting shipment items: " + e.message
			}	
		} 
		return shipmentItems;
	}
	
	/**
	 * Gets all shipments related to this order item 
	 * (ignoring any orphaned shipment item references)
	 * 
	 * @return
	 */
	def shipments() { 
		def shipments = []
		orderShipments.each { 
			try { 
				def shipment = Shipment.get(it?.shipmentItem?.shipment?.id) 
				if (shipment) { 
					shipments << shipment
				}
			} catch (Exception e) { 
				log.error "Error getting shipment: " + e.message
			} 
		} 
		return shipments;
	}

	def totalPrice() { 
		return ( quantity ? quantity : 0.0 ) * ( unitPrice ? unitPrice : 0.0 );
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
