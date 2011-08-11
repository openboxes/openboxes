package org.pih.warehouse.fulfillment

public enum FulfillmentStatus {

	NOT_FULFILLED(0),
	PARTIALLY_FULFILLED(1),
	FULFILLED(2)
	
	int sortOrder

	FulfillmentStatus(int sortOrder) { [ this.sortOrder = sortOrder ] }
	
	static int compare(FulfillmentStatus a, FulfillmentStatus b) {
		return a.sortOrder <=> b.sortOrder
	}
	
	static list() {
		[ NOT_FULFILLED, PARTIALLY_FULFILLED, FULFILLED ]
	}
	
	String toString() { return name() }

}
