package org.pih.warehouse.request

import java.util.Date;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class RequestItem implements Serializable {
	
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

	static transients = [ "type" ]
	
	static belongsTo = [ request : Request ]
	
    static constraints = {
    	description(nullable:true)
		category(nullable:true)
		product(nullable:true)
		inventoryItem(nullable:true)
		requestedBy(nullable:true)
		quantity(nullable:false, min:1)
		unitPrice(nullable:true)
	}

	
	String getType() { 
		return (product)?"Product":(category)?"Category":"Unclassified"
	}

	Integer quantityFulfilled() { 
		def fulfillmentItems = request.fulfillment.fulfillmentItems.findAll { it.requestItem == this }
		return (fulfillmentItems) ? fulfillmentItems.sum { it.quantity } : 0;
	}
	
	Integer quantityRemaining() { 
		return quantity - quantityFulfilled();
	}
	
	
	Boolean isComplete() { 
		return !isPending();
	}
	
	Boolean isPending() { 
		return quantityRemaining() > 0;
	}
	
	
	def totalPrice() { 
		return ( quantity ? quantity : 0.0 ) * ( unitPrice ? unitPrice : 0.0 );
	}
			
		
}
