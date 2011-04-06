package org.pih.warehouse.inventory;

import java.util.Date;
import org.pih.warehouse.inventory.TransactionCode 

class TransactionType implements Serializable {
	
	String name
	String description
	Integer sortOrder = 0
	Date dateCreated
	Date lastUpdated
	TransactionCode transactionCode
	
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		transactionCode(nullable:false)
		dateCreated(display:false)
		lastUpdated(display:false)
	}

	static mapping = {
		sort "sortOrder"
	}
	
}
