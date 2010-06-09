package org.pih.warehouse

class ReferenceNumber {

	String identifier
	ReferenceNumberType referenceNumberType
	
	
	// Constraints
	static constraints = {
		identifier(nullable:true)
		referenceNumberType(nullable:true)
	}
	
	
	
	
}
