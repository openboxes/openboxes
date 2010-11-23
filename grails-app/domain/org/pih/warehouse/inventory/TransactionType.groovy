package org.pih.warehouse.inventory;

import java.util.Date;

class TransactionType {
	
	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
		
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}

	static mapping = {
		sort "sortOrder"
	}
	
}
