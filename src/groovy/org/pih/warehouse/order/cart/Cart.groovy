package org.pih.warehouse.order.cart

import org.pih.warehouse.product.Product;

class Cart implements Serializable {
	
	// Key: item id, value: item count
	//def items = new HashMap<Long,Integer>()
	def items = new ArrayList<CartItem>();
	
	
	boolean isEmpty() {
		items.isEmpty()
	}		
	
	void addItem(CartItem cartItem) {
		items.add(cartItem)
	}
	
	void removeItem(CartItem cartItem) {
		items.remove(cartItem);
	}

	List<CartItem> getItems() { 
		return items;
	}	
}