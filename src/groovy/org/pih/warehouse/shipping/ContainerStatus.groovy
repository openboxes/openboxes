package org.pih.warehouse.shipping;

public enum ContainerStatus {

	OPEN('Open'),
	CLOSED('Closed');
 
	String name
	ContainerStatus(String name) { this.name = name; }

	static list() {
		[ OPEN, CLOSED ]
	}
}

