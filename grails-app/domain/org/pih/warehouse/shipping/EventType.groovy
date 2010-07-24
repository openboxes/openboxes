package org.pih.warehouse.shipping

import org.pih.warehouse.core.Type;

/**
 * Represents the type of an Event
 * 
 * This is distinct from ShipmentStatus in that status is meant to reflect the overall
 * status of a Shipment from Supplier to final destination, whereas ShipmentEvent is
 * meant to represent a particular Event which occurs during the course of Shipment.
 */
class EventType extends Type{

	// Indicates whether this event type can represent 
	// an initial, pending, and/or completed state
	Boolean initial = false;
	Boolean pending = false;
	Boolean complete = false;
	
	static constraints = { 
		initial(nullable:true)
		pending(nullable:true)
		complete(nullable:true)
	}
	
	static transients = [ "status", "optionValue" ]
	
	String getStatus() { 
		if (initial) return "Initiated"
		else if (complete) return "Completed"
		return "Pending"
	}
	
	String getOptionValue() { 
		return name + " [" + this.getStatus() + "]" 
	}
	
	
	String toString() { return "$name"; }	
	
}
