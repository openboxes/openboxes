package org.pih.warehouse.request

import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ShipmentStatusCode;

public enum RequestStatus {

	NOT_YET_REQUESTED("request.status.notYetRequested.label",1),
	REQUESTED("request.status.requested.label",2),
	FULFILLED("request.status.fulfilled.label", 3),
	SHIPPED("request.status.shipped.label", 4),
	RECEIVED("request.status.received.label",5),
	CANCELED("request.status.canceled.label", 6)
	
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
