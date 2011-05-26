package org.pih.warehouse.order

import java.util.Date;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;

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
	
	static hasMany = [ comments : Comment ]

    static constraints = {
    	description(nullable:true)
		category(nullable:true)
		product(nullable:true)
		inventoryItem(nullable:true)
		requestedBy(nullable:true)
		quantity(nullable:false)
	}

	String getOrderItemType() { 
		return (product)?"product":(category)?"category":"unclassified"
	}	
	
}
