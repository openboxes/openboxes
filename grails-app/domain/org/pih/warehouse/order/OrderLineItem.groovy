package org.pih.warehouse.order;

import org.pih.warehouse.catalog.CatalogItem;
import org.pih.warehouse.product.Product;

class OrderLineItem {

	int price;
	int quantity;
	Product product;
	
	static transients = ["totalPrice"]
	
    static constraints = {
		quantity(min:0)
		product(nullable:true)
    }
	
	int getTotalPrice() {
		price * quantity
	}
	
}
