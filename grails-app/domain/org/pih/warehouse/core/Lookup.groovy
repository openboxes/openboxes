package org.pih.warehouse.core;

import java.util.Date;

class Lookup { 
	
	String name;
	String code;
	String description;
	Integer sortOrder;
	Date dateCreated;
	Date lastUpdated;
	
	
	static constraints = {
		name(nullable:false)
		code(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	
	static mapping = {
		sort "sortOrder"
	}

}