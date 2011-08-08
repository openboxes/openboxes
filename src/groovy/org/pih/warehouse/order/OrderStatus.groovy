package org.pih.warehouse.order

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum OrderStatus {

	PENDING(1),
	PLACED(2),
	PARTIALLY_RECEIVED(3),
	RECEIVED(4)
	
	int sortOrder

	OrderStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(OrderStatus a, OrderStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ PENDING, PLACED, PARTIALLY_RECEIVED, RECEIVED ]
	}
	
	String toString() { return name() }
	
}
