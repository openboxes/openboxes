package org.pih.warehouse.inventory;

import java.util.Date;
import org.pih.warehouse.core.TransactionTypes;

class TransactionType {
	
	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
	TransactionTypes transactionType;
	
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		transactionType(nullable:false)
		dateCreated(display:false)
		lastUpdated(display:false)
	}

	static mapping = {
		sort "sortOrder"
	}
	
}
