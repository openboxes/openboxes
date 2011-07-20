package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;
import org.pih.warehouse.product.Category


class InventoryItemCommand {
	
	
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
	
}



