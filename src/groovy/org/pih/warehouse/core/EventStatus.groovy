package org.pih.warehouse.core;

public enum EventStatus {

	CREATED('Pending'),
	SHIPPED('Shipped'),
	RECEIVED('Received')
 
	String name

	EventStatus(String name) { this.name = name }

	static list() {
		[ CREATED, SHIPPED, RECEIVED ]
	}
}

