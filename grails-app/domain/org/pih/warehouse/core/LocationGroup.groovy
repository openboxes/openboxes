package org.pih.warehouse.core

import java.util.Date;

/**
 * Represents a logical grouping of locations (e.g. a site 
 * that has multiple facilities like depots, pharmacies, etc).
 */
class LocationGroup implements Serializable {

	String name

	Date dateCreated;
	Date lastUpdated;
	
	static constraints = { 
		name(nullable:true, maxSize: 255)
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	
	String toString() { return "$name"; }
}
