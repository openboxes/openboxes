package org.pih.warehouse.shipping;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventCode;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.ListCommand;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.donation.Donor;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionException;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.receiving.ReceiptItem;

class ShipmentService {

	def mailService;
	def sessionFactory;
	def inventoryService;
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
	
	List<Shipment> getAllShipments(String sort, String order) {
		return Shipment.list(['sort':sort, 'order':order])
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
		def shipmentMap = new TreeMap<ShipmentStatusCode, ListCommand>();
		
		ShipmentStatusCode.list().each { 
			shipmentMap[it] = [];
		}
		shipments.each {
			
			def key = it.getStatus().code;			 
			def shipmentList = shipmentMap[key];
			if (!shipmentList) {
				shipmentList = new ListCommand(category: key, objectList: new ArrayList());
			}
			shipmentList.objectList.add(it);
			shipmentMap.put(key, shipmentList)
		}	
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

	List<Shipment> getPendingShipments(Location location) { 
		def shipments = Shipment.withCriteria {
			eq("origin", location)
		}
		
		return shipments.findAll { !it.hasShipped() }
	}
	

	List<Shipment> getShipmentsByDestination(Location location) {
		return Shipment.withCriteria { 
			eq("destination", location) 
		}
	}
	
	List<Shipment> getShipmentsByOrigin(Location location) {
		return Shipment.withCriteria { 
			eq("origin", location);
		}
	}
	
	List<Shipment> getShipments(ShipmentType shipmentType, Location origin, Location destination, ShipmentStatusCode statusCode, Date statusStartDate, Date statusEndDate) {
		def shipments = Shipment.withCriteria {
			and {
				if (shipmentType) { eq("shipmentType", shipmentType) }
				if (origin) { eq("origin", origin) }
				if (destination) { eq("destination", destination) }
			}
		}
		
		// now filter by event code and eventdate
		shipments = shipments.findAll( { def status = it.getStatus()
											if (statusCode && status.code != statusCode) { return false }
											if (statusStartDate && status.date < statusStartDate) { return false }
											if (statusEndDate && status.date >= statusEndDate.plus(1)) { return false }
											return true
										} )
										
		return shipments
	}
	
	/**
	 * Saves a shipment
	 */
	void saveShipment(Shipment shipment) {
		shipment.save(flush:true)
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
		// nothing to do if null
		if (!container) { return }
		
		// first we need recursively call method to handle deleting all the child containers
		def childContainers = container.containers.collect { it }   // make a copy to avoid concurrent modification
		childContainers.each { 
			deleteContainer(it) 
		}
		
		// remove all items in the container from the parent shipment
		container.getShipmentItems().each { 
			container.shipment.removeFromShipmentItems(it).save(flush:true) 
		}
		
		// NOTE: I'm using the standard "remove" set method here instead of the removeFrom Grails
		// code because the removeFrom code wasn't working correctly, I think because of
		// the fact that a container can be associated with both a shipment and another container
		
		// remove the container from its parent
		container.parentContainer?.containers?.remove(container)
					
		// remove the container itself from the parent shipment
		container.shipment.containers.remove(container)
		
		container.shipment.save(flush:true)
	}
	
	/**
	 * Deletes a shipment item
	 */
	void deleteShipmentItem(ShipmentItem item) {
		def shipment = item.shipment
		shipment.removeFromShipmentItems(item)
	}
	
	/**
	 * Makes a specified number of copies of the passed container, including it's children containers 
	 * and shipment item, and connects them all properly to the parent shipment
	 */
	void copyContainer(Container container, Integer quantity) {
		// probably could speed the performance up on this by not going one by one
		// but this is pretty clear to understand
		quantity.times { 
			copyContainer(container) 
		}
	}
	
	/**
	 * Makes a copy of the passed container, including it's children containers and shipment items,
	 * and connects it properly to the parent shipment
	 */
	Container copyContainer(Container container)  {
		Container newContainer = copyContainerHelper(container)

		// the new container should have the same parent as the old container
		if (container.parentContainer) { container.parentContainer.addToContainers(newContainer) }		
		saveShipment(container.shipment)
	}
	
	
	private Container copyContainerHelper(Container container) {
		
		// first, make a copy of this container
		Container newContainer = container.copyContainer()
		
		// clone all the child containers and attach them to this container		
		for (Container c in container.containers) {	
			newContainer.addToContainers(copyContainerHelper(c)) 
		}
		
		// TODO: figure out sort order
		
		// now create clones of all the shipping items on this container
		for (ShipmentItem item in container.shipment.shipmentItems.findAll( {it.container == container} )) {
			def newItem = item.cloneShipmentItem()
			// set the container for the new item to this container
			newItem.container = newContainer
			// add the item to the parent shipment
			container.shipment.addToShipmentItems(newItem)
		}
		
		// add the new container to the shipment
		container.shipment.addToContainers(newContainer)
				
		return newContainer	
	}
	
	public ShipmentItem copyShipmentItem(ShipmentItem itemToCopy) {
		def shipmentItem = new ShipmentItem();
		shipmentItem.lotNumber = itemToCopy.lotNumber
		shipmentItem.expirationDate = itemToCopy.expirationDate
		shipmentItem.product = itemToCopy.product
		shipmentItem.quantity = itemToCopy.quantity
		shipmentItem.recipient = itemToCopy.recipient
		shipmentItem.container = itemToCopy.container
		shipmentItem.shipment =  itemToCopy.shipment
		shipmentItem.donor =  itemToCopy.donor
		return shipmentItem;
	} 
	
	public ShipmentItem findShipmentItem(ShipmentItem itemToFind) { 
		def shipmentItem = null;
		log.info("find shipment item by " + itemToFind.shipment + " > " + itemToFind.container + " > " + itemToFind.product + " > " + itemToFind.lotNumber )
		
		def criteria = ShipmentItem.createCriteria()
		shipmentItem = criteria.get {
			and { 
				eq("shipment", itemToFind.shipment)
				if (itemToFind.container) { 
					eq("container", itemToFind.container)
				}
				else { 
					isNull("container")
				}
				eq("product", itemToFind.product)
				if (itemToFind.lotNumber) { 
					eq("lotNumber", itemToFind.lotNumber)
				}
				else { 
					isNull("lotNumber")
				}
			}
			maxResults(1)
			
		}
		return shipmentItem;
	}
	
	
	/**
	 * Get a list of shipments 
	 * @param location
	 * @param eventCode
	 * @return
	 */
	List<Shipment> getReceivingByDestinationAndStatus(Location location, ShipmentStatusCode statusCode) { 		
		def shipmentList = getRecentIncomingShipments(location?.id)
		if (shipmentList) { 
			shipmentList = shipmentList.findAll { it.status.code == statusCode } 
		}
		return shipmentList;		
	}
	
	/**
	void sendShipment(Shipment shipmentInstance, String comment, User userInstance, Location locationInstance) {
		sendShipment(shipmentInstance, comment, userInstance, locationInstance, new Date(), null)
	}
	*/
	
	void sendShipment(Shipment shipmentInstance, String comment, User userInstance, Location locationInstance, Date shipDate, Set<Person> emailRecipients) { 

		try { 
			if (!shipDate || shipDate > new Date()) {
				shipmentInstance.errors.reject("shipment.invalid.invalidShipDate", "Shipping date [" + shipDate + "] must occur on or before today.") 
				throw new ShipmentException(message: "Shipping date [" + shipDate + "] must occur on or before today.", shipment: shipmentInstance)
			}				
			if (shipmentInstance.hasShipped()) { 
				shipmentInstance.errors.reject("shipment.invalid.alreadyShipped", "Shipment has alerady shipped")
				throw new ShipmentException(message: "Shipment has already been shipped.", shipment: shipmentInstance);
			}
			// don't allow the shipment to go out if it has errors, or if this shipment has already been shipped, or if the shipdate is after today
			if (!shipmentInstance.hasErrors()) {				
				// Add comment to shipment (as long as there's an actual comment 
				// after trimming off the extra spaces)
				if (comment) {
					shipmentInstance.addToComments(new Comment(comment: comment, sender: userInstance))
				}
					
				// Add a Shipped event to the shipment									
				createShipmentEvent(shipmentInstance, shipDate, EventCode.SHIPPED, locationInstance);
																
				// Save updated shipment instance (adding an event and comment)
				if (!shipmentInstance.hasErrors() && shipmentInstance.save()) { 
					
					// only need to create a transaction if the source is a Warehouse
					if (shipmentInstance.origin?.isWarehouse()) {
						inventoryService.createSendShipmentTransaction(shipmentInstance)
					}
					triggerSendShipmentEmails(shipmentInstance, userInstance, emailRecipients)
				}
				else { 
					throw new ShipmentException(message: "Failed to send shipment due to errors ", shipment: shipmentInstance)
				}
			}
			
			// Shipment has errors or it has already shipped or ship date is 
			else {
				log.error("Failed to send shipment due to errors")
				// TODO: make this a better error message
				throw new ShipmentException(message: "Failed to send shipment ", shipment: shipmentInstance)
			}
		} catch (Exception e) { 
			// rollback all updates 
			log.error(e);
			throw e
			//shipmentInstance.errors.reject("shipment.invalid", e.message);  // this didn't seem to be working properly
		}				
	} 	
	
	void createShipmentEvent(Shipment shipmentInstance, Date eventDate, EventCode eventCode, Location location) { 
		boolean eventAlreadyExists = Boolean.FALSE;
		
		// Get the appropriate event type for the given event code
		EventType eventType = EventType.findByEventCode(eventCode)
		if (!eventType) {
			throw new RuntimeException(message: "System could not find event type for event code '" + eventCode + "'")
		}
		
		// If 'requested' event type already exists, return
		log.info("exists " + eventAlreadyExists)
		shipmentInstance?.events.each {
			if (it.eventType == eventType)
				eventAlreadyExists = Boolean.TRUE;
		}
		// Avoid duplicate events
		if (!eventAlreadyExists) {
			log.info ("Event does not exist")
			
			// enforce that we are only storing the date component here
			eventDate.clearTime()
			
			def eventInstance = new Event(eventDate: eventDate, eventType: eventType, eventLocation: location);
			if (!eventInstance.hasErrors() && eventInstance.save()) { 
				shipmentInstance.addToEvents(eventInstance);
			}
			else { 
				shipementInstance.errors.reject("shipment.shipmentEvents.invalid");
			}
		}

	}
	
	void triggerSendShipmentEmails(Shipment shipmentInstance, User userInstance, Set<Person> recipients) { 
	
		if (!shipmentInstance.hasErrors() && recipients) {

			// add the current user to the list of email recipients
			recipients = recipients + userInstance
			
			log.info("Mailing shipment emails to ${recipients.name}")
			
			// TODO: change this to create an email from a standard template (ie, an email packing list?)
			def subject = "The ${shipmentInstance.shipmentType?.name} shipment ${shipmentInstance.name} has been shipped"
			def message = "The ${shipmentInstance.shipmentType?.name} shipment ${shipmentInstance.name} has been shipped"
			
			mailService.sendMail(subject, message, recipients.email)
		}
	}
	
	
	
	
	
	void receiveShipment(Shipment shipmentInstance, String comment, User user, Location location) { 
		
		//try {
			
			if (!shipmentInstance.hasShipped()) { 
				throw new ShipmentException(message: "Shipment has not been shipped yet.", shipment: shipmentInstance)
			}
			
			if (shipmentInstance.wasReceived()) {
				throw new ShipmentException(message: "Shipment has already been received.", shipment: shipmentInstance)
			}
			
			if (shipmentInstance.receipt.getActualDeliveryDate() > new Date()) { 
				throw new ReceiptException(
					message: "Delivery date [" + shipmentInstance.receipt.getActualDeliveryDate() + "] must occur on or before today.", 
					shipment: shipmentInstance,
					receipt: shipmentInstance.receipt)
			}
			
			
			if (!shipmentInstance.receipt.hasErrors() && shipmentInstance.receipt.save(flush: true)) {
				
				// Add comment to shipment (as long as there's an actual comment
				// after trimming off the extra spaces)
				if (comment) {
					shipmentInstance.addToComments(new Comment(comment: comment, sender: user));
				}

				// Add a Received event to the shipment
				createShipmentEvent(shipmentInstance, shipmentInstance.receipt.actualDeliveryDate, EventCode.RECEIVED, location);
												
				// Save updated shipment instance
				shipmentInstance.save(flush:true);
			
				// only need to create a transaction if the destination is a warehouse
				if (shipmentInstance.destination?.isWarehouse()) {
				
					// Create a new transaction for incoming items
					Transaction creditTransaction = new Transaction()
					creditTransaction.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
					creditTransaction.source = shipmentInstance?.origin
					creditTransaction.destination = null
					creditTransaction.inventory = shipmentInstance?.destination?.inventory ?: inventoryService.addInventory(shipmentInstance.destination)
					creditTransaction.transactionDate = shipmentInstance.getActualDeliveryDate()
					
					shipmentInstance.receipt.receiptItems.each {
						def inventoryItem = InventoryItem.findByLotNumberAndProduct(it.lotNumber, it.product)
						
						// If the inventory item doesn't exist, we create a new one
						if (!inventoryItem) {
							inventoryItem = new InventoryItem();
							inventoryItem.lotNumber = it.lotNumber
							inventoryItem.expirationDate = it.expirationDate;
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
								throw new ShipmentException("Failed to receive shipment while saving inventory item ",
									shipment: shipmentInstance)
							}
						}
						
						// Create a new transaction entry
						TransactionEntry transactionEntry = new TransactionEntry();
						transactionEntry.quantity = it.quantityReceived;
						transactionEntry.inventoryItem = inventoryItem;
						creditTransaction.addToTransactionEntries(transactionEntry);
					}
					
					// TODO: had to comment out these flash.message because they were throwing a no-such
					// property exception; can you use "flash" within a service method?
					if (!creditTransaction.hasErrors() && creditTransaction.save(flush:true)) { 
						// saved successfully
						//flash.message = "Transaction was created successfully"
					}
					else { 
						// did not save successfully, display errors message
						//flash.message = "Transaction has errors"
						throw new TransactionException("Failed to receive shipment due to error while saving transaction", 
							transaction: creditTransaction)
					}
				}
			}
			else {
				log.error (shipmentInstance.receipt.errors)
				// TODO: make this a better error message
				throw new ReceiptException(message: "Failed to receive shipment due to error while saving receipt", 
					receipt: shipmentInstance.receipt)
			}
		//} catch (Exception e) {
			// rollback all updates and throw an exception
		//	log.error("Caught exception ", e);
		//	throw new RuntimeException("Failed to receive shipment due to unknown error", e);
			//shipmentInstance.errors.reject("shipmentInstance.invalid", e.message);  // this didn't seem to be working properly
		//}
	}
		
