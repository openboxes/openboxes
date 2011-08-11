package org.pih.warehouse.request

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum RequestStatus {

	NOT_REQUESTED(1),
	REQUESTED(2),
	OPEN(3),
	FULFILLED(4),
	SHIPPED(5),
	RECEIVED(6),
	CANCELED(7)
	
	int sortOrder

	RequestStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(RequestStatus a, RequestStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ NOT_REQUESTED, REQUESTED, OPEN, FULFILLED, SHIPPED, RECEIVED, CANCELED ]
	}
	
	String toString() { return name() }

}
