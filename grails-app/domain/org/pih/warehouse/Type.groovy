package org.pih.warehouse

class Type {	

	String name
	String description
	
	static constraints = { 
		name(nullable:false)
		description(nullable:true)		
	}

	String toString() { return "$name"; }	

}
