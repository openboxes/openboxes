package org.pih.warehouse.core

import java.util.Date;

class Organization extends Party {

	String name
	String description 
	
	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	
	static mapping = { 
		tablePerHierarchy false
		table 'organization' 
	}
	

	static constraints = { 
		name(nullable:false)
		description(nullable:true)
	}
	
}
