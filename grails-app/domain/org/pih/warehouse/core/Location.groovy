package org.pih.warehouse.core

import java.util.Date;

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location implements java.io.Serializable {
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
		name(nullable:false)
		address(nullable:true)
		locationType(nullable:true)
		logo(nullable:true, maxSize:10485760) // 10 MBs
	}
	
	String toString() { return this.name } 
}
