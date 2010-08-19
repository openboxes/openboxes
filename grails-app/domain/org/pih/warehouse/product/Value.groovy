package org.pih.warehouse.product;

import java.util.Date;

class Value {
	
	String stringValue
	Float floatValue
	Integer integerValue 
	byte [] byteValue
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static constraints = { 
		stringValue(nullable:true)
		floatValue(nullable:true)
		integerValue(nullable:true)
		byteValue(nullable:true)	
	}

}
