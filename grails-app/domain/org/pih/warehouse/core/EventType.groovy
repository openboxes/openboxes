package org.pih.warehouse.core

import java.util.Date;

import org.pih.warehouse.core.EventCode;

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

	EventCode eventCode;		// CREATED, SHIPPED or RECEIVED
			
	static transients = [ "optionValue" ]
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		eventCode(nullable:false)		
		
		dateCreated(display:false)
		lastUpdated(display:false)
		
		optionValue(display:false)		
	}
	
	static mapping = {
		sort "sortOrder"
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
