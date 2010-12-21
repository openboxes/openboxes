package org.pih.warehouse.product
;

/**
 * 
 * @author jmiranda
 */

public enum ProductClass {

	CONSUMABLE('Consumable'),
	DRUG('Drug'),
	DURABLE('Durable'),
	NON_INVENTORY('Non-Inventory');
	
	String name
	
	ProductClass(String name) { this.name = name; }

	static list() {
		[ CONSUMABLE, DRUG, DURABLE, NON_INVENTORY ]
	}
	
}

