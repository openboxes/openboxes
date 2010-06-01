package org.pih.warehouse

class ReferenceNumber {

	String identifier
	ReferenceType referenceType
	
	
	// Constraints
	static constraints = {
		identifier(nullable:true)
		referenceType(nullable:true)
	}
	
	
	
	
}
