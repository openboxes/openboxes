package org.pih.warehouse.shipping

import java.util.Date;

class ShipperService {

	String id
	String name					// Name of service (e.g. UPS Ground) 
	String description				// Description of the service (e.g. Delivery usually within 1-5 Business Days) 
	
	static belongsTo = [ shipper : Shipper ]

	static mapping = {
		id generator: 'uuid'
	}

	static constraints = {
		name(nullable:true, maxSize: 255)
		description(nullable:true, maxSize: 255)
		shipper(nullable:true)
	}
		
	
	
	String toString() { return "$name"; }
}
