package org.pih.warehouse.shipping

import java.util.Date;

class ShipperService {

	String name					// Name of service (e.g. UPS Ground) 
	String description				// Description of the service (e.g. Delivery usually within 1-5 Business Days) 
	
	static belongsTo = [ shipper : Shipper ]
	
	static constraints = {
		name(nullable:true)
		description(nullable:true)
		shipper(nullable:true)
	}
		
	String toString() { return "$name"; }
}
