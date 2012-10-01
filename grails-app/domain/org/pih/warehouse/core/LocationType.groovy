/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
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
