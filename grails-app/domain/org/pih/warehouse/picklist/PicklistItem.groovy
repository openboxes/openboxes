package org.pih.warehouse.picklist

import java.util.Date;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductGroup;
import org.pih.warehouse.request.RequestItem;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class PicklistItem implements Serializable {
	
	String id	
	RequestItem requestItem
	InventoryItem inventoryItem
	Integer quantity
	
	String status
	String reasonCode
	String comment
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	
	static belongsTo = [ picklist : Picklist ]

	static mapping = {
		id generator: 'uuid'
	}
		
    static constraints = {    	
		inventoryItem(nullable:true)
		requestItem(nullable:true)
		quantity(nullable:false)
		status(nullable:true)
		reasonCode(nullable:true)
		comment(nullable:true)
		
	}			
		
}
