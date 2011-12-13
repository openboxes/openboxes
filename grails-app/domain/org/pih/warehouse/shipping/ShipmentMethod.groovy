package org.pih.warehouse.shipping

import java.util.Date;

class ShipmentMethod implements java.io.Serializable {
	
	static belongsTo = Shipment
	
	String id
	Shipper shipper					// If you just want to store the shipper information
	ShipperService shipperService	// the selected shipping service
	String trackingNumber			// should be part of a shipment mode: tracking number, carrier, service
	Date dateCreated;
	Date lastUpdated;
	
	static mapping = {
		id generator: 'uuid'
	}
	
	static constraints = {
		shipper(nullable:true)
		shipperService(nullable:true)
		trackingNumber(nullable:true, maxSize: 255)
		//shipment(nullable:true)
	}
}
