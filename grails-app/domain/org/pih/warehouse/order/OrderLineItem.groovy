package org.pih.warehouse.order;

import org.pih.warehouse.catalog.CatalogItem;

class OrderLineItem {

	CatalogItem item	
	int quantity;
	int price;
	
	static transients = ["totalPrice"]
	
    static constraints = {
		quantity(min:0)
    }
	
	int getTotalPrice() {
		item ? item.price * quantity : 0
	}
	
}
