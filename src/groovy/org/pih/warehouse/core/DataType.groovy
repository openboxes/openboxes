package org.pih.warehouse.core;

public enum DataType {

	NUMBER('Number'),
	STRING('String'),
	DATE('Date'),
	CODED('Coded');
 
	String name

	DataType(String name) { this.name = name; }

	static list() {
		[ NUMBER, STRING, CODED]
	}
}

