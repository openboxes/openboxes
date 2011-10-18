package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum InventoryStatus {

	SUPPORTED(1),
	SUPPORTED_NON_INVENTORY(2),
	NOT_SUPPORTED(3)
	
	int sortOrder

	InventoryStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(InventoryStatus a, InventoryStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ SUPPORTED, SUPPORTED_NON_INVENTORY, NOT_SUPPORTED ]
	}
	
	String toString() { return name() }

}
