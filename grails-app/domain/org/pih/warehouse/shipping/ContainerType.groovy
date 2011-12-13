package org.pih.warehouse.shipping

import java.util.Date;

/**
 * Represents a means of packaging all or part of a Shipment
 * Examples of this would be Pallet, Box, Piece, Suitcase, etc
 */
class ContainerType implements java.io.Serializable {

	String id
	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;	
	
	static constraints = { 
		name(nullable:false, maxSize:255)
		description(nullable:true, maxSize:255)
		sortOrder(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}	

	static mapping = {
		id generator: 'uuid'
		sort "sortOrder"
	}

	String toString() { name } 
}
