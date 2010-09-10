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
class EventType {

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
	}
	
	static mapping = {
		sort "sortOrder"
	}

	
	String getStatus() { 
		return (eventStatus) ? eventStatus.name : "None";

	}
	
	String getOptionValue() { 
		return name + " [" + this.getStatus() + "]" 
	}
	
	
	String toString() { return "$name"; }	
	
}
