package org.pih.warehouse.order;

import org.pih.warehouse.Location;
import org.pih.warehouse.order.cart.ShoppingCart;

class Order implements Serializable {

	String orderNo
	Date dateOrdered
	Date dateShipped

	// Customer purchasing/requesting products	
	Location customer

	// Audit fields
	Date dateCreated;
	Date lastUpdated;

	static transients = ["totalPrice"]
	
    static mapping = {
    	table "`order`"
    }

	// Core association mappings
	static hasMany = [ lineItems : OrderLineItem ]
	
    static constraints = {
		
    }
	
	Order() { }
	
	Order(ShoppingCart shoppingCart) {
		Item.getAll(shoppingCart.itemIds).each {
			def lineItem = new OrderLineItem(item: it, quantity: shoppingCart.getQuantity(it.id))
			addToLineItems(lineItem)
		}
	}

	
	int getTotalPrice() {
		lineItems ? lineItems.sum { it.totalPrice } : 0
	}
	
}


