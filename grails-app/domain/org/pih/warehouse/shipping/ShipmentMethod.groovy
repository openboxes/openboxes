package org.pih.warehouse.shipping

import java.util.Date;

class ShipmentMethod {
	
	Shipper carrier						// the shipping organization that will transport the goods
	ShipperService shipmentService		// the selected shipping service selected
	String trackingNumber				// should be part of a shipment mode: tracking number, carrier, service

	// Audit fields
	Date dateCreated;
	Date lastUpdated;
	
	static constraints = {
		carrier(nullable:true)
		shipmentService(nullable:true)
		trackingNumber(nullable:true)
	}
}
