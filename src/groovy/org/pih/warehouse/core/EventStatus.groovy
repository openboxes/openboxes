package org.pih.warehouse.core;

public enum EventStatus {

	INITIAL('Initial'),
	PENDING('Pending'),
	COMPLETE('Complete');
 
	String name

	EventStatus(String name) { this.name = name; }

	static list() {
		[ INITIAL, PENDING, COMPLETE ]
	}
}

