package org.pih.warehouse.donation

import java.util.Date;

class Donor {
	
	String name
	String description
	Date dateCreated;
	Date lastUpdated;

	/*
	static mapping = {
		tablePerHierarchy false
		table 'donor'
	}*/
	
	static constraints = {
		name(nullable:false, maxSize:255)
		description(nullable:true, maxSize:255)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}

}
