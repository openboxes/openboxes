package org.pih.warehouse.shipping

import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Organization;
import org.pih.warehouse.donation.Donor;


class Shipment {
		
	String name 					// user-defined name of the shipment 
	String shipmentNumber			// an auto-generated shipment number

	Location origin					// the location from which the shipment will depart
	Location destination			// the location to which the shipment will arrive

	Date expectedDeliveryDate
	Date expectedShippingDate

	ShipmentType shipmentType		// the shipment type: Air, Sea Freight, Suitcase
	ShipmentMethod shipmentMethod	// the shipping carrier and shipping service used	
	ShipmentEvent currentEvent		// the current event (REMOVE)

	Person courier 					// the person or organization that actually carries the goods from A to B
	Person recipient				// the person or organization that is receiving the goods
	Donor donor						// the information about the donor (OPTIONAL)
	Float totalValue				// the total value of all items in the shipment
		
	// Audit fields 
	Date dateCreated;
	Date lastUpdated;
	
	
	static transients = [ 
		"allShipmentItems",
		"containersByType", 
		"mostRecentEvent", 
		"mostRecentStatus"]
	
	// Core association mappings
	static hasMany = [events : ShipmentEvent,
	                  containers : Container,
	                  documents : Document, 	                  
	                  comments : Comment,
	                  referenceNumbers : ReferenceNumber ]

	static mapping = {
		events sort: 'eventDate', order: 'desc'
		containers sort: 'dateCreated', order: 'asc'
		documents sort: 'dateCreated', order: 'asc'	
		comments joinTable:[name:'shipment_comment', key:'shipment_id', column:'comment_id'],
			sort: 'dateCreated', order: 'desc'		
	}

	// Constraints
	static constraints = {
		name(nullable:false, blank: false)
		shipmentNumber(nullable:true)	
		origin(nullable:false, blank: false, 
			validator: { value, obj -> return !value.equals(obj.destination)})
		destination(nullable:false, blank: false)		
		expectedShippingDate(nullable:true)
		//expectedShippingDate(validator:{value, obj-> return value.after(obj.checkIn)})		
		expectedDeliveryDate(nullable:true)
		//expectedDeliveryDate(validator:{value, obj-> return value.after(obj.checkIn)})		
		shipmentType(nullable:true)
		shipmentMethod(nullable:true)
		currentEvent(nullable:true)		

		courier(nullable:true)
		recipient(nullable:true)
		donor(nullable:true)
		totalValue(nullable:true)
		
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	
		events(nullable:true)
	}
	
	List<ShipmentItem> getAllShipmentItems() { 		
		List<ShipmentItem> allShipmentItems = new ArrayList<ShipmentItem>();		
		for (container in containers) {
			for (item in container.getShipmentItems()) { 
				allShipmentItems.add(item);				
			}
		}
		return allShipmentItems;		
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
	
	String getMostRecentStatus() { 
		if(mostRecentEvent) { 
			if (mostRecentEvent.getEventType()) { 
				return mostRecentEvent.getEventType().getStatus();
			}			
		}
		return "Pending";
	}
	
		
	/*
	int compareTo(obj) {
		dateCreated.compareTo(obj.dateCreated)
	}*/
 

		
}

