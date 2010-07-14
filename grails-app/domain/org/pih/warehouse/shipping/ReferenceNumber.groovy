package org.pih.warehouse.shipping

class ReferenceNumber {

	String identifier
	ReferenceNumberType referenceNumberType
	
	
	// Constraints
	static constraints = {
		identifier(nullable:true)
		referenceNumberType(nullable:true)
	}
	
	
	
	
}
