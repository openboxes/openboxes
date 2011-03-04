package org.pih.warehouse.core

import java.util.Date;

/**
 * Represents the type of a Location
 * 
 */
class LocationType implements Serializable {

	String name
	String code
	String description
	Integer sortOrder = 0;
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
	
	String toString() { return "$name"; }
}
