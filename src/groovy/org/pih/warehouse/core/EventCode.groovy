package org.pih.warehouse.core;

public enum EventCode {

	CREATED('Pending'),
	SHIPPED('Shipped'),
	RECEIVED('Received')
 
	String status

	EventCode(String status) { this.status = status }

	static list() {
		[ CREATED, SHIPPED, RECEIVED ]
	}
	
	String getName() { return status }
}

