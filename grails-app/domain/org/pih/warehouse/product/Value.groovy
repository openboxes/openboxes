package org.pih.warehouse.product;

import java.util.Date;

class Value {

	Float floatValue
	Integer integerValue
	String stringValue
	Date dateValue
	byte [] byteValue
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	// Values need to exist on their own because they will be answers 
	// to questions without needing to be linked to an attribute
	//static belongsTo = [ attribute : Attribute ]
	
	static constraints = { 
		stringValue(nullable:true)
		floatValue(nullable:true)
		integerValue(nullable:true)
		dateValue(nullable:true)
		byteValue(nullable:true)	
	}

}
