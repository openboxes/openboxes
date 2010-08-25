package org.pih.warehouse.donation

import java.util.Date;

class Donor {
	
	String name
	String description

	// Audit fields
	Date dateCreated;
	Date lastUpdated;

	/*
	static mapping = {
		tablePerHierarchy false
		table 'donor'
	}*/
	
	static constraints = {
		name(nullable:false)
		description(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}

}
