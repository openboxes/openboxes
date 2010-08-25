package org.pih.warehouse.shipping

import java.util.Date;

class ShipmentMethod {
	
	ShipperService shipperService		// the selected shipping service
	String trackingNumber			// should be part of a shipment mode: tracking number, carrier, service
	Date dateCreated;
	Date lastUpdated;
	
	static belongsTo = [ shipment : Shipment ]
	
	static constraints = {
		shipperService(nullable:true)
		trackingNumber(nullable:true)
		shipment(nullable:true)
	}
}
