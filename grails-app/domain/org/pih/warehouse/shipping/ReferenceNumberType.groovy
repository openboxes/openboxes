package org.pih.warehouse.shipping

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a means of packaging all or part of a Shipment
 * Examples of this would be Pallet, Box, Piece, Suitcase, etc
 */
class ReferenceNumberType implements Serializable {
	
	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;	
	
	static constraints = { 
		name(nullable:false)
		description(nullable:true)
		sortOrder(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}	

	static mapping = {
		sort "sortOrder"
	}

		
}
