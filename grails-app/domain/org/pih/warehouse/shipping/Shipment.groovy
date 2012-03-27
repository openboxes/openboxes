package org.pih.warehouse.shipping

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventCode;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.shipping.ReferenceNumber;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.inventory.Transaction;

import java.io.Serializable;

class Shipment implements Comparable, Serializable {
	
	String id
	String name 					// user-defined name of the shipment 
	String shipmentNumber			// an auto-generated shipment number
	Date expectedShippingDate		// the date the origin expects to ship the goods (required)
	Date expectedDeliveryDate		// the date the destination should expect to receive the goods (optional)
	Float statedValue
	Float totalValue				// the total value of all items in the shipment		
	String additionalInformation	// any additional information about the shipment (e.g., comments)
	Float weight											// weight of container
	String weightUnits  = Constants.DEFAULT_WEIGHT_UNITS	// standard weight unit: kg, lb
	
	// Audit fields
	Date dateCreated
	Date lastUpdated
	
	// One-to-one associations
	Location origin					// the location from which the shipment will depart
	Location destination			// the location to which the shipment will arrive
	ShipmentType shipmentType		// the shipment type: Air, Sea Freight, Suitcase
	ShipmentMethod shipmentMethod	// the shipping carrier and shipping service used	
	Receipt receipt					// the receipt for this shipment
	Person carrier 					// the person or organization that actually carries the goods from A to B
	Person recipient				// the person or organization that is receiving the goods	
	Donor donor						// the information about the donor (OPTIONAL)
	
	// One-to-many associations
	SortedSet events;
	
	List documents;
	List comments;
	List referenceNumbers;
	
	static transients = [ 
		"allShipmentItems",
		"unpackedShipmentItems",
		"containersByType", 
		"mostRecentEvent", 
		"status",
		"actualShippingDate",
		"actualDeliveryDate" ]
	
	static mappedBy = [outgoingTransactions: 'outgoingShipment',
		incomingTransactions: 'incomingShipment']
	
	// Core association mappings
	static hasMany = [events : Event,
	                  comments : Comment,
	                  containers : Container,
	                  documents : Document, 	                  
					  shipmentItems : ShipmentItem,
	                  referenceNumbers : ReferenceNumber,
					  outgoingTransactions : Transaction,
					  incomingTransactions : Transaction ]
	

	
	// Ran into Hibernate bug HHH-4394 and GRAILS-4089 when trying to order the associations.  This is due to the 
	// fact that the many side of the association (e.g. 'events') does not have a belongsTo 'shipment'.  So instead
	// of adding a foreign key reference to the 'event' table, GORM creates a new join table 'shipment_event' to 
	// map 'events' to 'shipments' (which is exactly what I want).  However, the events are not 'eagerly' fetched
	// so the query to pull the data (and sort) only uses the 'shipment_event' table.  So for now, I'm going to 
	// use a SortedSet for events and have the Event class implement Comparable. 

	static mapping = {
		id generator: 'uuid'
		additionalInformation type: "text"
		events cascade: "all-delete-orphan"
		comments cascade: "all-delete-orphan"
		containers cascade: "all-delete-orphan"
		documents cascade: "all-delete-orphan"
		shipmentItems cascade: "all-delete-orphan"
		shipmentMethod cascade: "all-delete-orphan"
		referenceNumbers cascade: "all-delete-orphan"
		receipt casade: "all-delete-orphan"
		containers sort: 'sortOrder', order: 'asc'
		//shipmentItems sort: 'lotNumber', order: 'asc'
		//events joinTable:[name:'shipment_event', key:'shipment_id', column:'event_id']
	}	

	// Constraints
	static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		shipmentNumber(nullable:true, maxSize: 255)	
		origin(nullable:false, blank: false, 
			validator: { value, obj -> !value.equals(obj.destination)})
		destination(nullable:false, blank: false)		
		expectedShippingDate(nullable:false, 
			validator: { value, obj-> !obj.expectedDeliveryDate || value.before(obj.expectedDeliveryDate + 1)})		
		expectedDeliveryDate(nullable:true)	// optional		
		shipmentType(nullable:false)
		shipmentMethod(nullable:true)
		receipt(nullable:true)
		additionalInformation(nullable:true, maxSize: 2147483646)
		carrier(nullable:true)
		recipient(nullable:true)
		donor(nullable:true)
		statedValue(nullable:true, max:99999999F)
		totalValue(nullable:true, max:99999999F)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		weight(nullable:true, max:99999999F)
		weightUnits(nullable:true)
		// a shipment can't have two reference numbers of the same type (we may want to change this, but UI makes this assumption at this point)
		referenceNumbers ( validator: { referenceNumbers ->
        	referenceNumbers?.collect( {it.referenceNumberType?.id} )?.unique( { a, b -> a <=> b } )?.size() == referenceNumbers?.size()        
		} )

