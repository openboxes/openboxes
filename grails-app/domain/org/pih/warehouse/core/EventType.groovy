package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.core.EventStatus;

/**
 * Represents the type of an Event
 * 
 * This is distinct from ShipmentStatus in that status is meant to reflect the overall
 * status of a Shipment from Supplier to final destination, whereas ShipmentEvent is
 * meant to represent a particular Event which occurs during the course of Shipment.
 */
class EventType implements Comparable {

	String name
	String description
	Integer sortOrder = 0;
	ActivityType activityType;
	Date dateCreated;
	Date lastUpdated;

	EventStatus eventStatus;		// default status: initial, completed, pending
			
	static transients = [ "status", "optionValue" ]
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		eventStatus(nullable:true)		
		activityType(nullable:true)
		
		dateCreated(display:false)
		lastUpdated(display:false)
		status(display:false)
		optionValue(display:false)		
	}
	
	static mapping = {
		sort "sortOrder"
	}

	
	String getStatus() { 
		return (eventStatus) ? eventStatus.name : "Invalid";
	}
	
	String getOptionValue() { 
		return (activityType ? activityType.name + " - " : "") + (description) ? description : name; 
	}
	
	
	String toString() { return "$name"; }	
	
	int compareTo(obj) {
		if (obj?.sortOrder && sortOrder) {
			return obj.sortOrder <=> sortOrder 
		}		
		return obj.id <=> id;
	}	
}
