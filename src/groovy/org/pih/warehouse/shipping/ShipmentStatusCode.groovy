package org.pih.warehouse.shipping

public enum ShipmentStatusCode {

	CREATED(0),
	PENDING(1),
	SHIPPED(2),
	RECEIVED(3)
	
	int sortOrder

	ShipmentStatusCode(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(ShipmentStatusCode a, ShipmentStatusCode b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ CREATED, PENDING, SHIPPED, RECEIVED ]
	}
	
	
	String getName() { return name() }
	
	String toString() { return name() }
}