package org.pih.warehouse.shipping;

import java.util.List;
import java.util.Map;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventStatus;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.ListCommand;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.receiving.Receipt;

class ShipmentService {
	
	def sessionFactory;
	boolean transactional = true
	
	/**
	 * Returns the shipment referenced by the passed id parameter;
	 * if id is null, returns a new Shipment object
	 */
	
	Shipment getShipmentInstance(Long shipmentId) {
		return getShipmentInstance(shipmentId, null)
	}
	
	/**
	 * Returns the shipment referenced by the passed id parameter;
	 * if id is null, returns a new Shipment object of the specified
	 * shipment type
	 */
	Shipment getShipmentInstance(Long shipmentId, String shipmentType) {
		if (shipmentId) {
			Shipment shipment = Shipment.get(shipmentId)
			if (!shipment) {
				throw new Exception("No shipment found with shipmentId " + shipmentId)
			}
			else {
				return shipment
			}
		}
		else {
			Shipment shipment = new Shipment()
			
			if (shipmentType) {
				ShipmentType shipmentTypeObject = ShipmentType.findByNameIlike(shipmentType)
				if (!shipmentTypeObject) {
					throw new Exception(shipmentType + " is not a valid shipment type")
				}
				else {
					shipment.shipmentType = shipmentTypeObject
				}
			}		
			return shipment
		}
	}
	
