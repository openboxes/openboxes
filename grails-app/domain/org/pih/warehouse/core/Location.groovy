package org.pih.warehouse.core

import java.util.Date;

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location {

	String name
	String logoUrl 
	Address address

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static constraints = {
		logoUrl(nullable:true)
	}
}
