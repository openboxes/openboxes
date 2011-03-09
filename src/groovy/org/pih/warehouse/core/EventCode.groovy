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
	
	static EventCode getByStatus(String status) {
		list().find( {it.status == status} )
	}
	
	String getName() { return status }
}

