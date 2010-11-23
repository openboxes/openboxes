package org.pih.warehouse.inventory

import org.pih.warehouse.product.Product;

/**
 * We only track InventoryLot's in the warehouse.  An InventoryLot references
 * a Product and contains information about that lots expiration.  
 */
class InventoryLot {	
	
	Product product					// Reference to the product
	String lotNumber				// lot number
	Integer initialQuantity			// initial quantity of lot 
	Date manufactureDate			// date when lot was manufactured
	Date expirationDate				// date when lot will expire
	
	// InventoryLot(s) should probably live on their own.  InventoryItem can reference InventoryLot, but should not
	// own an InventoryLot because an InventoryLot should live 
	// 
	// update: We should actually let inventory own inventory lots - but without delete on cascade.   
	static belongsTo = [ inventory: Inventory ];
	
    static constraints = {
		product(nullable:false)	
		lotNumber(nullable:true, unique: true)
		initialQuantity(nullable:false)
		manufactureDate(nullable:true)
		expirationDate(nullable:true)		
	}
    
}
