package org.pih.warehouse.order.cart

import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;

class CartItem implements Serializable, Comparable<CartItem> {

	Product product;
	String lotNumber;
	Inventory inventory;
	InventoryItem inventoryItem;
	Integer quantity;
	
	int compareTo(CartItem cartItem) { 
		return this.product <=> other.product
	}
}