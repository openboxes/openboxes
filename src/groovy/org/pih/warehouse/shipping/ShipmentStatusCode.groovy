package org.pih.warehouse.shipping

public enum ShipmentStatusCode {

	PENDING("shipping.pending.label",1),
	SHIPPED("shipping.shipped.label",2),
	RECEIVED("shipping.received.label",3)
	
	String name
	int sortOrder

	ShipmentStatusCode(String name, int sortOrder) { [ this.name = name, this.sortOrder = sortOrder ] }
	
	static int compare(ShipmentStatusCode a, ShipmentStatusCode b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ PENDING, SHIPPED, RECEIVED ]
	}
	
	static ShipmentStatusCode getByName(String name) {
		list().find( { it.name == name } )
	}
	
	String toString() { return name }
}