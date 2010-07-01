package org.pih.warehouse

class Shipment {
		
	// Core data elements
	String name 
	String identifier
	
	// Status 
	//Boolean shipped
	//Boolean delivered
	
	// Status dates
	Date expectedShippingDate
	Date actualShippingDate
	Date expectedDeliveryDate
	Date actualDeliveryDate
	
	// Shipping fields
	Location origin
	Location destination
	String trackingNumber				// should be part of a shipment mode: tracking number, carrier, service
	ShipmentType shipmentType
	ShipmentMethod shipmentMethod
	ShipmentStatus shipmentStatus
	
	// Donation information
	Boolean donation 
	Organization donor
	
	//SortedSet containers
	//SortedSet documents
	SortedSet<ShipmentEvent> events
	
	//Shipper shipper 		// the person or organization shipping the goods
	//Carrier carrier 		// the person or organization that actually carries the goods from A to B
	//Recipient recipient	// the person or organization that is receiving the goods
	
	// Audit fields 
	Date dateCreated;
	Date lastUpdated;
	
	String getIdentifier() { 
		return (id) ? expectedShippingDate.format('MMddyyyy') + "-" + String.valueOf(id).padLeft(6, "0")  : "(new shipment)";		
	}
	
	
	// Core association mappings
	static hasMany = [events : ShipmentEvent,
	                  containers : Container,
	                  documents : Document, 	                  
	                  comments : Comment,
	                  //products : Product,
	                  //shipmentLineItems : ShipmentItem, 
	                  referenceNumbers : ReferenceNumber]
	
	// Constraints
	static constraints = {
		name(nullable:false, blank: false)
		identifier(nullable:true)
		
		origin(nullable:false, blank: false, validator: { value, obj -> return !value.equals(obj.destination)})
		destination(nullable:false, blank: false)
		
		expectedShippingDate(nullable:false)
		actualShippingDate(nullable:true)
		
		// date validation looks something like this
		//expectedShippingDate(validator:{value, obj->
		//	return value.after(obj.checkIn)
		//})
				
		
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(nullable:true)
		
		trackingNumber(nullable:true)
		shipmentType(nullable:true)
		shipmentMethod(nullable:true)
		shipmentStatus(nullable:true)
		
		donation(nullable:true)
		donor(nullable:true)
		
		events(nullable:true)

		dateCreated(nullable:true);
		lastUpdated(nullable:true);
		

		//referenceNumbers(nullable:true)
		//documents(nullable:true)
		//containers(nullable:true)
		
	}
}
