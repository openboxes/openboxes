package org.pih.warehouse.core;

public enum EventStatus {

	NEW('New'),
	UNKNOWN('Unknown'),
	INITIAL('Initial'),
	PENDING('Pending'),
	COMPLETE('Complete'),
	PACKED('Packed'),
	SHIPPED('Shipped'),
	RECEIVED('Received')
 
	String name

	EventStatus(String name) { this.name = name; }

	static list() {
		[ INITIAL, PENDING, COMPLETE, UNKNOWN, NEW, PACKED, SHIPPED, RECEIVED ]
	}
}

