package org.pih.warehouse.shipping

import java.io.Serializable;

class ReferenceNumber implements Serializable{

	String id
	String identifier
	ReferenceNumberType referenceNumberType
	
	static mapping = {
		id generator: 'uuid'
	}		
	
	// Constraints
	static constraints = {
		identifier(nullable:true, maxSize:255)
		referenceNumberType(nullable:true)
	}
	
	String toString() { identifier } 
}
