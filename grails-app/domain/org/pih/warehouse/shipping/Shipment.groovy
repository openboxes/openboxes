package org.pih.warehouse.shipping

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventCode;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.shipping.ReferenceNumber;
import org.pih.warehouse.donation.Donor;
import java.io.Serializable;

class Shipment implements Serializable {
	
	String name 					// user-defined name of the shipment 
	String shipmentNumber			// an auto-generated shipment number
	Date expectedShippingDate		// the date the origin expects to ship the goods (required)
	Date expectedDeliveryDate		// the date the destination should expect to receive the goods (optional)
	Float totalValue				// the total value of all items in the shipment		
	String additionalInformation	// any additional information about the shipment (e.g., comments)

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
		"containersByType", 
		"mostRecentEvent", 
		"mostRecentStatus",
		"actualShippingDate",
		"actualDeliveryDate" ]
	
	// Core association mappings
	static hasMany = [events : Event,
	                  comments : Comment,
	                  containers : Container,
	                  documents : Document, 	                  
					  shipmentItems : ShipmentItem,
	                  referenceNumbers : ReferenceNumber ]
	

	
	// Ran into Hibernate bug HHH-4394 and GRAILS-4089 when trying to order the associations.  This is due to the 
	// fact that the many side of the association (e.g. 'events') does not have a belongsTo 'shipment'.  So instead
	// of adding a foreign key reference to the 'event' table, GORM creates a new join table 'shipment_event' to 
	// map 'events' to 'shipments' (which is exactly what I want).  However, the events are not 'eagerly' fetched
	// so the query to pull the data (and sort) only uses the 'shipment_event' table.  So for now, I'm going to 
	// use a SortedSet for events and have the Event class implement Comparable. 

	static mapping = {
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
		//events joinTable:[name:'shipment_event', key:'shipment_id', column:'event_id']
	}	

	/*	
	static mapping = {
		containers sort: 'dateCreated', order: 'asc'
		events sort: 'eventDate', order: 'desc'
		documents sort: 'dateCreated', order: 'desc'
		comments sort: 'dateCreated', order: 'desc'
		
		//events joinTable:[name:'shipment_event', key:'shipment_id', column:'event_id'], sort: 'eventDate', order: 'desc'
		//documents joinTable:[name:'shipment_document', key:'shipment_id', column:'document_id'], sort: 'dateCreated', order: 'asc'	
		//comments joinTable:[name:'shipment_comment', key:'shipment_id', column:'comment_id'], sort: 'dateCreated', order: 'desc'		
	}
	*/

	// Constraints
	static constraints = {
		name(nullable:false, blank: false)
		shipmentNumber(nullable:true)	
		origin(nullable:false, blank: false, 
			validator: { value, obj -> !value.equals(obj.destination)})
		destination(nullable:false, blank: false)		
		expectedShippingDate(nullable:false, 
			validator: { value, obj-> !obj.expectedDeliveryDate || value.before(obj.expectedDeliveryDate + 1)})		
		expectedDeliveryDate(nullable:true)	// optional		
		shipmentType(nullable:true)
		shipmentMethod(nullable:true)
		receipt(nullable:true)
		additionalInformation(nullable:true)
		carrier(nullable:true)
		recipient(nullable:true)
		donor(nullable:true)
		totalValue(nullable:true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
		
		// a shipment can't have two reference numbers of the same type (we may want to change this, but UI makes this assumption at this point)
		referenceNumbers ( validator: { referenceNumbers ->
        	referenceNumbers?.collect( {it.referenceNumberType?.id} )?.unique( { a, b -> a <=> b } )?.size() == referenceNumbers?.size()        
		} )
	}

	String toString() { return "$name"; }
	
	/** 
	 * Transient method that gets all shipment items two-levels deep.
	 * 
	 * TODO Need to make this recursive.
	 * 	
	 * @return
	 */
	
	List<ShipmentItem> getAllShipmentItems() { 		
		List<ShipmentItem> allShipmentItems = new ArrayList<ShipmentItem>();		
		for (container in containers) {
			for (item in container?.shipmentItems) { 
				allShipmentItems.add(item);				
			}
			for (childContainer in container?.containers) { 
				for (childItem in childContainer?.shipmentItems) { 
					allShipmentItems.add(childItem);
				}
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
	
	Boolean hasShipped() {
		return events.any { it.eventType?.eventCode == EventCode.SHIPPED }
	}
	
	Boolean wasReceived() { 
		return events.any { it.eventType?.eventCode == EventCode.RECEIVED }
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
	
	EventType getMostRecentStatus() { 		
		return getMostRecentEvent()?.getEventType()
	}
		
	/**
	 * Adds a new container to the shipment of the specified type
	 */
	Container addNewContainer (ContainerType containerType) {
		def sortOrder = (this.containers) ? this.containers.size()+1 : 1
		
		def container = new Container(
			containerType: containerType, 
			shipment: this,
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
}

