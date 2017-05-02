/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping

import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.*
import org.pih.warehouse.donation.Donor
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.receiving.Receipt

// import java.io.Serializable;

class Shipment implements Comparable, Serializable {

    def beforeInsert = {
        //def currentUser = AuthService.currentUser.get()
        //if (currentUser) {
        //    createdBy = currentUser
        //    updatedBy = currentUser
        //}

    }
    def beforeUpdate = {
        //def currentUser = AuthService.currentUser.get()
        //if (currentUser) {
        // updatedBy = currentUser
        //}
    }

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

    Long shipmentItemCount

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
			"actualDeliveryDate",
			"recipients",
			"consignorAddress",
			"consigneeAddress"
    ]
	
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
		cache true
		additionalInformation type: "text"
		events cascade: "all-delete-orphan"
		comments cascade: "all-delete-orphan"
		//containers cascade: "all-delete-orphan"
		documents cascade: "all-delete-orphan"
		//shipmentItems cascade: "all-delete-orphan"
        shipmentItemCount(formula: '(select count(shipment_item.id) from shipment_item where (shipment_item.shipment_id = id))')
		shipmentMethod cascade: "all-delete-orphan"
		referenceNumbers cascade: "all-delete-orphan"
		receipt cascade: "all-delete-orphan"
		containers sort: 'sortOrder', order: 'asc'
		//shipmentItems sort: 'lotNumber', order: 'asc'
		//events joinTable:[name:'shipment_event', key:'shipment_id', column:'event_id']
        //outgoingTransactions cascade: "all-delete-orphan"
        //incomingTransactions cascade: "all-delete-orphan"
	}

	// Constraints
	static constraints = {
		name(nullable:false, blank: false, maxSize: 255)
		shipmentNumber(nullable:true, maxSize: 255)	
		origin(nullable:false, 
			validator: { value, obj -> !value.equals(obj.destination)})
		destination(nullable:false)		
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
	
	//String getShipmentNumber() {
	//	return (id) ? "S" + String.valueOf(id).padLeft(6, "0")  : "(new shipment)";
	//}


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
		
		addToContainers(container)
			
		return container
	}



    /**
     * Get all recipients for this shipment
     *
     * @return
     */
    def getRecipients() {
        def recipients = []
        containers.each { container ->
            if (container?.recipient?.email) {
                recipients.add(container.recipient)
            }
        }
        shipmentItems.each { shipmentItem ->
            if (shipmentItem?.recipient?.email) {
                recipients.add(shipmentItem.recipient)
            }
        }
        if (recipient?.email) {
            recipients.add(recipient)
        }
        return recipients?.unique()
    }

    String getConsigneeAddress() {
        return destination.address.description?:destination?.locationGroup.address?.description
    }

    String getConsignorAddress() {
        return origin.address.description?:origin?.locationGroup.address?.description
    }

	/**
	 * Clones the specified container
	 */
	void cloneContainer(Container container, Integer quantity) {
		// def newContainer = new Container()
	}
	
	Float totalWeightInKilograms() {
		return containers.findAll { it.parentContainer == null }.collect { it.totalWeightInKilograms() }.sum()
	}

	Float totalWeightInPounds() {
		return containers.findAll { it.parentContainer == null }.collect { it.totalWeightInPounds() }.sum()
	}


	Collection findAllParentContainers() {
		return containers.findAll { !it.parentContainer }
	}

	Collection findAllChildContainers(Container container) {
		return Container.findAllByShipmentAndParentContainer(this, container)
	}

	Container findContainerByName(String name) {
		return containers.find { it.name.equalsIgnoreCase(name) }
	}

	Container addNewPallet(palletName) {
		ContainerType palletType = ContainerType.findById(Constants.PALLET_CONTAINER_TYPE_ID)
		Container pallet = addNewContainer(palletType)
		pallet.name = palletName
		return pallet;
	}


	Container findOrCreatePallet(String palletName) {
		Container pallet = findContainerByName(palletName)
		if (!pallet) {
			pallet = addNewPallet(palletName)
		}
		return pallet
	}

	ShipmentItem findShipmentItem(InventoryItem inventoryItem, Container container) {
        ShipmentItem shipmentItem = ShipmentItem.withCriteria(uniqueResult: true) {
            eq('shipment', this)
            if (container) {
                eq('container', container)
            }
            else {
                isNull('container')
            }
            eq('inventoryItem', inventoryItem)
        }
        return shipmentItem
    }


	Collection findShipmentItemsByContainer(container) {
		return ShipmentItem?.findAllByShipmentAndContainer(this, container)
	}

	Integer countShipmentItemsByContainer(container) {
		return ShipmentItem?.countByShipmentAndContainer(this, container)
	}

	Integer countShipmentItems() {
		return ShipmentItem.countByShipment(this)
	}

	TimeDuration timeToProcess() {
		return timeDuration(dateScheduled(), dateShipped())
	}
	TimeDuration timeInTransit() {
		return timeDuration(dateShipped(), dateDelivered())
	}
	TimeDuration timeInCustoms() {
		return timeDuration(dateCustomsEntry(), dateCustomsRelease())
	}
	TimeDuration timeDuration(Date startDate, Date endDate) {
		if (startDate && endDate) {
			return TimeCategory.minus(endDate, startDate)
		}
		return null
	}

	Date dateScheduled() {
		Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.SCHEDULED }
		return event?.eventDate ?: dateCreated
	}
	Date dateShipped() {
		Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.SHIPPED }
		return event?.eventDate ?: actualShippingDate
	}
	Date dateDelivered() {
		Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.RECEIVED || event?.eventType?.eventCode == EventCode.DELIVERED }
		return event?.eventDate ?: actualDeliveryDate
	}
	Date dateCustomsEntry() {
		Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.CUSTOMS_ENTRY }
		return event?.eventDate
	}
	Date dateCustomsRelease() {
		Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.CUSTOMS_RELEASE }
		return event?.eventDate
	}
}

