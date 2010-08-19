package org.pih.warehouse.core

import java.util.Date;

class Type {	

	String name
	String description
	Integer sortOrder = 0;

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
		
	static constraints = { 
		name(nullable:false)
		description(nullable:true)		
		sortOrder(nullable:true)
	}

	static mapping = {
		sort "sortOrder"
	}
	
	String toString() { return "$name"; }	
	
}
