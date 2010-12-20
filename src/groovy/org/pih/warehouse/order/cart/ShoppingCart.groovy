package org.pih.warehouse.order.cart

import org.pih.warehouse.product.Product;

class ShoppingCart implements Serializable {
	
	// Key: item id, value: item count
	def items = new HashMap<Long,Integer>()
	
	boolean isEmpty() {
		items.isEmpty()
	}
	
	int getQuantity() {
		items.values().sum() as int ?: 0
	}
	
	int getQuantity(Long id) {
		items.get(id) ?: 0
	}
		
	void clearItem(Long id) {
		items.remove(id)
	}
	
	void addItem(Long id) {
		def count = items.get(id) ?: 0
		items.put(id, count + 1)
	}
	
	void removeItem(Long id) {
		def count = items.get(id)
		if (count && count > 1) {
			items.put(id, count - 1)
		} else {
			clearItem(id)
		}
	}
	
	List<Long> getItemIds() {
		items.keySet() as List
	}
}