		// a shipment can't have two events with the same event code (this should be the case for the current event codes: CREATED, SHIPPED, RECEIVED)
		// we may want to change this in the future?
		events ( validator: { events ->
        	events?.collect( {it.eventType?.eventCode} )?.unique( { a, b -> a <=> b } )?.size() == events?.size()        
		} )
	}

	String toString() { return "$name"; }
	
	/**
	 * Sort by name
	 */
	
	// TODO: is this in descending order for a good reason?
	int compareTo(obj) { 
		obj.name <=> name 
	}
	
	/** 
	 * Transient method that gets all shipment items two-levels deep.
	 * 
	 * TODO Need to make this recursive.
	 * 	
	 * @return
	 */
	
	Collection<ShipmentItem> getAllShipmentItems() { 		
		List<ShipmentItem> shipmentItems = new ArrayList<ShipmentItem>();	
		
		for (shipmentItem in unpackedShipmentItems) { 
			shipmentItems.add(shipmentItem)
		}
			
		for (container in containers) {
			for (shipmentItem in container?.shipmentItems) { 
				shipmentItems.add(shipmentItem);				
			}
			for (childContainer in container?.containers) { 
				for (shipmentItem in childContainer?.shipmentItems) { 
					shipmentItems.add(shipmentItem);
				}
			}
		}
		return shipmentItems;		
	}
	
	Collection<ShipmentItem> getUnpackedShipmentItems() { 
		return shipmentItems.findAll { !it.container }  
	}
	
	String getShipmentNumber() {
		return (id) ? "S" + String.valueOf(id).padLeft(6, "0")  : "(new shipment)";
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
	
	Boolean isPending() { 
		return !this.hasShipped() && !this.wasReceived()
	}
	
	
	Boolean hasShipped() {
		return events.any { it.eventType?.eventCode == EventCode.SHIPPED }
	}
	
	Boolean wasReceived() { 
		return events.any { it.eventType?.eventCode == EventCode.RECEIVED }
	}
	
	/*
	Boolean isIncoming(Location currentLocation) { 
		//return destination?.id == currentLocation?.id
		return true;
	}
	
	Boolean isOutgoing(Location currentLocation) { 
		//return origin?.id == currentLocation?.id
		return false;
	}
	
	Boolean isIncomingOrOutgoing(Location currentLocation) { 
		return isIncoming(currentLocation) || isOutgoing(currentLocation)
	}
	
	Boolean isDeleteAllowed(Location currentLocation) { 
		return isIncomingOrOutgoing(currentLocation)
	}
	
	Boolean isEditAllowed(Location currentLocation) { 
		return isIncomingOrOutgoing(currentLocation)
	}
	*/
	
	Boolean isReceiveAllowed() { 
		return hasShipped() && !wasReceived()
	}
	
	Boolean isSendAllowed() { 
		return !hasShipped() && !wasReceived()
	}
	
	ReferenceNumber getReferenceNumber(String typeName) { 
		def referenceNumberType = ReferenceNumberType.findByName(typeName);
		if (referenceNumberType) { 
			for(referenceNumber in referenceNumbers) { 
				if (referenceNumber.referenceNumberType == referenceNumberType) { 
					return referenceNumber;
				}
			}
		}
		return null;
		
	}
	
	Date getActualShippingDate() { 
		for (event in events) { 
			if (event?.eventType?.eventCode == EventCode.SHIPPED) { 
				return event?.eventDate;
			}
		}
		return null;
	}

	Date getActualDeliveryDate() { 
		for (event in events) {
			if (event?.eventType?.eventCode == EventCode.RECEIVED) {
				return event?.eventDate;
			}
		}
		return null;
	}
		
	
	Event getMostRecentEvent() { 		
		if (events && events.size() > 0) {
			return events.iterator().next();
		}
		return null;
	}
	
	ShipmentStatus getStatus() { 		
		if (this.wasReceived()) {
			return new ShipmentStatus( [ code:ShipmentStatusCode.RECEIVED, 
			                             date:this.getActualDeliveryDate(),
			                             location:this.destination] )
		}
		else if (this.hasShipped()) {
			return new ShipmentStatus( [ code:ShipmentStatusCode.SHIPPED,
			                             date:this.getActualShippingDate(),
			                             location:this.origin] )
		}
		else {
			return new ShipmentStatus( [ code:ShipmentStatusCode.PENDING, 
			                             date:null,
			                             location:null] )
		}
	}
		
	/**
	 * Adds a new container to the shipment of the specified type
	 */
	Container addNewContainer (ContainerType containerType) {
		def sortOrder = (this.containers) ? this.containers.size() : 0
		
		def container = new Container(
			containerType: containerType, 
			shipment: this,
			recipient: this.recipient,
			sortOrder: sortOrder
		)
		
		this.addToContainers(container)
			
		return container
	}
	
	/**
	 * Clones the specified container
	 */
	void cloneContainer(Container container, Integer quantity) {
		def newContainer = new Container(
			
		)
	}
	
	Float totalWeightInKilograms() {
		return containers.findAll { it.parentContainer == null }.collect { it.totalWeightInKilograms() }.sum()
	}

	Float totalWeightInPounds() {
		return containers.findAll { it.parentContainer == null }.collect { it.totalWeightInPounds() }.sum()
	}

}

