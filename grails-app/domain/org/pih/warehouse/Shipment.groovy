package org.pih.warehouse

class Shipment {
	
 	// Core data elements
	String reference
	ReferenceType referenceType
	String comments	
	
	// Status 
	Boolean shipped
	Boolean delivered
	
	// Status dates
	Date expectedShippingDate
	Date actualShippingDate
	Date expectedDeliveryDate
	Date actualDeliveryDate
	
	// Shipping fields
	Location origin
	Location destination
	String trackingNumber				// should be part of a shipment mode: tracking number, carrier, service
	ShipmentMethod shipmentMethod
	ShipmentStatus shipmentStatus
	
	//SortedSet containers
	//SortedSet documents
	SortedSet events
	
	//Shipper shipper 		// the person or organization shipping the goods
	//Carrier carrier 		// the person or organization that actually carries the goods from A to B
	//Recipient recipient	// the person or organization that is receiving the goods
	
	
	
	// Core association mappings
	static hasMany = [ 
		containers : Container,
		documents : Document, 
		events : ShipmentEvent
		//products : Product,
		//shipmentLineItems : ShipmentItem, 
	]
	
	// Constraints
	static constraints = {
		
		reference(nullable:true)
		referenceType(nullable:true)
		
		origin(nullable:false)
		destination(nullable:false)
		comments(nullable:true)
		
		shipped(nullable:true)
		expectedShippingDate(nullable:true)
		actualShippingDate(nullable:true)

		// date validation looks something like this
		//expectedShippingDate(validator:{value, obj->
		//return value.after(obj.checkIn)
		//})

		
		delivered(nullable:true)
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(nullable:true)
		
		trackingNumber(nullable:true)
		shipmentMethod(nullable:true)
		shipmentStatus(nullable:true)
		
		events(nullable:true)
		documents(nullable:true)
		containers(nullable:true)
		
	}
}