	public Receipt createReceipt(Shipment shipmentInstance, Date dateDelivered) { 
		Receipt receiptInstance = new Receipt()
		shipmentInstance.receipt = receiptInstance
		receiptInstance.shipment = shipmentInstance		
		receiptInstance.recipient = shipmentInstance?.recipient
		receiptInstance.expectedDeliveryDate = shipmentInstance?.expectedDeliveryDate;
		receiptInstance.actualDeliveryDate = dateDelivered;
		shipmentInstance.shipmentItems.each {
			ReceiptItem receiptItem = new ReceiptItem(it.properties);
			receiptItem.setQuantityShipped (it.quantity);
			receiptItem.setQuantityReceived (it.quantity);
			receiptItem.setLotNumber(it.lotNumber);
			receiptItem.setExpirationDate(it.expirationDate);
			receiptInstance.addToReceiptItems(receiptItem);
		}
		return receiptInstance;
	}
	
	
	
	/**
	 * Fetches the shipment workflow associated with this shipment
	 * (Note that, as of now, there can only be one shipment workflow per shipment type)
	 */
	ShipmentWorkflow getShipmentWorkflow(Shipment shipment) {
		if (!shipment?.shipmentType) { return null }
		return ShipmentWorkflow.findByShipmentType(shipment.shipmentType)
	}
	
}
