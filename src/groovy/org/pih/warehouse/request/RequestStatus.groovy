package org.pih.warehouse.request

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum RequestStatus {

	NOT_YET_REQUESTED("Not yet requested",1),
	REQUESTED("Requested",2),
	FULFILLED("Fulfilled", 3),
	SHIPPED("Shipped", 4),
	RECEIVED("Received",5),
	CANCELED("Canceled", 6)
	
	String name
	int sortOrder

	RequestStatus(String name, int sortOrder) { [ this.name = name, this.sortOrder = sortOrder ] }
	
	static int compare(RequestStatus a, RequestStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ NOT_YET_REQUESTED, REQUESTED, FULFILLED, SHIPPED, RECEIVED ]
	}
	
	static RequestStatus getByName(String name) {
		list().find( { it.name == name } )
	}
	
	String toString() { return name }
}
