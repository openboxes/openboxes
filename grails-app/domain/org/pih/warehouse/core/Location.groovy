package org.pih.warehouse.core

import java.util.Date;

/**
 * A location can be a customer, warehouse, or supplier.  
 */
class Location implements Comparable, java.io.Serializable {
	String name
	byte [] logo				// logo
	Address address
	String fgColor
	String bgColor
	LocationGroup locationGroup;
	LocationType locationType	
	Location parentLocation; 
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ parentLocation : Location ]
	static hasMany = [ locations : Location, supportedActivities : String ]
	
	static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		address(nullable:true)
		locationType(nullable:false)
		parentLocation(nullable:true)
		bgColor(nullable:true)
		fgColor(nullable:true)
		logo(nullable:true, maxSize:10485760) // 10 MBs
		
		dateCreated(display:false)
		lastUpdated(display:false)
	}
	
	String toString() { return this.name } 
	
	int compareTo(obj) { 
		return name <=> obj?.name
	}
	
	/**
	 * Indicates whether the location supports the given activity.
	 * 
	 * @param activity	the given activity
	 * @return	true if the activity is supported, false otherwise
	 */
	Boolean supportsActivity(ActivityCode activity) {
		return supportsActivity(activity.id)
	}

	/**
	 * Indicates whether the location supports the given activity.
	 * 
	 * @param activity	the given activity id
	 * @return	true if the activity is supported, false otherwise
	 */
	Boolean supportsActivity(String activity) { 
		boolean supportsActivity
		if (supportedActivities) {
			supportsActivity = supportedActivities?.contains(activity);
		}
		else {
			supportsActivity = locationType?.supportedActivities?.contains(activity);
		}
		return supportsActivity;

	}
	
	Boolean isWarehouse() {
		return locationType.id == LocationType.findById(Constants.WAREHOUSE_LOCATION_TYPE_ID).id
	}
}
