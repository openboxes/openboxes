package org.pih.warehouse

/**
 * Represents the type of an Event
 * 
 * This is distinct from ShipmentStatus in that status is meant to reflect the overall
 * status of a Shipment from Supplier to final destination, whereas ShipmentEvent is
 * meant to represent a particular Event which occurs during the course of Shipment.
 */
class EventType {

	String code			// i18n message code
	String name
	String description 
		
	static constraints = { 
		code(nullable:true)
		name(nullable:true)
		description(nullable:true)
	}
	
	String toString() { return "$name"; }	
	
}
