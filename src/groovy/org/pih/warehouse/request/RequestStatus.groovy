package org.pih.warehouse.request

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum RequestStatus {

	NEW(1),
	REQUESTED(2),
	OPEN(3),
	PICKED(4),
	FULFILLED(5),
	SHIPPED(6),
	RECEIVED(7),
	CANCELED(8)
	
	int sortOrder

	RequestStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(RequestStatus a, RequestStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ NEW, REQUESTED, OPEN, PICKED, FULFILLED, SHIPPED, RECEIVED, CANCELED ]
	}
	
	String toString() { return name() }

}
