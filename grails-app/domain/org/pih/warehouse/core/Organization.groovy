package org.pih.warehouse.core

class Organization {

	String name
	String description 
	
	static constraints = { 
		description(nullable:true)
	}
	
}