	List<Shipment> getAllShipments() {
		return Shipment.list()
	}
	
	
	List<Shipment> getRecentOutgoingShipments(Long locationId) { 		
		Location location = Location.get(locationId);
		//def upcomingShipments = Shipment.findAllByOriginAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
		//	[max:5, offset:2, sort:"expectedShippingDate", order:"desc"]);
		
		def criteria = Shipment.createCriteria()
		def now = new Date()
		def upcomingShipments = criteria.list {
			and { 
				eq("origin", location)
				or {
					between("expectedShippingDate",null,null)
					//between("expectedShippingDate",now-5,now+30)
					isNull("expectedShippingDate")
				}
			}
		}
		
		def shipments = new ArrayList<Shipment>();		
		for (shipment in upcomingShipments) { 
			shipments.add(shipment);
		}
						
		/*
		def unknownShipments = Shipment.findAllByOriginAndExpectedShippingDateIsNull(location);		
		for (shipment in unknownShipments) { 
			shipments.add(shipment);
		}*/
		
		return shipments;
	}
	
	
	List<Shipment> getRecentIncomingShipments(Long locationId) { 		
		Location location = Location.get(locationId);
		//return Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
		return Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, null, null,
			[max:10, offset:2, sort:"expectedShippingDate", order:"desc"]);
	}
	
	
	Map<EventType, ListCommand> getShipmentsByStatus(List shipments) {
		//return shipments.groupBy { it.mostRecentStatus } 
		def shipmentMap = new TreeMap<EventType, ListCommand>();
		shipments.each {
			
			def eventType = it.getMostRecentStatus();			
			def key = (eventType?.eventStatus) ? eventType?.eventStatus : EventStatus.UNKNOWN;
			def shipmentList = shipmentMap[key];
			if (!shipmentList) {
				shipmentList = new ListCommand(category: key, objectList: new ArrayList());
			}
			shipmentList.objectList.add(it);
			shipmentMap.put(key, shipmentList)
		}
		//log.info("shipmentMap: " + shipmentMap)		
		return shipmentMap;
	}
	
	
	List<Shipment> getShipments() { 		
		
		return getAllShipments()
		
		/*
		return Shipment.withCriteria { 				
			eq("mostRecentEvent.eventType.id", EventType.findByName("Departed"))
		}*/

		/*		
		//def sessionFactory
		//sessionFactory = ctx.sessionFactory  // this only necessary if your are working with the Grails console/shell
		def session = sessionFactory.currentSession		
		def query = session.createSQLQuery(
			"""
			select s.* , count(*)
			from shipment s, shipment_event se, event e 
			where se.shipment_events_id = s.id 
			and se.event_id = e.id 
			group by s.id having count(*) > 1
			order by s.name
			"""
		);
		query.addEntity(org.pih.warehouse.shipping.Shipment.class); // this defines the result type of the query
		//query.setInteger("ids", 1);
		return query.list();	// return query.list()*.name;
		*/
		
		//def criteria = Shipment.createCriteria()
		//def results = criteria.list {
			//or {
			//	   for (e in branchList) {
			//		   eq("branch", b)
			//	   }
			//	}
		//}
	}
	

	
	
	
	List<Shipment> getShipmentsByLocation(Location location) {
		return Shipment.withCriteria { 
			or {	
				eq("destination", location)
				eq("origin", location)
			}
		}
	}    
	
	
	List<Shipment> getShipmentsByName(String name) {
		return Shipment.withCriteria { 
			ilike("name", "%" +name + "%")
		}
	}
	
	List<Shipment> getShipmentsByNameAndDestination(String name, Location location) {
		return Shipment.withCriteria {
			and { 
				ilike("name", "%" +name + "%")
				eq("destination", location)
			}
		}
	}

	List<Shipment> getShipmentsByNameAndOrigin(String name, Location location) {
		return Shipment.withCriteria {
			and {
				ilike("name", "%" +name + "%")
				eq("origin", location)
			}
		}
	}


	List<Shipment> getShipmentsByDestination(Location location) {
		return Shipment.withCriteria { 
			eq("destination", location) 
		}
	}
	
	List<Transaction> getShipmentsByOrigin(Location location) {
		return Shipment.withCriteria { 
			eq("origin", location);
		}
	}
	
	/**
	 * Saves a shipment
	 */
	void saveShipment(Shipment shipment) {
		if (shipment) {
			// if this is shipment has no events (i.e., it is a new shipment) set it as pending
			if (!(shipment.events?.size() > 0)) {
				def event = new Event(
					eventDate: new Date(),
					eventType: EventType.findByName("Requested"),		// FIXME Event type needs to be refactored a bit
					//eventLocation: Location.get(session.warehouse.id)
				)
				event.save(flush:true);
				shipment.addToEvents(event)
			}
			
			shipment.save(flush:true)
		}
	}
	
	/**
	 * Saves a container
	 */
	void saveContainer(Container container) {
		container.save(flush:true)
	}
	
	/**
	 * Saves an item
	 */
	void saveShipmentItem(ShipmentItem item) {
		item.save(flush:true)
	}
	
	/**
	 * Deletes a container
	 */
	void deleteContainer(Container container) {
		// first we need to handle deleting all the child containers
		for (childContainer in container.containers) {
			deleteContainer(childContainer)
		}
		
		// remove all items in the container from the parent shipment
		def shipment = container?.shipment
		for (ShipmentItem item in container.getShipmentItems()) {
			shipment.removeFromShipmentItems(item).save(flush:true)
		}	
		
		// NOTE: I'm using the standard "remove" set method here instead of the removeFrom Grails
		// code because the removeFrom code wasn't working correctly, I think because of
		// the fact that a container can be associated with both a shipment and another container
		
		// remove the container from its parent
		container?.parentContainer?.containers?.remove(container)
				
		// remove the container itself from the parent shipment
		shipment?.containers?.remove(container)
		
		shipment.save(flush:true)
	}
	
	/**
	 * Deletes a shipment item
	 */
	void deleteShipmentItem(ShipmentItem item) {
		def shipment = item.shipment
		
		log.error("so we are trying to remove item with id = " + item.id + " and quantity = " + item.quantity)
		
		shipment.removeFromShipmentItems(item)
		
	}
	
	
	/**
	 * Get a list of shipments 
	 * @param location
	 * @param eventStatus
	 * @return
	 */
	List<Shipment> getReceivingByDestinationAndStatus(Location location, EventStatus eventStatus) { 		
		def shipmentList = getRecentIncomingShipments(location?.id)
		if (shipmentList) { 
			//shipmentList = shipmentList.findAll { it.mostRecentStatus = eventStatus } 
		}
		return shipmentList;		
	}
	
	
	void sendShipment(Shipment shipmentInstance, String comment, User user, Location location) { 
		
		try { 
			if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {				
				// Add comment to shipment (as long as there's an actual comment 
				// after trimming off the extra spaces)
				if (comment) {
					shipmentInstance.addToComments(
						new Comment(comment: comment, sender: user));
				}

				// Add a Shipped event to the shipment
				EventType eventType = EventType.findByName("Shipped")
				if (eventType) {
					def event = new Event();
					event.eventDate = new Date()
					event.eventType = eventType
					event.eventLocation = location
					event.save(flush:true);
					shipmentInstance.addToEvents(event);
				}
				else {
					throw new Exception("Expected event type 'Shipped'")
				}
												
				// Save updated shipment instance
				shipmentInstance.save();
			
				// Create a new transaction for outgoing items
				Transaction debitTransaction = new Transaction();
				debitTransaction.transactionType = TransactionType.get(1); 	// transfer
				debitTransaction.source = shipmentInstance?.origin
				debitTransaction.destination = shipmentInstance?.destination;
				debitTransaction.inventory = shipmentInstance?.origin?.inventory
				debitTransaction.transactionDate = new Date();
				
				shipmentInstance.shipmentItems.each { 
					def inventoryItem = InventoryItem.findByLotNumberAndProduct(it.lotNumber, it.product)
					
					// If the inventory item doesn't exist, we create a new one
					if (!inventoryItem) {
						inventoryItem = new InventoryItem();
						inventoryItem.lotNumber = it.lotNumber
						inventoryItem.product = it.product
						if (!inventoryItem.hasErrors() && inventoryItem.save()) {
							// at this point we've saved the inventory item successfully
						}
						else {
							// 
							inventoryItem.errors.allErrors.each { error->
								def errorObj = [inventoryItem, error.getField(), error.getRejectedValue()] as Object[]
								shipmentInstance.errors.reject("inventoryItem.invalid",
									errorObj, "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
							}
							return;
						}
					}
					
					// Create a new transaction entry
					TransactionEntry transactionEntry = new TransactionEntry();
					transactionEntry.quantity = 0 - it.quantity;
					transactionEntry.lotNumber = it.lotNumber
					transactionEntry.product = it.product;
					transactionEntry.inventoryItem = inventoryItem;
					debitTransaction.addToTransactionEntries(transactionEntry);
				}
				debitTransaction.save(flush:true);
			}
		} catch (Exception e) { 
			// rollback all updates 
			log.error(e);
			shipmentInstance.errors.reject("shipmentInstance.invalid", e.message);
		}				
	} 	
	
	
	void receiveShipment(Shipment shipmentInstance, Receipt receiptInstance, String comment, User user, Location location) { 
		
		try {
			
			if (!receiptInstance.hasErrors() && receiptInstance.save(flush: true)) {
				
				// Add comment to shipment (as long as there's an actual comment
				// after trimming off the extra spaces)
				if (comment) {
					shipmentInstance.addToComments(
						new Comment(comment: comment, sender: user));
				}

				// Add a Shipped event to the shipment
				EventType eventType = EventType.findByName("Received")
				if (eventType) {
					def event = new Event();
					event.eventDate = new Date()
					event.eventType = eventType
					event.eventLocation = location
					event.save(flush:true);
					shipmentInstance.addToEvents(event);
				}
				else {
					throw new Exception("Expected event type 'Shipped'")
				}
												
				// Save updated shipment instance
				shipmentInstance.save();
			
				// Create a new transaction for outgoing items
				Transaction creditTransaction = new Transaction();
				creditTransaction.transactionType = TransactionType.get(1); 	// transfer
				creditTransaction.source = shipmentInstance?.origin
				creditTransaction.destination = shipmentInstance?.destination;
				creditTransaction.inventory = shipmentInstance?.destination?.inventory
				creditTransaction.transactionDate = new Date();
				
				shipmentInstance.shipmentItems.each {
					def inventoryItem = InventoryItem.findByLotNumberAndProduct(it.lotNumber, it.product)
					
					// If the inventory item doesn't exist, we create a new one
					if (!inventoryItem) {
						inventoryItem = new InventoryItem();
						inventoryItem.lotNumber = it.lotNumber
						inventoryItem.product = it.product
						if (!inventoryItem.hasErrors() && inventoryItem.save()) {
							// at this point we've saved the inventory item successfully
						}
						else {
							//
							inventoryItem.errors.allErrors.each { error->
								def errorObj = [inventoryItem, error.getField(), error.getRejectedValue()] as Object[]
								shipmentInstance.errors.reject("inventoryItem.invalid",
									errorObj, "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
							}
							return;
						}
					}
					
					// Create a new transaction entry
					TransactionEntry transactionEntry = new TransactionEntry();
					transactionEntry.quantity = it.quantity;
					transactionEntry.lotNumber = it.lotNumber
					transactionEntry.product = it.product;
					transactionEntry.inventoryItem = inventoryItem;
					creditTransaction.addToTransactionEntries(transactionEntry);
				}
				creditTransaction.save(flush:true);
			}
		} catch (Exception e) {
			// rollback all updates
			log.error(e);
			shipmentInstance.errors.reject("shipmentInstance.invalid", e.message);
		}
	}
		
}
