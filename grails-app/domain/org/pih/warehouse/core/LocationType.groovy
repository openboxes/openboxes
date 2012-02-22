package org.pih.warehouse.core

import java.util.Date;
import org.springframework.context.i18n.LocaleContextHolder as LCH

/**
 * Represents the type of a Location
 * 
 */
class LocationType implements Comparable, Serializable {

	String id
	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
			
	static hasMany = [ supportedActivities : String ]
	
	static mapping = {
		id generator: 'uuid'
		// Needs to be eagerly fetched because of Location.supportsActivity() method
		supportedActivities lazy: false
	}
	
	static constraints = { 
		name(nullable:false, maxSize: 255)
		description(nullable:true, maxSize: 255)
		sortOrder(nullable:true)		
		dateCreated(display:false)
		lastUpdated(display:false)
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

  
		
	
	int compareTo(obj) {
		return description <=> obj?.description
	}
	
}
