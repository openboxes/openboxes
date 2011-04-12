package org.pih.warehouse.shipping

import java.util.Date;

/**
 * Represents the type of shipment (Sea, Air, Suitcase)
 */
class ShipmentType implements java.io.Serializable {

	String name
	String description
	Integer sortOrder = 0;
	Date dateCreated;
	Date lastUpdated;
	
	static constraints = {
		name(nullable:false, maxSize: 255)
		description(nullable:true, maxSize: 255)
		sortOrder(nullable:true)
		dateCreated(display:false)
		lastUpdated(display:false)
	}

	static mapping = {
		sort "sortOrder"
	}

	
	String toString() { name }	
		
}
