package org.pih.warehouse

class Value {
	
	String stringValue
	Float floatValue
	Integer integerValue 
	byte [] byteValue
	
	static constraints = { 
		stringValue(nullable:true)
		floatValue(nullable:true)
		integerValue(nullable:true)
		byteValue(nullable:true)	
	}

}
