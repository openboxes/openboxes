package org.pih.warehouse.inventory;

public enum InventoryItemType {

	SERIALIZED('Serialized'),
	NON_SERIALIZED('Non-serialized');

	String name

	InventoryItemType(String name) { this.name = name; }

	static list() {
		[ SERIALIZED, NON_SERIALIZED]
	}
}

