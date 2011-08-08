package org.pih.warehouse.request

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum RequestStatus {

	NOT_YET_REQUESTED(1),
	REQUESTED(2),
	FULFILLED(3),
	SHIPPED(4),
	RECEIVED(5),
	CANCELED(6)
	
	int sortOrder

	RequestStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(RequestStatus a, RequestStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ NOT_YET_REQUESTED, REQUESTED, FULFILLED, SHIPPED, RECEIVED ]
	}
	
	String toString() { return name() }

}
