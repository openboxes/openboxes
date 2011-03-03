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
class EventType implements Comparable, Serializable {

	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;

	EventStatus eventStatus;		// SHIPPED or RECEIVED
			
	static transients = [ "status", "optionValue" ]
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		eventStatus(nullable:false)		
		
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
		return (description) ? description : name; 
	}
	
	
	String toString() { return "$name"; }	
	
	int compareTo(obj) {
		if (obj?.sortOrder && sortOrder) {
			return obj.sortOrder <=> sortOrder 
		}		
		return obj.id <=> id;
	}	
}
