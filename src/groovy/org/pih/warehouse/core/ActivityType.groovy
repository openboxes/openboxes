package org.pih.warehouse.core;

public enum ActivityType {

	ORDERING('Ordering'),
	SHIPPING('Shipping'),
	RECEIVING('Receiving'); 

	String name

	ActivityType(String name) { this.name = name; }

	static list() {
		[ ORDERING, SHIPPING, RECEIVING]
	}
}

