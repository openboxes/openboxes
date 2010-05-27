package org.pih.warehouse

class Organization {

	String name
	String description 
	
	static constraints = { 
		description(nullable:true)
	}
	
}
