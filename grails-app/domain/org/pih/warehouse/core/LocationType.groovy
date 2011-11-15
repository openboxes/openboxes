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
	
	static mapping = {
		// Needs to be eagerly fetched because of Location.supportsActivity() method
		supportedActivities lazy: false
	}
	
	/**
	* Indicates whether the location type supports the given activity.
	*
	* @param activity	the given activity
	* @return	true if the activity is supported, false otherwise
	*/
   Boolean supports(ActivityCode activity) {
	   return supports(activity.id)
   }

   /**
	* Indicates whether the location type supports the given activity.
	*
	* @param activity	the given activity id
	* @return	true if the activity is supported, false otherwise
	*/
   Boolean supports(String activity) {
		return supportedActivities?.contains(activity);
   }
	
	
	String toString() { return "$name"; }
}
