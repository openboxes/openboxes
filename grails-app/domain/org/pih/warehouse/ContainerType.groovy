package org.pih.warehouse

/**
 * Represents a means of packaging all or part of a Shipment
 * Examples of this would be Pallet, Box, Piece, Suitcase, etc
 */
class ContainerType {

	String code		// i18n message code
	String name
	String description
	
	static constraints = { 
		code(nullable:true)
		name(nullable:false)
		description(nullable:true)
		
	}

	String toString() { return "$name"; }	
	
}
