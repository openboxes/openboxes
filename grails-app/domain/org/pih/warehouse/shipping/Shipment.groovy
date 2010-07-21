package org.pih.warehouse.shipping

import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Organization;

class Shipment {
		
	def searchable = true
	
	// Core data elements
	String name 
	String shipmentNumber
	
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
	//SortedSet<ShipmentEvent> events
		
	//Shipper shipper 		// the person or organization shipping the goods
	//Carrier carrier 		// the person or organization that actually carries the goods from A to B
	//Recipient recipient	// the person or organization that is receiving the goods
	
	// Audit fields 
	Date dateCreated;
	Date lastUpdated;
	
	
	static transients = [ "allShipmentItems", "containersByType", "mostRecentEvent"]
	
	// Core association mappings
	static hasMany = [events : ShipmentEvent,
	                  containers : Container,
	                  documents : Document, 	                  
	                  comments : Comment,
	                  //products : Product,
	                  //shipmentLineItems : ShipmentItem, 
	                  referenceNumbers : ReferenceNumber]

	static mapping = {
		events sort: 'eventDate', order: 'desc'
	}

	// Constraints
	static constraints = {
		name(nullable:false, blank: false)
		shipmentNumber(nullable:true)
		
		origin(nullable:false, blank: false, validator: { value, obj -> return !value.equals(obj.destination)})
		destination(nullable:false, blank: false)
		
		expectedShippingDate(nullable:true)
		actualShippingDate(nullable:true)
		expectedDeliveryDate(nullable:true)
		actualDeliveryDate(nullable:true)

		// date validation looks something like this
		//expectedShippingDate(validator:{value, obj->
		//	return value.after(obj.checkIn)
		//})
				
		
		
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
	
	
	List<ShipmentItem> getAllShipmentItems() { 		
		
	}

	String getShipmentNumber() {
		return (id) ? String.valueOf(id).padLeft(6, "0")  : "(new shipment)";
	}


	Map<String, List<Container>> getContainersByType() { 
		Map<String, List<Container>> containerMap = new HashMap<String,List<Container>>();
		containers.each { 
			
			def containersByType = containerMap.get(it.containerType.name);
			if (!containersByType) { 
				containersByType = new ArrayList<Container>();
			}
			containersByType.add(it);
			containerMap.put(it.containerType.name, containersByType)			
		}
		return containerMap;
		
	}
	
	ShipmentEvent getMostRecentEvent() { 		
		if (events && events.size() > 0) {
			return events.iterator().next();
		}
		return null;
	}
		
	/*
	int compareTo(obj) {
		dateCreated.compareTo(obj.dateCreated)
	}*/
 

		
}

