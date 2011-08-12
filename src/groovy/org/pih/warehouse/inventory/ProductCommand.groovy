package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category

class ProductCommand {
	
	Category category
	Product product 
	Integer quantityOnHand = 0;
	Integer quantityToShip = 0;
	Integer quantityToReceive = 0;
	
	static constraints = {
		category(nullable: true) 
		product(nullable:true)
		quantityOnHand(nullable:true)
		quantityToShip(nullable:true)
		quantityToReceive(nullable:true)
	}
	
	int hashcode() {
		if (product != null) {
			return product.id.hashCode();
		}
		return super.hashCode();
	}
	
	boolean equals(Object o) {
		if (o instanceof ProductCommand) {
			ProductCommand that = (ProductCommand)o;
			return this.product.id == that.product.id;
		}
		return false;
	}
}



