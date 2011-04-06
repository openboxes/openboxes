package org.pih.warehouse.inventory



public enum TransactionCode {

	DEBIT,
	CREDIT,
	INVENTORY,
	PRODUCT_INVENTORY
 
	static list() {
		[ DEBIT, CREDIT, INVENTORY, PRODUCT_INVENTORY ]
	}
}
