package org.pih.warehouse.order

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum OrderStatus {

	PENDING("Pending",1),
	PLACED("Placed",2),
	RECEIVED("Received",3)
	
	String name
	int sortOrder

	OrderStatus(String name, int sortOrder) { [ this.name = name, this.sortOrder = sortOrder ] }
	
	static int compare(OrderStatus a, OrderStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ PENDING, PLACED, RECEIVED ]
	}
	
	static OrderStatus getByName(String name) {
		list().find( { it.name == name } )
	}
	
	String toString() { return name }
}
