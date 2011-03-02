package org.pih.warehouse.core

import java.util.Date;

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location implements Comparable, java.io.Serializable {
	String name
	byte [] logo				// logo
	Address address
	LocationType locationType	
	Location parentLocation; 
	
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ parentLocation : Location ]
	static hasMany = [ locations : Location ]
	
	static constraints = {
		name(nullable:false, blank: false)
		address(nullable:true)
		locationType(nullable:false)
		parentLocation(nullable:true)
		logo(nullable:true, maxSize:10485760) // 10 MBs
		
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	
	String toString() { return this.name } 
	
	int compareTo(obj) { 
		return name <=> obj?.name
	}
}
