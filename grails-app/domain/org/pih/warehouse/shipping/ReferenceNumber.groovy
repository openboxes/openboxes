package org.pih.warehouse.shipping

import java.io.Serializable;

class ReferenceNumber implements Serializable{

	String identifier
	ReferenceNumberType referenceNumberType
	
	
	// Constraints
	static constraints = {
		identifier(nullable:true)
		referenceNumberType(nullable:true)
	}
	
	String toString() { identifier } 
}
