package org.pih.warehouse.product;

import java.util.Date;

/**
 * See http://en.wikipedia.org/wiki/Drug#Medication for administration route types
 */
class DrugRouteType {

	String name
	String description
	Integer sortOrder = 0;
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

}
