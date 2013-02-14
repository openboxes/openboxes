/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping;

import grails.validation.ValidationException
import org.hibernate.exception.ConstraintViolationException;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventCode;
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.ListCommand
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionException;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.receiving.ReceiptItem;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

class ShipmentService {

	def mailService;
	def sessionFactory;
	def productService
	def inventoryService;
	def identifierService
	boolean transactional = true
   
	
	/**
	 * Returns the shipment referenced by the passed id parameter;
	 * if id is null, returns a new Shipment object
	 * 
	 * @param shipmentId
	 * @return
	 */
	Shipment getShipmentInstance(String shipmentId) {
		return getShipmentInstance(shipmentId, null)
	}
	
	/**
	 * Returns the shipment referenced by the passed id parameter;
	 * if id is null, returns a new Shipment object of the specified
	 * shipment type
	 * 
	 * @param shipmentId
	 * @param shipmentType
	 * @return
	 */
	Shipment getShipmentInstance(String shipmentId, String shipmentType) {
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
	
	
	/**
	 * @param sort
	 * @param order
	 * @return	all shipments sorted by the given sort column and ordering
	 */
	List<Shipment> getAllShipments(String sort, String order) {
		return Shipment.list(['sort':sort, 'order':order])
	}
	
	
	/**
	 * @return all shipments 
	 */
	List<Shipment> getAllShipments() {
		return Shipment.list()
	}

	
	/**
	 * 	
	 * @return
	 */
	Object getProductMap() { 
		
		def criteria = ShipmentItem.createCriteria();		
		def quantityMap = criteria.list {
			projections {
				sum('quantity')
			}
			groupProperty "product"
		}
		return quantityMap
		
	}
	
	
	/**
	 * 
	 * @param locationId
	 * @return
	 */
	List<Shipment> getRecentOutgoingShipments(String locationId) { 		
		Location location = Location.get(locationId);
		//def upcomingShipments = Shipment.findAllByOriginAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
		//	[max:5, offset:2, sort:"expectedShippingDate", order:"desc"]);
		
		def criteria = Shipment.createCriteria()
		def now = new Date()
		def upcomingShipments = criteria.list {
			and { 
				eq("origin", location)
				or {
					//between("expectedShippingDate",null,null)
					between("expectedShippingDate",now-5,now+30)
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
	
	/**
	 * 
	 * @param locationId
	 * @return
	 */
	List<Shipment> getRecentIncomingShipments(String locationId) { 		
		Location location = Location.get(locationId);
		//return Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
		return Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30,
			[max:10, offset:2, sort:"expectedShippingDate", order:"desc"]);
	}
	

	/**
	 * 	
	 * @param shipments
	 * @return
	 */
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
	
	/**
	 * 
	 * @return
	 */
	List<Shipment> getShipments() { 		
		
		return getAllShipments()
		
		/*
		return Shipment.withCriteria { 				
			eq("mostRecentEvent.eventType.id", EventType.findByName("Departed"))  // change this to reference by id if we reimplement this
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
	

	
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByLocation(Location location) {
		return Shipment.withCriteria { 
			or {	
				eq("destination", location)
				eq("origin", location)
			}
		}
	}    
	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	List<Shipment> getShipmentsByName(String name) {
		return Shipment.withCriteria { 
			ilike("name", "%" +name + "%")
		}
	}
	
	/**
	 * 
	 * @param name
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByNameAndDestination(String name, Location location) {
		return Shipment.withCriteria {
			and { 
				ilike("name", "%" +name + "%")
				eq("destination", location)
			}
		}
	}

	
	/**
	 * 
	 * @param name
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByNameAndOrigin(String name, Location location) {
		return Shipment.withCriteria {
			and {
				ilike("name", "%" +name + "%")
				eq("origin", location)
			}
		}
	}

	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getPendingShipments(Location location) { 
		def shipments = Shipment.withCriteria {
			eq("origin", location)
		}
		
		return shipments.findAll { !it.hasShipped() }
	}
	
	/**
	 * 
	 * @param location
	 * @param product
	 * @return
	 */
	List<ShipmentItem> getPendingShipmentItemsWithProduct(Location location, Product product) {
		def shipmentItems = []
		def shipments = getPendingShipments(location);		
		
		shipments.each { 
			def shipmentItemList = it.shipmentItems.findAll { it.product == product }
			shipmentItemList.each { 
				shipmentItems << it;
			}
		}
	
		return shipmentItems;		
	}
	
	/**
	 * Get all shipments that are shipping to the given location.
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getIncomingShipments(Location location) {
		return Shipment.withCriteria { eq("destination", location) }.findAll { it.isPending() }		
	}
	
	
	/**
	 * Get all shipments that are shipping from the given location.
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getOutgoingShipments(Location location) {
		return Shipment.withCriteria { eq("origin", location) }.findAll { it.isPending() } 		
	}

	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByDestination(Location location) {
		return Shipment.withCriteria { 
			eq("destination", location) 
		}
	}
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByOrigin(Location location) {
		return Shipment.withCriteria { 
			eq("origin", location);
		}
	}
	
	
	/**
	 * 
	 * @param shipmentType
	 * @param origin
	 * @param destination
	 * @param statusCode
	 * @param statusStartDate
	 * @param statusEndDate
	 * @return
	 */
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
											//if (statusStartDate && status.date < statusStartDate) { return false }
											//if (statusEndDate && status.date >= statusEndDate.plus(1)) { return false }
											return true
										} )		
		
		shipments = shipments.findAll() { (!statusStartDate || it.expectedShippingDate >= statusStartDate) && (!statusEndDate || it.expectedShippingDate <= statusEndDate) }
			
		return shipments
	}
	
	/**
	 * Saves a shipment
	 * 
	 * @param shipment
	 */
	void saveShipment(Shipment shipment) {
		if (!shipment.shipmentNumber) { 
			shipment.shipmentNumber = identifierService.generateShipmentIdentifier()
		}
		shipment.save(flush:true, failOnError:true)
	}
	
	/**
	 * Saves a container
	 * 
	 * @param container
	 */
	void saveContainer(Container container) {	
		if (!container.recipient) { 			
			container.recipient = (container?.parentContainer?.recipient)?:container.shipment.recipient;
		}
		container.save()
	}
	
	/**
	 * Move a container and all of its children to the new shipment.
	 * 
	 * @param oldContainer
	 * @param newShipment
	 */
	void moveContainers(Container oldContainer, Shipment newShipment) { 		
		//if (oldContainer.containers) { 			
			//oldContainer.containers.each { 
			//	moveContainer(it, newShipment)
			//}
		//	throw new UnsupportedOperationException();
		//}
		moveContainer(oldContainer, newShipment)		
	}
	
	/**
	 * Move a container from one shipment to the given shipment.
	 * 
	 * @param container
	 * @param shipment
	 */
	/*
	void moveContainer(Container oldContainer, Shipment newShipment) { 
		
		// Copy the container and add to the new shipment
		def newContainer = oldContainer.copyContainer();		
		newShipment.addToContainers(newContainer)
		
		// Copy each shipment item from one shipment to the other
		def oldShipment = oldContainer.shipment;
		oldContainer.shipmentItems.each {
			def shipmentItem = new ShipmentItem();
			shipmentItem.container = newContainer
			shipmentItem.lotNumber = it.lotNumber
			shipmentItem.expirationDate = it.expirationDate
			shipmentItem.quantity = it.quantity
			shipmentItem.product = it.product
			shipmentItem.recipient = it.recipient
			newShipment.addToShipmentItems(shipmentItem);
			oldShipment.removeFromShipmentItems(it)
			it.delete();
		}		
		newShipment.save(failOnError:true)
		
		// Remove old container from shipment
		oldShipment.removeFromContainers(oldContainer);
		oldContainer.delete();
	}
	*/
	
	
	/**
	 * @param container the container to be moved
	 * @param shipmentTo the shipment to which the container will be moved
	 */
	void moveContainer(Container container, Shipment shipmentTo) { 		
		
		if (container.containers) { 
			throw new ValidationException("Cannot move a container with child containers", container.errors)
		}
				
		def shipmentFrom = container.shipment
		shipmentFrom.removeFromContainers(container)
		shipmentTo.addToContainers(container)

		def shipmentItems = ShipmentItem.findAllByContainer(container)
		println "Shipment items " + shipmentItems?.size()
		shipmentItems.each { shipmentItem ->
			shipmentFrom.removeFromShipmentItems(shipmentItem)
			shipmentTo.addToShipmentItems(shipmentItem)
		}
		

		/*
		def previousShipment = container.shipment;
		shipment.addToContainers(container)
		//container.shipment = shipment
		container.containers.each { child ->
			//child.shipment = shipment
			container.addToContainers(child)
		}
		shipment.save()
		previousShipment.save()
		//container.refresh()
		//previousShipment.refresh()
		 */
	}
	
	/*
	void moveContainer(Container container, Shipment shipment) { 
		// Get a copy of the container to be moved		
		def newContainer = container.copyContainer()
		
		// Move all children containers
		container.containers.each { childContainer ->
			newContainer.addToContainers(childContainer)
			childContainer.shipment = shipment
			childContainer.parentContainer.shipment = shipment
		}

		
		// 
		//newContainer.containers.each { childContainer ->
		//}
		
		// Remove the container from the existing shipment
		def shipmentOld = container.shipment
		shipmentOld.removeFromContainers(container)
		saveShipment(shipmentOld)
		
		// Add the cloned container to the new shipment
		shipment.addToContainers(newContainer)
		
		saveShipment(shipment)
	}
	*/
	
	/*
	void moveContainer(Container container, Shipment newShipment) { 
		
		def oldShipment = container.shipment
		
		
		// Move all shipment items in the container
		def shipmentItems = oldShipment.shipmentItems.findAll { it.container == container }
		shipmentItems.each { item ->
			newShipment.addToShipmentItems(item);
		}

		// Move all subcontainers and shipment items in the subcontainer
		container?.containers?.each { box -> 
			newShipment.addToContainers(box);
			shipmentItems = oldShipment.shipmentItems.findAll { it.container == box }
			shipmentItems.each { item -> 
				newShipment.addToShipmentItems(item);
			}
		}

		newShipment.addToContainers(container);
		container.shipment = newShipment
		saveShipment(newShipment)
		
		oldShipment.removeFromContainers(container)
		saveShipment(oldShipment)
		//container.shipment = newShipment
		//container.save(true)
		//container.shipment = newShipment;		
		//newShipment.addToContainers(container);
		//saveShipment(newShipment)
		//oldShipment.removeFromContainers(container);
		//saveShipment(oldShipment)		
	}
	*/
	
	/**
	 * Saves an item
	 * 
	 * @param item
	 */
	void saveShipmentItem(ShipmentItem shipmentItem) {
		/*
		if (!item.recipient) { 
			item.recipient = (item?.container?.recipient)?:(item?.shipment?.recipient);
		}*/
		shipmentItem.save()
	}
	
	
	
	/**
	 * Saves an item
	 * 
	 * @param shipmentItem
	 * @param shipment
	 */
	void addToShipmentItems(ShipmentItem shipmentItem, Shipment shipment) {
		// Need to set the shipment here for validation purposes
		shipmentItem.shipment = shipment;
		if (validateShipmentItem(shipmentItem)) { 
			shipment.addToShipmentItems(shipmentItem);
			shipment.save()			
		}
	}

	
	/**
	 * Validate the shipment item 	
	 * 
	 * @param shipmentItem
	 * @return
	 */
	boolean validateShipmentItem(ShipmentItem shipmentItem) { 
		def location = Location.get(shipmentItem?.shipment?.origin?.id);
		log.info("Validating shipment item at " + location?.name + " for product " + shipmentItem.product + " and lot number " + shipmentItem.lotNumber)
		def onHandQuantity = inventoryService.getQuantity(location, shipmentItem.product, shipmentItem.lotNumber)
		log.info("Checking shipment item quantity [" + shipmentItem.quantity + "] vs onhand quantity [" + onHandQuantity + "]");
		if (!shipmentItem.validate()) { 
			throw new ShipmentItemException(message: "shipmentItem.invalid", shipmentItem: shipmentItem)
		}
		
		if (shipmentItem.quantity > onHandQuantity) { 
			throw new ShipmentItemException(message: "shipmentItem.quantity.cannotExceedOnHandQuantity", shipmentItem: shipmentItem)
		}
		return true;
	}
	
	
	/**
	 * Deletes a shipment and all of its related objects
	 * 
	 * @param shipment
	 */
	void deleteShipment(Shipment shipment) { 
		shipment.delete()
	}
	
	/**
	 * Deletes a container, but leaves all shipment items 
	 * 
	 * @param container
	 */
	void deleteContainer(Container container) {

		// nothing to do if null
		if (!container) { return }
		
		def shipment = container.shipment
		
		// first we need recursively call method to handle deleting all the child containers
		def childContainers = container.containers.collect { it }   // make a copy to avoid concurrent modification
		childContainers.each { 
			deleteContainer(it) 
		}
		
		// remove all items in the container from the parent shipment
		container.getShipmentItems().each { shipmentItem ->
			//shipment.removeFromShipmentItems(it)
			shipmentItem.container = null
		}
		
		// NOTE: I'm using the standard "remove" set method here instead of the removeFrom Grails
		// code because the removeFrom code wasn't working correctly, I think because of
		// the fact that a container can be associated with both a shipment and another container
		
		// remove the container from its parent
		//container.parentContainer?.removeFromContainers(container)
					
		// remove the container itself from the parent shipment
		shipment.removeFromContainers(container)
		
		//container.shipment.save()
		container.delete()
	}
	
	
	/**
	 * Deletes a shipment item
	 * 
	 * @param item
	 */
	void deleteShipmentItem(ShipmentItem shipmentItem) {		
		def shipment = Shipment.get(shipmentItem.shipment.id)
		shipment.removeFromShipmentItems(shipmentItem)
		shipmentItem.delete(flush:true)				
	}
	
	/**
	 * Makes a specified number of copies of the passed container, including it's children containers 
	 * and shipment item, and connects them all properly to the parent shipment
	 * 
	 * @param container
	 * @param quantity
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
	 * 
	 * @param container
	 * @return
	 */
	Container copyContainer(Container container)  {
		Container newContainer = copyContainerHelper(container)

		// the new container should have the same parent as the old container
		if (container.parentContainer) { container.parentContainer.addToContainers(newContainer) }		
		saveShipment(container.shipment)
	}
	
	
	/**
	 * 
	 * @param container
	 * @return
	 */
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
        shipmentItem.inventoryItem = itemToCopy.inventoryItem
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

	
	ShipmentItem findShipmentItem(Shipment shipment, Container container, InventoryItem inventoryItem) { 
		return shipment.shipmentItems.find { it.container == container && it.inventoryItem == inventoryItem } 
	}
	
    ShipmentItem findShipmentItem(Shipment shipment,
                                  Container container,
                                  Product product,
                                  String lotNumber) {
        return shipment.shipmentItems.find { it.container == container &&
                it.product == product &&
                it.lotNumber == lotNumber }
    }

    ShipmentItem findShipmentItem(Shipment shipment,
                                  Container container,
                                  Product product,
                                  String lotNumber,
                                  InventoryItem inventoryItem) {
        return shipment.shipmentItems.find { it.container == container &&
                it.product == product &&
                it.lotNumber == lotNumber &&
                it.inventoryItem == inventoryItem }
    }

    /**
	 * Get a list of shipments.
	 * 
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
	 * 
	 * @param shipmentInstance
	 * @param comment
	 * @param userInstance
	 * @param locationInstance
	 * @param shipDate
	 * @param emailRecipients
	 */
	void sendShipment(Shipment shipmentInstance, String comment, User userInstance, Location locationInstance, Date shipDate) { 
		sendShipment(shipmentInstance, comment, userInstance, locationInstance, shipDate, true);
	}
	
	/**
	 * 
	 * @param shipmentInstance
	 * @param comment
	 * @param userInstance
	 * @param locationInstance
	 * @param shipDate
	 * @param emailRecipients
	 * @param debitStockOnSend
	 */
	void sendShipment(Shipment shipmentInstance, String comment, User userInstance, Location locationInstance, Date shipDate, Boolean debitStockOnSend) { 
		log.info "Send shipment ${shipmentInstance?.name}"
		try { 
			if (!shipDate || shipDate > new Date()) {
				shipmentInstance.errors.reject("shipment.invalid.invalidShipDate", "Shipping date [" + shipDate + "] must occur on or before today.") 
				throw new ShipmentException(message: "Shipping date [" + shipDate + "] must occur on or before today.", shipment: shipmentInstance)
			}				
			if (shipmentInstance.hasShipped()) { 
				shipmentInstance.errors.reject("shipment.invalid.alreadyShipped", "Shipment has already shipped")
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
					
					// TODO only need to create a transaction if the source is a depot - (we need to think about this)
					if (shipmentInstance.origin?.isWarehouse() && debitStockOnSend) {
						inventoryService.createSendShipmentTransaction(shipmentInstance)
					}
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
	
	
	/**
	 * 
	 * @param shipmentInstance
	 * @param eventDate
	 * @param eventCode
	 * @param location
	 */
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
	
	

	
	
	void markAsReceived(Shipment shipment, Location location) { 
		try { 
			// Add a Received event to the shipment
			createShipmentEvent(shipment, new Date(), EventCode.RECEIVED, location);
											
			// Save updated shipment instance
			shipment.save();
			
		} catch (Exception e) { 
			throw new ShipmentException();
		}
	}
	
	
	/**
	 * 
	 * @param command
	 */
	void receiveShipment(command) { 
		if (!command.validate()) {
			throw new ValidationException("Receive shipment is not valid", command.errors)
		}
	}
		
	
	/**
	 * 
	 * @param shipmentInstance
	 * @param comment
	 * @param user
	 * @param location
	 */
	void receiveShipment(Shipment shipmentInstance, String comment, User user, Location location, Boolean creditStockOnReceipt) { 
		
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
			
			
			if (!shipmentInstance.receipt.hasErrors() && shipmentInstance.receipt.save()) {
				
				// Add comment to shipment (as long as there's an actual comment
				// after trimming off the extra spaces)
				if (comment) {
					shipmentInstance.addToComments(new Comment(comment: comment, sender: user));
				}

				// Add a Received event to the shipment
				createShipmentEvent(shipmentInstance, shipmentInstance.receipt.actualDeliveryDate, EventCode.RECEIVED, location);
												
				// Save updated shipment instance
				shipmentInstance.save();
			
				// only need to create a transaction if the destination is a warehouse
				if (shipmentInstance.destination?.isWarehouse() && creditStockOnReceipt) {
				
					// Create a new transaction for incoming items
					Transaction creditTransaction = new Transaction()
					creditTransaction.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
					creditTransaction.source = shipmentInstance?.origin
					creditTransaction.destination = null
					creditTransaction.inventory = shipmentInstance?.destination?.inventory ?: inventoryService.addInventory(shipmentInstance.destination)
					creditTransaction.transactionDate = shipmentInstance.getActualDeliveryDate()
					
					// 
					shipmentInstance.receipt.receiptItems.each {
						def inventoryItem = 
							inventoryService.findOrCreateInventoryItem(it.product, it.lotNumber, it.expirationDate)							
						
						if (inventoryItem.hasErrors()) { 							
							inventoryItem.errors.allErrors.each { error->
								def errorObj = [inventoryItem, error.getField(), error.getRejectedValue()] as Object[]
								shipmentInstance.errors.reject("inventoryItem.invalid",
									errorObj, "[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
							}
							throw new ShipmentException("Failed to receive shipment while saving inventory item ",
								shipment: shipmentInstance)
						}
						
						// Create a new transaction entry
						TransactionEntry transactionEntry = new TransactionEntry();
						transactionEntry.quantity = it.quantityReceived;
						transactionEntry.inventoryItem = inventoryItem;
						creditTransaction.addToTransactionEntries(transactionEntry);
						//creditTransaction.incomingShipment = shipmentInstance
					}
					
					// TODO: had to comment out these flash.message because they were throwing a no-such
					// property exception; can you use "flash" within a service method?
					if (!creditTransaction.hasErrors() && creditTransaction.save()) { 
						// saved successfully
						//flash.message = "Transaction was created successfully"
					}
					else { 
						// did not save successfully, display errors message
						//flash.message = "Transaction has errors"
						throw new TransactionException("Failed to receive shipment due to error while saving transaction", 
							transaction: creditTransaction)
					}

					// Associate the incoming transaction with the shipment					
					shipmentInstance.addToIncomingTransactions(creditTransaction) 
					shipmentInstance.save();
					
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

	
	/**
	 * 
	 * @param shipmentInstance
	 * @param dateDelivered
	 * @return
	 */
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
	 * Fetches shipment workflow associated with a shipment of the 
	 * given shipmentId.
	 * 
	 * @param shipmentId
	 * @return
	 */
	ShipmentWorkflow getShipmentWorkflow(String shipmentId) {
		def shipment = Shipment.get(shipmentId)		
		if (!shipment?.shipmentType) { return null }
		return ShipmentWorkflow.findByShipmentType(shipment.shipmentType)
	}

	
	/**
	 * Fetches the shipment workflow associated with this shipment
	 * (Note that, as of now, there can only be one shipment workflow per shipment type)
	 * 
	 * @param shipment
	 * @return
	 */
	ShipmentWorkflow getShipmentWorkflow(Shipment shipment) {
		if (!shipment?.shipmentType) { return null }
		return ShipmentWorkflow.findByShipmentType(shipment.shipmentType)
	}
	
	
	/**
	 * 
	 * @param productIds
	 * @param location
	 * @return
	 */
	ItemListCommand getAddToShipmentCommand(List<String> productIds, Location location) { 
		// Find all inventory items that match the selected products
		def products = []
		def inventoryItems = []
		if (productIds) {
			products = Product.findAll("from Product as p where p.id in (:ids)", [ids:productIds])
			inventoryItems = InventoryItem.findAll("from InventoryItem as i where i.product.id in (:ids)", [ids:productIds])
		}

		// Get quantities for all inventory items
		def quantityOnHandMap = inventoryService.getQuantityForInventory(location.inventory)
		def quantityShippingMap = getQuantityForShipping(location)
		def quantityReceivingMap = getQuantityForReceiving(location)
		
				
		// Create command objects for each item
		def commandInstance = new ItemListCommand();
		if (inventoryItems) {
			inventoryItems.each { inventoryItem ->
				def item = new ItemCommand();
				item.quantityOnHand = quantityOnHandMap[inventoryItem]
				item.quantityShipping = quantityShippingMap[inventoryItem]
				item.quantityReceiving = quantityReceivingMap[inventoryItem]
				item.inventoryItem = inventoryItem
				item.product = inventoryItem?.product
				item.lotNumber = inventoryItem?.lotNumber
				commandInstance.items << item;
			}
		}
		
		return commandInstance
	}
	
   

	
	Boolean addToShipment(ItemListCommand command) { 	
			
		def atLeastOneUpdate = false;
		
		command.items.each {
			// Check if shipment item already exists
			def shipmentItem = findShipmentItem(it.shipment, it.container, it.inventoryItem)			

			// Only add a shipment item for rows that have a quantity greater than 0
			if (it.quantity > 0) {
				
				if (!it.shipment) { 
					command.errors.reject("shipmentItem.shipment.required")
					throw new ValidationException("Shipment is required", command.errors);
				}
				
				// If the shipment item already exists, we just add to the quantity 
				if (shipmentItem) {
					log.info "Found existing shipment item ..." + shipmentItem.id					
					shipmentItem.quantity += it.quantity;
					try { 
						validateShipmentItem(shipmentItem); 
					} catch (ShipmentItemException e) {
						log.info("Validation exception " + e.message);
						throw new ValidationException(e.message, e.shipmentItem.errors);
					}
				}
				else {
					log.info("Creating new shipment item ...");
					def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
					shipmentItem = new ShipmentItem(shipment: it.shipment, container: it.container, 
						inventoryItem: it.inventoryItem, product: it.product, lotNumber: it.lotNumber, quantity: it.quantity);					
					addToShipmentItems(shipmentItem, it.shipment);
				}
				atLeastOneUpdate = true;
			}
			log.info "Adding item with lotNumber=" + it?.lotNumber + " product=" + it?.product?.name + " and  qty=" + it?.quantity +
			" to shipment=" + it.shipment + " into container=" + it.container
		

		}
		return atLeastOneUpdate
	}	
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	Map getQuantityForShipping(Location location) { 
		return getQuantityByInventoryItem(getPendingShipments(location));
		
	}
	

	/**
	 * 
	 * @param location
	 * @return
	 */
	Map getQuantityForReceiving(Location location) { 
		return getQuantityByInventoryItem(getIncomingShipments(location));
	}
	
	/**
	 * 
	 * @param shipments
	 * @return
	 */
	Map getQuantityByInventoryItem(List<Shipment> shipments) { 		
		def quantityMap = [:]
		shipments.each { shipment ->
			shipment.shipmentItems.each { shipmentItem ->
				def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(shipmentItem.product, shipmentItem.lotNumber)
				if (inventoryItem) {
					def quantity = quantityMap[inventoryItem];
					if (!quantity) quantity = 0;
					quantity += shipmentItem.quantity;
					quantityMap[inventoryItem] = quantity
				}
			}
		}
		return quantityMap;

	}
	
   /**
	*
	* @param location
	* @return
	*/
   Map getIncomingQuantityByProduct(Location location, List<Product> products) {
	   return getQuantityByProduct(getIncomingShipments(location), products)
   }
   
   /**
   *
   * @param location
   * @return
   */
  Map getOutgoingQuantityByProduct(Location location, List<Product> products) {
	  return getQuantityByProduct(getOutgoingShipments(location), products)
  }
  
  /**
   * 
   * @param shipments
   * @return
   */
   Map getQuantityByProduct(List<Shipment> shipments, List<Product> products) {
	   def quantityMap = [:]	   
	   shipments.each { shipment ->
		   shipment.shipmentItems.each { shipmentItem ->
               def product = shipmentItem.product
               if (product) {
                   if (products.contains(product)) {
                       def quantity = quantityMap[product];
                       if (!quantity) quantity = 0;
                       quantity += shipmentItem.quantity;
                       quantityMap[product] = quantity
                   }
               }
		   }
	   }
	   return quantityMap;
   }
   
   
   
	public void rollbackLastEvent(Shipment shipmentInstance) { 
		
		def eventInstance = shipmentInstance.mostRecentEvent
		
		if (!eventInstance) {
			throw new RuntimeException("Cannot rollback shipment status because there are no recent events")
		}

		try {
			
			if (eventInstance?.eventType?.eventCode == EventCode.RECEIVED) {
				if (shipmentInstance?.receipt) {
					shipmentInstance?.receipt.delete()
					shipmentInstance?.receipt = null
				}
				
				def transactions = Transaction.findAllByIncomingShipment(shipmentInstance)
				transactions.each { transactionInstance ->
					if (transactionInstance) { 
						shipmentInstance.removeFromIncomingTransactions(transactionInstance)
						transactionInstance?.delete();					
					}
				}
								
				shipmentInstance.removeFromEvents(eventInstance)
				eventInstance?.delete()
				shipmentInstance?.save()				
			}
			else if (eventInstance?.eventType?.eventCode == EventCode.SHIPPED) { 
				def transactions = Transaction.findAllByOutgoingShipment(shipmentInstance)
				transactions.each { transactionInstance -> 
					if (transactionInstance) {
						shipmentInstance.removeFromOutgoingTransactions(transactionInstance)
						transactionInstance?.delete();
					}
				}
				
				shipmentInstance.removeFromEvents(eventInstance)
				eventInstance.delete()
				shipmentInstance.save()				
			}
			
			
		} catch (Exception e) {
			log.error("Error rolling back most recent event", e)
			throw new RuntimeException("Error rolling back most recent event")
		}
	}

    boolean moveItem(ShipmentItem itemToMove, Map<String, Integer> containerIdToQuantityMap) {
        def totalQuantity = 0;
        containerIdToQuantityMap.each { String k, Integer v ->
            totalQuantity += v;
        }

        if( totalQuantity > itemToMove.quantity )
            return false;

        def shipment = itemToMove.shipment;
        containerIdToQuantityMap.each { String containerId, int quantity ->
            def container = Container.get(containerId)

            def existingItem = findShipmentItem(
					itemToMove.shipment,
                    container,
                    itemToMove.product,
                    itemToMove.lotNumber,
                    itemToMove.inventoryItem);

            if (existingItem) {
                existingItem.quantity += quantity;
            } else {
                def shipmentItem = copyShipmentItem(itemToMove);
                shipmentItem.container = container;
                shipmentItem.quantity = quantity;
                shipment.addToShipmentItems(shipmentItem);
            }

            itemToMove.quantity -= quantity
            if(itemToMove.quantity == 0) {
                shipment.removeFromShipmentItems(itemToMove);
            }
        }

        return true;
    }

}
