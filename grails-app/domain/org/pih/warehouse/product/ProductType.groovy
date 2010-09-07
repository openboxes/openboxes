package org.pih.warehouse.product;

import java.util.Date;

class ProductType {

    String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
	ProductType parentProductType // if this is a subtype, then parent will not be null
	
	static constraints = {
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		parentProductType(nullable:true)    
	}

	static mapping = {
		sort "sortOrder"
	}

	String toString() { return "$name"; }
		
}
