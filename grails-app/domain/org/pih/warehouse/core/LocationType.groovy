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
			
	static hasMany = [ supportedActivities : String ]
	
	static constraints = { 
		name(nullable:false, maxSize: 255)
		code(nullable:false, maxSize: 255)
		description(nullable:true, maxSize: 255)
		sortOrder(nullable:true)		
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	
	String toString() { return "$name"; }
}
