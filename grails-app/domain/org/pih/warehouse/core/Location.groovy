package org.pih.warehouse.core

import java.util.Date;

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location {
	String name
	byte [] logo				// logo
	String logoUrl 
	Address address
	Date dateCreated;
	Date lastUpdated;
	
	static constraints = {
		logo(nullable:true, maxSize:10485760) // 10 MBs
		logoUrl(nullable:true)
	}
}
