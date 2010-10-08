package org.pih.warehouse.core;

public enum EventStatus {

	NEW('New'),
	UNKNOWN('Unknown'),
	INITIAL('Initial'),
	PENDING('Pending'),
	COMPLETE('Complete');
 
	String name

	EventStatus(String name) { this.name = name; }

	static list() {
		[ NEW, INITIAL, PENDING, COMPLETE, UNKNOWN ]
	}
}

