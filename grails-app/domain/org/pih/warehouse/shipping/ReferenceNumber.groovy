package org.pih.warehouse.shipping

import java.io.Serializable;

class ReferenceNumber implements Serializable{

	String identifier
	ReferenceNumberType referenceNumberType
	
	
	// Constraints
	static constraints = {
		identifier(nullable:true, maxSize:255)
		referenceNumberType(nullable:true)
	}
	
	String toString() { identifier } 
}
