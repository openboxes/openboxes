package org.pih.warehouse.product
;

/**
 * 
 * @author jmiranda
 */

public enum ProductClass {

	BUNDLED('Bundled'),
	CONSUMABLE('Consumable'),
	DRUG('Drug'),
	DURABLE('Durable'),
	MIXED('Mixed'),
	NON_INVENTORY('Non-Inventory');
	
	String name
	
	ProductClass(String name) { this.name = name; }

	static list() {
		[ BUNDLED, CONSUMABLE, DRUG, DURABLE, MIXED, NON_INVENTORY ]
	}
	
}

