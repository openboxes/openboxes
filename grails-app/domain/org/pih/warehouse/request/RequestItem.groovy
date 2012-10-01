/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.request

import java.util.Date;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.picklist.PicklistItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.ProductGroup;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;

class RequestItem implements Serializable {
	
	String id
	String description	
	Category category
	Product product
	ProductGroup productGroup
	InventoryItem inventoryItem
	Integer quantity
	Float unitPrice	
	Person requestedBy	// the person who actually requested the item
	
	
	// Audit fields
	Date dateCreated
	Date lastUpdated

	static transients = [ "type", "status" ]
	
	static belongsTo = [ request : Request ]

	static mapping = {
		id generator: 'uuid'
	}
		
    static constraints = {
    	description(nullable:true)
		category(nullable:true)
		product(nullable:true)
		productGroup(nullable:true)
		inventoryItem(nullable:true)
		requestedBy(nullable:true)
		quantity(nullable:false, min:1)
		unitPrice(nullable:true)
	}

	
	String getStatus() { 
		def picklistItems = getPicklistItems();
		def quantityPicked = picklistItems.sum { it.quantity } 
		if (quantityPicked == quantity) return "tick"
		else if (quantityPicked < quantity) return "flag_yellow"
		else if (quantityPicked > quantity) return "flag_red"
		return "grey"
	}
	
	List getPicklistItems() { 
		return PicklistItem.findAllByRequestItem(this)
	}
	
	String getType() { 
		return (product)?"Product":(productGroup)?"ProductGroup":(category)?"Category":""
	}
	
	String displayName() {
		if (product) {
			return product.name;
		}
		else if (productGroup) { 
			return productGroup.description
		}
		else if (category) {
			return category.name
		}
		else {
			return description;
		}
	}

	Integer quantityFulfilled() { 
		def fulfillmentItems = request?.fulfillment?.fulfillmentItems.findAll { it.requestItem == this }
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
