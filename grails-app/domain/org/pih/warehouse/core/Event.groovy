package org.pih.warehouse.core

import java.util.Date;
import org.pih.warehouse.core.Location;

/**
* Represents a particular Event of interest during the course of a Shipment
* Examples might be:
*
*  Shipment #1 Departed from Boston on 1/1/2010:
*  		{eventDate: 1/1/2010, eventLocation: Boston, eventType: SHIPPED}
*
*  Shipment #2 Arrived at Customs on 5/5/2010:
*  		{eventDate: 5/5/2010, eventLocation: Customs, eventType: ARRIVED}
*/
class Event implements Comparable, Serializable {
	
	Date eventDate				// The date and time on which the Event occurred	
	EventType eventType			// The type of the Event
	Location eventLocation		// The Location at which the Event occurred
	Date dateCreated;
	Date lastUpdated;
	
	//static belongsTo = [ shipment : Shipment ]
						 
	static constraints = {
		eventDate(nullable:true)
		eventType(nullable:true)
		eventLocation(nullable:true)
	}

	String toString() { return "$eventType $eventLocation on $eventDate"; }
	
	int compareTo(obj) { 
		def diff = obj?.eventDate <=> eventDate 
		if (diff==0) {
			diff = obj?.eventType <=> eventType
		}
		return diff
	}
}
