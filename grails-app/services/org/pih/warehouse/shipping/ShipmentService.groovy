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

import grails.validation.ValidationException
import org.apache.commons.validator.EmailValidator
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.*
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem

import javax.mail.internet.InternetAddress

class ShipmentService {

    boolean transactional = true

    MailService mailService;
    def sessionFactory;
    def productService
    def inventoryService;
    def identifierService
    def documentService

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
            } else {
                return shipment
            }
        } else {
            Shipment shipment = new Shipment()

            if (shipmentType) {
                ShipmentType shipmentTypeObject = ShipmentType.findByNameIlike(shipmentType)
                if (!shipmentTypeObject) {
                    throw new Exception(shipmentType + " is not a valid shipment type")
                } else {
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
	List<Shipment> getRecentOutgoingShipments(String locationId, int daysBefore, int daysAfter) { 		
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
					between("expectedShippingDate", now -daysBefore, now +daysAfter)
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
	List<Shipment> getRecentIncomingShipments(String locationId, int daysBefore, int daysAfter) { 		
		def startTime = System.currentTimeMillis()
		Location location = Location.get(locationId);
		Date fromDate = new Date() - daysBefore
		Date toDate = new Date() + daysAfter
		//return Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, new Date()-30, new Date()+30, 
		def shipments = Shipment.findAllByDestinationAndExpectedShippingDateBetween(location, fromDate, toDate, 
			[max:10, offset:2, sort:"expectedShippingDate", order:"desc"]);
		
		log.info "Get recent incoming shipments " + (System.currentTimeMillis() - startTime) + " ms"
		return shipments
	}
	

	/**
	 * 	
	 * @param shipments
	 * @return
	 */
	Map<EventType, ListCommand> getShipmentsByStatus(List shipments) { 
		def startTime = System.currentTimeMillis()
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

        log.info "Get shipments by status " + (System.currentTimeMillis() - startTime) + " ms"
		
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
                ilike("name", "%" + name + "%")
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
                ilike("name", "%" + name + "%")
                eq("origin", location)
            }
        }
    }


    List<Shipment> getPendingShipments(Location origin) {
        return getPendingShipments(origin, null)
    }

	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getPendingShipments(Location origin, Location destination) {
		def shipments = Shipment.withCriteria {
			if (origin) {
                eq("origin", origin)
            }
            if (destination) {
                eq("destination", destination)
            }
            or {
                isEmpty('events')
                events {
                    eventType {
                        not {
                            // TODO This should really be a list managed by the EventCode enum e.g. EventCode.listPending()
                            'in'("eventCode", [EventCode.SHIPPED, EventCode.RECEIVED])
                        }
                    }
                }
            }
		}
		
		return shipments
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
        shipments.addAll(getIncomingShipments(location));
		
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
	List<Shipment> getIncomingShipments(Location destination) {
        return getPendingShipments(null, destination)
	}
	
	
	/**
	 * Get all shipments that are shipping from the given location.
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getOutgoingShipments(Location origin) {
        getPendingShipments(origin, null)
	}

	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByDestination(Location location) {
		def startTime = System.currentTimeMillis()
		def shipments = Shipment.withCriteria { 
			eq("destination", location) 
		}
        log.info "Get shipments by destination " + (System.currentTimeMillis() - startTime) + " ms"
		return shipments
	}
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	List<Shipment> getShipmentsByOrigin(Location location) {
		def startTime = System.currentTimeMillis()
		def shipments = Shipment.withCriteria { 
			eq("origin", location);
		}
        log.info "Get shipments by origin " + (System.currentTimeMillis() - startTime) + " ms"
		
		return shipments
	}

    /**
     * Used to perform a bulk update on shipments that do not have their current status set.
     *
     * @return
     */
    boolean bulkUpdateShipments() {
        long startTime = System.currentTimeMillis()
        boolean success = false
        try {
            def shipments = Shipment.findAllByCurrentStatusIsNullAndEventsIsNotNull([max:1000])
            if (shipments) {
                shipments.each {
                    it.currentStatus = it.status.code
                    it.currentEvent = it.mostRecentEvent
                    it.save(flush: true)
                }
                long elapsedTime = System.currentTimeMillis() - startTime
                log.info "Successfully bulk updated ${shipments?.size() ?: 0} shipments in ${elapsedTime} ms"
            }
            success = true
        } catch (Exception e) {
            log.error("Unable to bulk update all shipments" + e.message, e)
        }
        return success

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
	List<Shipment> getShipments(String terms, ShipmentType shipmentType, Location origin, Location destination,
                                ShipmentStatusCode statusCode, Date statusStartDate, Date statusEndDate, Date lastUpdatedStart, Date lastUpdatedEnd, Integer limit) {

        log.info "Get shipments: " + terms + " " + shipmentType + " " + origin + " " + destination + " " + lastUpdatedStart + " " + lastUpdatedEnd

        def shipments = Shipment.withCriteria {
            and {
                if (terms) {
                    or {
                        ilike("name", "%" + terms + "%")
                        ilike("shipmentNumber", "%" + terms + "%")
                    }
                }
                if (shipmentType) { eq("shipmentType", shipmentType) }
                if (origin) { eq("origin", origin) }
                if (destination) { eq("destination", destination) }
                if (lastUpdatedStart) { ge("lastUpdated", lastUpdatedStart)}
                if (lastUpdatedEnd) { le("lastUpdated", lastUpdatedEnd)}
                if (statusCode) { eq("currentStatus", statusCode)}

                order("dateCreated", "desc")
                order("lastUpdated", "desc")
            }
            maxResults(limit)
        }

        log.info "Shipments: " + shipments.size()

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

	void moveShipmentItemToContainer(String shipmentItemId, String containerId) {
		log.info "Move shipment item ${shipmentItemId} to container ${containerId} "
		def shipmentItem = ShipmentItem.get(shipmentItemId);
		if (shipmentItem) {
			if (containerId.equals("trash")) {
				log.info "Removing item " + shipmentItem + " from " + shipmentItem?.container
				shipmentItem.container = null;
				shipmentItem.shipment.removeFromShipmentItems(shipmentItem)
				shipmentItem.delete(flush: true);
			} else {
				def container = Container.get(containerId);
				log.info "Move item " + shipmentItem + " from " + shipmentItem?.container + " to " + container
				shipmentItem.container = container;
				shipmentItem.save(flush: true);
			}
		}

	}

	void moveContainerToContainer(String childContainerId, String parentContainerId) {
		log.info "Move child container ${childContainerId} to parent container ${parentContainerId} "
		def childContainer = Container.get(childContainerId);
		if (childContainer) {
			if (parentContainerId.equals("trash")) {
				log.info "Removing container " + childContainer + " from shipment " + childContainer.shipment
				deleteContainer(childContainer)
			} else {
				def parentContainer = Container.get(parentContainerId);
				log.info "Move container " + childContainer + " from container " + childContainer?.parentContainer + " to container " + parentContainer
				// Cannot move a child container into another child container
				if (parentContainer?.parentContainer) {
					throw new ShipmentException(message: "Moving a container into a sub-container in currently not supported.", shipment: childContainer?.shipment)
				}
				if (childContainer.containers && parentContainer) {
					throw new ShipmentException(message: "Moving a container with subcontainers into another container is currently not supported.", shipment: childContainer?.shipment)
				}

				childContainer.parentContainer = parentContainer
				childContainer.shipment.save(flush:true);
			}
		}

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
        log.info "Shipment items " + shipmentItems?.size()
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
	boolean saveShipmentItem(ShipmentItem shipmentItem) {
        return shipmentItem.save()
	}
	
	
	/**
	 * Add a shipment item to a shipment.
	 * 
	 * @param shipmentItem
	 * @param shipment
	 */
	void addToShipmentItems(ShipmentItem shipmentItem, Shipment shipment) {
		// Need to set the shipment here for validation purposes
		shipmentItem.shipment = shipment;

		// Check if it requires validation
		boolean validated = true
		if (shipment?.origin?.isWarehouse()) {
			validated = validateShipmentItem(shipmentItem)
		}

		if (validated) {
			shipment.addToShipmentItems(shipmentItem);
			shipment.save()
		}
	}

	boolean addToShipmentItems(String shipmentId, String containerId, String inventoryItemId, Integer quantity) {

		Shipment shipment = Shipment.get(shipmentId)
		Container container = Container.get(containerId)
		InventoryItem inventoryItem = InventoryItem.get(inventoryItemId)
		if (!inventoryItem) {
            shipment.errors.reject("shipmentItem.inventoryItem.required")
			throw new ValidationException("Cannot add shipment item without valid inventory item", shipment.errors)
		}
		ShipmentItem shipmentItem = new ShipmentItem();
		shipmentItem.inventoryItem = inventoryItem
		shipmentItem.lotNumber = inventoryItem.lotNumber
		shipmentItem.expirationDate = inventoryItem.expirationDate
		shipmentItem.product = inventoryItem.product
		shipmentItem.quantity = quantity
		shipmentItem.container = container
		shipmentItem.shipment =  shipment

        addToShipmentItems(shipmentItem, shipment)
	}


	void sortContainers(containerIds) {

		containerIds.eachWithIndex { id, index ->
			def container = Container.get(id)
			container.sortOrder = index
			container.save(flush:true);
			println ("container " + container.name + " saved at index " + index)
		}
		//container.shipment.save(flush:true)
		//container.shipment.refresh()

	}


    /**
     * Validate the shipment item when it's being added to the shipment.
     *
     * @param shipmentItem
     * @return
     */
    boolean validateShipmentItem(ShipmentItem shipmentItem) {
        def location = Location.get(shipmentItem?.shipment?.origin?.id);
        log.info("Validating shipment item at " + location?.name + " for product=" + shipmentItem.product + ", lotNumber=" + shipmentItem.inventoryItem + ", binLocation=" + shipmentItem.binLocation)

        log.info "location = id:${location.id} name:${location.name} code:${location.locationNumber} local:${location.local}"

        // Location must be locally managed and
        if (location?.local && location.isWarehouse()) {

            // FIXME Please refactor this mess at a later date
            // If bin location is provided, check whether there's any stock in the bin location, then check against the lot number
            def quantityOnHand = shipmentItem.binLocation ?
                    inventoryService.getQuantityFromBinLocation(shipmentItem.binLocation, shipmentItem.inventoryItem) :
                    inventoryService.getQuantity(location, shipmentItem.product, shipmentItem.lotNumber)

            if (!shipmentItem.validate()) {
                throw new ValidationException("Shipment item is invalid", shipmentItem.errors)
            }

            log.info("Checking shipment item quantity [" + shipmentItem.quantity + "] vs onhand quantity [" + quantityOnHand + "]");
            if (shipmentItem.quantity > quantityOnHand) {
                shipmentItem.errors.rejectValue("quantity", "shipmentItem.quantity.cannotExceedOnHandQuantity",
                        [shipmentItem.quantity, quantityOnHand, shipmentItem?.product?.productCode, location.name].toArray(), "Shipping quantity cannot exceed on-hand quantity for product code " + shipmentItem.product.productCode)
                //throw new ShipmentItemException(message: "shipmentItem.quantity.cannotExceedOnHandQuantity", shipmentItem: shipmentItem)
                throw new ValidationException("Shipment item is invalid", shipmentItem.errors)
            }
        }
        return true;
    }


	
	/**
	 * Deletes a shipment and all of its related objects
	 * 
	 * @param shipment
	 */
	void deleteShipment(Shipment shipment) { 
		shipment.delete(flush:true)
	}


	void createContainers(shipmentId, containerId, containerTypeId, containerText) {
		log.info "Adding containers to shipment "

		Shipment shipment = Shipment.get(shipmentId)
		Container parentContainer = Container.get(containerId)
		if (shipment) {
			containerText.split("\n").each { name ->
				def containerType = ContainerType.get(containerTypeId)
				if (!containerType) {
					throw new ShipmentException(message: "You must specify a container type when creating new containers", shipment: shipment)
				}

				Container container = shipment.addNewContainer(containerType)
				container.name = name
				if (parentContainer) {
					container.parentContainer = parentContainer
				}
				saveContainer(container)
			}
		}
	}


    void deleteAllContainers(String id, boolean deleteItems) {
        Shipment shipment = Shipment.get(id)
        List containerIds = shipment?.findAllParentContainers()*.id
        deleteContainers(id, containerIds, deleteItems)

        // Delete all unpacked items
        shipment.unpackedShipmentItems.each { shipmentItem ->
            shipment.removeFromShipmentItems(shipmentItem)
            shipmentItem.delete()
            //shipment.save()
        }

    }


	void deleteContainers(String id, List containerIds, boolean deleteItems) {
        log.info "Delete containers " + containerIds + " from shipment " + id
		Shipment shipment = Shipment.get(id)
		if (shipment) {
            if (containerIds) {
                containerIds.each { containerId ->
                    Container container = Container.get(containerId)
                    println("Contains items: " + container.shipmentItems)
                    if (!deleteItems && container.shipmentItems) {
                        throw new ShipmentException(message: "Cannot delete container that contains items", shipment: shipment);
                    } else {
                        container.shipmentItems.each { shipmentItem ->
                            shipment.removeFromShipmentItems(shipmentItem)
                            shipmentItem.delete()
                        }

                    }
                    shipment.removeFromContainers(container);
                    if (container?.parentContainer) {
                        container?.parentContainer?.removeFromContainers(container)
                    }
                    container.delete();
                }
            }
		}
	}

	/**
	 * Deletes a container, but leaves all shipment items 
	 * 
	 * @param container
	 */
	void deleteContainer(Container container) {

        // nothing to do if null
        if (!container) {
            return
        }

        List containerIds = []
        if (container.containers) {
            container?.containers.each {
                containerIds << it.id
            }
        }
        containerIds << container.id
        deleteContainers(container?.shipment?.id, containerIds, true)
    }

    void deleteContainerKeepItems(Container container) {
        // nothing to do if null
        if (!container) {
            return
        }

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

		// remove from parent container
		if (container.parentContainer) {
			container.parentContainer.removeFromContainers(container)
		}
		//container.shipment.save()
		container.delete()
	}
	
	
	/**
	 * Deletes a shipment item
	 * 
	 * @param item
	 */
	void deleteShipmentItem(ShipmentItem shipmentItem) {
		if (shipmentItem) {
			def shipment = Shipment.get(shipmentItem.shipment.id)
			shipment.removeFromShipmentItems(shipmentItem)
			shipmentItem.delete(flush: true)
		}
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
		if (!shipDate || shipDate > new Date()) {
			shipmentInstance.errors.reject("shipment.invalid.invalidShipDate", "Shipping date [" + shipDate + "] must occur on or before today.")
			//throw new ShipmentException(message: "Shipping date [" + shipDate + "] must occur on or before today.", shipment: shipmentInstance)
		}
		if (shipmentInstance.hasShipped()) {
			shipmentInstance.errors.reject("shipment.invalid.alreadyShipped", "Shipment has already shipped")
			//throw new ShipmentException(message: "Shipment has already been shipped.", shipment: shipmentInstance);
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
			throw new ShipmentException(message: "Failed to send shipment", shipment: shipmentInstance)
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
        log.info "Creating shipment event ${eventDate} ${eventCode}"

		// Get the appropriate event type for the given event code
		EventType eventType = EventType.findByEventCode(eventCode)
		if (!eventType) {
			throw new RuntimeException("Unable to find event type for event code '" + eventCode + "'")
		}

		// Prevent duplicate events
        Event eventAlreadyExists = shipmentInstance?.events?.find { it.eventType?.eventCode == eventType?.eventCode }
		if (eventAlreadyExists) {
            shipmentInstance.errors.reject("shipment.eventAlreadyExists.message", "Event ${eventCode} already exists");
            throw new ValidationException("Unable to create shipment event", shipmentInstance.errors)
        }

        // Attempt to add the event to the shipment
        def eventInstance = new Event(eventDate: eventDate, eventType: eventType, eventLocation: location);
        if (!eventInstance.hasErrors()) {
            shipmentInstance.addToEvents(eventInstance);
        }
        else {
            throw new ValidationException("Unable to create shipment event", eventInstance.errors)
        }

	}

	void receiveShipments(List shipmentIds, String comment, String userId, String locationId, Boolean creditStockOnReceipt) {
		if (!shipmentIds) {
			throw new IllegalArgumentException("Must select at least one shipment in order to use bulk receipt")
		}
		shipmentIds.each { shipmentId ->
            Shipment shipment = Shipment.get(shipmentId)
            shipment.receipt = createReceipt(shipment, shipment.actualShippingDate+1)
			receiveShipment(shipmentId, comment, userId, locationId, creditStockOnReceipt)
		}
	}

    void rollbackShipments(List shipmentIds) {
        if (!shipmentIds) {
            throw new IllegalArgumentException("Must select at least one shipment in order to use bulk rollback")
        }
        shipmentIds.each { shipmentId ->
            Shipment.withNewSession {
                Shipment shipment = Shipment.load(shipmentId)
                rollbackLastEvent(shipment)
            }
        }
    }

    void markShipmentsAsReceived(List shipmentIds) {
        if (!shipmentIds) {
            throw new IllegalArgumentException("Must select at least one shipment in order to use mark as received")
        }

        Location location = Location.get(session.warehouse.id)
        shipmentIds.each { shipmentId ->
            Shipment shipment = Shipment.get(shipmentId)
            markAsReceived(shipment, location)
        }
    }



	void markAsReceived(Shipment shipment, Location location) { 
		try { 
			// Add a Received event to the shipment
			createShipmentEvent(shipment, new Date(), EventCode.RECEIVED, location);
											
			// Save updated shipment instance
			shipment.save();
			
		} catch (Exception e) { 
			throw new ShipmentException(message: e.message);
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


	boolean validateReceipt(Receipt receiptInstance) {

		if (!receiptInstance.validate()) {
			throw new ValidationException("receipt ${receiptInstance} not valid", receiptInstance.errors)
		}

		// validate all shipments items
		receiptInstance.shipment.shipmentItems.each { shipmentItem ->
			if (!shipmentItem.validate()) {
				throw new ValidationException("shipment item ${shipmentItem} not valid", receiptInstance.errors)
			}

		}

	}


	Receipt findOrCreateReceipt(Shipment shipmentInstance) {

		Receipt receiptInstance

		// Existing receipt
		if (shipmentInstance.receipt) {
			receiptInstance = shipmentInstance.receipt
		}
		// No existing receipt, instantiate the model to be used
		else {
			log.info "Receipt does not exists, please prepare one"
			receiptInstance = new Receipt(recipient:shipmentInstance?.recipient, shipment: shipmentInstance, actualDeliveryDate: new Date());
			shipmentInstance.receipt = receiptInstance

			def shipmentItems = shipmentInstance.shipmentItems.sort{  it?.container?.sortOrder }
			shipmentItems.each { shipmentItem ->

				def inventoryItem =
						//inventoryService.findInventoryItemByProductAndLotNumber(shipmentItem.product, shipmentItem.lotNumber)
						inventoryService.findOrCreateInventoryItem(shipmentItem.product, shipmentItem.lotNumber, shipmentItem.expirationDate)

				if (inventoryItem) {
					log.info "Adding receipt item for inventory item " + inventoryItem

					ReceiptItem receiptItem = new ReceiptItem();
					receiptItem.quantityShipped = shipmentItem.quantity;
					receiptItem.quantityReceived = shipmentItem.quantity;
					receiptItem.lotNumber = shipmentItem.lotNumber;
					receiptItem.product = inventoryItem.product
					receiptItem.inventoryItem = inventoryItem
					receiptItem.shipmentItem = shipmentItem
					receiptInstance.addToReceiptItems(receiptItem);
					shipmentItem.addToReceiptItems(receiptItem)
					receiptInstance.save(flush:true)
				}
				else {
					receiptInstance.errors.reject('inventoryItem', 'receipt.inventoryItem.invalid')
				}

			}
			shipmentInstance.save(flush:true)
		}
		return receiptInstance



	}

	
	/**
	 * 
	 * @param shipmentInstance
	 * @param comment
	 * @param user
	 * @param location
	 */
	void receiveShipment(String shipmentId, String comment, String userId, String locationId, Boolean creditStockOnReceipt) {
        log.info "Receiving shipment " + shipmentId
		User user = User.get(userId)
		Location location = Location.get(locationId)
		Shipment shipmentInstance = Shipment.get(shipmentId)


		if (shipmentInstance?.destination != location) {
			throw new ShipmentException(message: "Shipment must be received by the destination location ${shipmentInstance?.destination?.name}", shipment: shipmentInstance)

		}
		if (!shipmentInstance.hasShipped()) {
			throw new ShipmentException(message: "Shipment has not been shipped yet.", shipment: shipmentInstance)
		}

		if (shipmentInstance.wasReceived()) {
			throw new ShipmentException(message: "Shipment has already been received.", shipment: shipmentInstance)
		}

		if (shipmentInstance.receipt.actualDeliveryDate > new Date()) {
            shipmentInstance.errors.reject("shipment.mustBeReceivedOnOrBeforeToday.message",
                    "Delivery date [" + shipmentInstance.receipt.getActualDeliveryDate() + "] must occur on or before today.")

            throw new ValidationException("Shipment is not valid", shipmentInstance.errors)

		}


		if (!shipmentInstance.receipt.hasErrors() && shipmentInstance.receipt.save(flush:true)) {

			// Add comment to shipment (as long as there's an actual comment
			// after trimming off the extra spaces)
			if (comment) {
				shipmentInstance.addToComments(new Comment(comment: comment, sender: user));
			}

			// Add a Received event to the shipment
			createShipmentEvent(shipmentInstance, shipmentInstance.receipt.actualDeliveryDate, EventCode.RECEIVED, location);

			// Save updated shipment instance
			shipmentInstance.save(flush:true);
			shipmentInstance.receipt.save(flush:true)

			// only need to create a transaction if the destination is a warehouse
			if (shipmentInstance.destination?.isWarehouse() && creditStockOnReceipt) {

				// Create a new transaction for incoming items
				Transaction creditTransaction = new Transaction()
				creditTransaction.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
				creditTransaction.source = shipmentInstance?.origin
				creditTransaction.destination = null
				creditTransaction.inventory = shipmentInstance?.destination?.inventory ?: inventoryService.addInventory(shipmentInstance.destination)
				creditTransaction.transactionDate = shipmentInstance.receipt.actualDeliveryDate

				shipmentInstance.receipt.receiptItems.each {
					def inventoryItem =
						inventoryService.findOrCreateInventoryItem(it.product, it.lotNumber, it.expirationDate)

					if (inventoryItem.hasErrors()) {
						inventoryItem.errors.allErrors.each { error->
							def errorObj = [inventoryItem, error.field, error.rejectedValue] as Object[]
							shipmentInstance.errors.reject("inventoryItem.invalid",
								errorObj, "[${error.field} ${error.rejectedValue}] - ${error.defaultMessage} ");
						}
						throw new ValidationException("Failed to receive shipment while saving inventory item ", shipmentInstance.errors)
					}

					// Create a new transaction entry
					TransactionEntry transactionEntry = new TransactionEntry();
					transactionEntry.quantity = it.quantityReceived;
					transactionEntry.binLocation = it.binLocation
					transactionEntry.inventoryItem = inventoryItem;
					creditTransaction.addToTransactionEntries(transactionEntry);
					//creditTransaction.incomingShipment = shipmentInstance
				}

				if (!creditTransaction.hasErrors() && creditTransaction.save()) {
					// saved successfully
				}
				else {
					// did not save successfully, display errors message
                    throw new ValidationException("Failed to receive shipment due to error while saving transaction", creditTransaction.errors)
				}

				// Associate the incoming transaction with the shipment
				shipmentInstance.addToIncomingTransactions(creditTransaction)
				shipmentInstance.save(flush:true);

			}
		}
		else {
            throw new ValidationException("Failed to receive shipment due to error while saving receipt", shipmentInstance?.receipt?.errors)
		}
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
					// def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
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


	void deleteReceipt(Shipment shipmentInstance) {
		if (shipmentInstance?.receipt) {
			shipmentInstance?.receipt.delete()
			shipmentInstance?.receipt = null
		}
	}

	void deleteInboundTransactions(Shipment shipmentInstance) {
		def transactions = Transaction.findAllByIncomingShipment(shipmentInstance)
		transactions.each { transactionInstance ->
			if (transactionInstance) {
				shipmentInstance.removeFromIncomingTransactions(transactionInstance)
				transactionInstance?.delete();
			}
		}

	}

	void deleteOutboundTransactions(Shipment shipmentInstance) {
		def transactions = Transaction.findAllByOutgoingShipment(shipmentInstance)
		transactions.each { transactionInstance ->
			if (transactionInstance) {
				shipmentInstance.removeFromOutgoingTransactions(transactionInstance)
				transactionInstance?.delete();
			}
		}
	}


	void deleteEvent(Shipment shipmentInstance, Event eventInstance) {
		shipmentInstance.removeFromEvents(eventInstance)
		eventInstance?.delete()
		shipmentInstance?.save()
	}
   
	void rollbackLastEvent(Shipment shipmentInstance) {
		
		def eventInstance = shipmentInstance.mostRecentEvent
		
		if (!eventInstance) {
			throw new RuntimeException("Cannot rollback shipment status because there are no recent events")
		}

		try {
			
			if (eventInstance?.eventType?.eventCode == EventCode.RECEIVED) {
				deleteReceipt(shipmentInstance)
				deleteInboundTransactions(shipmentInstance)
				deleteEvent(shipmentInstance, eventInstance)
			}
			else if (eventInstance?.eventType?.eventCode == EventCode.SHIPPED) {
				deleteReceipt(shipmentInstance)
				deleteOutboundTransactions(shipmentInstance)
				deleteEvent(shipmentInstance, eventInstance)
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


	boolean exportPackingList(String shipmentId, OutputStream outputStream) {
		Shipment shipment = Shipment.get(shipmentId)
		documentService.generatePartialPackingList(outputStream, shipment)
	}

	List parsePackingList(InputStream inputStream) {

		List packingListItems = []

		HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
		HSSFSheet worksheet = workbook.getSheetAt(0);

		Iterator<Row> rowIterator = worksheet.iterator();
		int cellIndex = 0
		Row row
		while (rowIterator.hasNext()) {
			row = rowIterator.next();

			// Skip the first row
			if (row.getRowNum() == 0) {
				continue
			}

			try {
				cellIndex = 0;
				def palletName = getStringCellValue(row.getCell(cellIndex++))
				def boxName = getStringCellValue(row.getCell(cellIndex++))
				def productCode = getStringCellValue(row.getCell(cellIndex++))
				def productName = getStringCellValue(row.getCell(cellIndex++))
				def lotNumber = getStringCellValue(row.getCell(cellIndex++))
				def expirationDate = getDateCellValue(row.getCell(cellIndex++))
				def quantity = getNumericCellValue(row.getCell(cellIndex++))
                def unitOfMeasure = getStringCellValue(row.getCell(cellIndex++))
                def recipient = getStringCellValue(row.getCell(cellIndex++))

                if (productCode && quantity > 0) {
                    packingListItems << [
                            palletName: palletName,
                            boxName: boxName,
                            productCode: productCode,
                            productName: productName,
                            lotNumber: lotNumber,
                            expirationDate: expirationDate,
                            quantity: quantity,
                            unitOfMeasure:unitOfMeasure,
                            recipient:recipient
                    ]
                }
			}
			catch (IllegalStateException e) {
				log.error("Error parsing XLS file " + e.message, e)
                throw new RuntimeException("Error parsing XLS file at row " + (row.rowNum+1) + " column " + cellIndex + " caused by: " + e.message, e)
			}
            catch (Exception e) {
                log.error("Error parsing XLS file " + e.message, e)
                throw new RuntimeException("Error parsing XLS file at row " + (row.rowNum+1) + " column " + cellIndex + " caused by: " + e.message, e)

            }


		}
		return packingListItems
	}

    Date getDateCellValue(Cell cell) {
        Date value = null
        if (cell) {
            try {
                value = cell.getDateCellValue()
            }
            catch (IllegalStateException e) {
                log.warn("Error parsing string cell value [${cell}]: " + e.message, e)
                //value = Date.parse("dd-MMM-yyyy", getStringCellValue(cell))
                throw e;
            }
        }
        return value

    }

    String getStringCellValue(Cell cell) {
        String value = null
        if (cell) {
            try {
                value = cell.getStringCellValue()
            }
            catch (IllegalStateException e) {
                log.warn("Error parsing string cell value [${cell}]: " + e.message, e)
                value = Integer.valueOf((int) cell.getNumericCellValue())
            }
        }
        return value?.trim()
    }

    double getNumericCellValue(Cell cell) {
        double value = 0.0
        if (cell) {
            try {
                value = cell.getNumericCellValue()
            }
            catch (IllegalStateException e) {
                log.warn("Error parsing numeric cell value [${cell}]: " + e.message, e)
                //value = Double.parseDouble(getStringCellValue(cell))
                throw e;

            }
        }
        return value
    }


	boolean validatePackingList(List packingListItems, Location location) {

		packingListItems.each { item ->
			// Find a product using the product code
			Product product = Product.findByProductCode(item.productCode)
			if (!product) {
				throw new RuntimeException("Cannot find product with product code " + item.productCode)
			}
			item.product = product


            log.info ("item pallet " + item.palletName)
            log.info ("item box " + item.boxName)


            // there's a pallet
            if (!item.palletName && item?.boxName) {
                throw new RuntimeException("You must enter a valid Pallet if using the Box column for item " + item.productCode)
            }

			// If the location is a warehouse (it manages inventory) then we need to ensure that there's enough of the
			// item in stock before we add it to the shipment. If the location is a supplier, we don't care.
			if (location?.isWarehouse()) {
				def onHandQuantity = inventoryService.getQuantity(location, product, item.lotNumber)
				log.info("Checking shipment item quantity [" + item.quantity + "] vs onhand quantity [" + onHandQuantity + "]");

				if (item.quantity > onHandQuantity) {
					throw new RuntimeException("Quantity to ship exceeds quantity on hand for item " + item.productCode + " at location " + location?.name)
				}
			}
		}

		return true;
	}

    /**
     * Finds (or creates) a person record given the provided address (e.g. Justin Miranda <justin@openboxes.com>)
     *
     * @param address address string in RFC822 format
     * @return
     */
    Person findOrCreatePerson(String recipient) {
        Person person
		if (EmailValidator.getInstance().isValid(recipient)) {
			InternetAddress emailAddress = new InternetAddress(recipient, false)
			person = Person.findByEmail(emailAddress.address)

			// Person record not found, creating a new person as long as the name is provided
			if (!person) {
				// If there's no personal attribute we cannot determine the first and last name of the recipient.
				// This will return null and should throw an error
				if (!emailAddress.personal) {
					throw new RuntimeException("Unable to find a recipient with email address ${recipient}.")
				}
				String[] names = emailAddress.personal.split(" ", 2)
				person = new Person(firstName: names[0], lastName: names[1], email: emailAddress.address)
				person.save(flush: true)
			}
		}
        else {
            String[] names = recipient.split(" ", 2)
            if (names.length <= 1) {
                throw new RuntimeException("Recipient must have at least two names (i.e. first name and last name)")
            }

            person = Person.findByFirstNameAndLastName(names[0], names[1])
            if (!person) {
                person = new Person(firstName: names[0], lastName: names[1])
                person.save(flush: true)
            }
        }
        return person

    }

	boolean importPackingList(String shipmentId, InputStream inputStream) {
		try {

			Shipment shipment = Shipment.get(shipmentId)

			List packingListItems = parsePackingList(inputStream)


			if (validatePackingList(packingListItems, shipment?.origin)) {

				packingListItems.each { item ->

					// Find or create an inventory item given the product, lot number, and expiration date
					InventoryItem inventoryItem = inventoryService.findOrCreateInventoryItem(item.product, item.lotNumber, item.expirationDate)
					log.info("Inventory item: " + inventoryItem)

					// Find or create the pallet and box (if provided). Items are added to Unpacked Items by default.
					Container pallet = item.palletName ? shipment.findOrCreatePallet(item.palletName) : null
					Container box = item.boxName ? pallet?.findOrCreateBox(item.boxName) : null

					// The container assigned to the shipment item should be the one that contains the item (e.g. box contains item, pallet contains boxes)
					Container container = box ?: pallet ?: null

                    Person recipient
                    if (item.recipient) {
                        recipient = findOrCreatePerson(item.recipient)
                    }
					// Check to see if a shipment item already exists within the given container
					ShipmentItem shipmentItem = shipment.findShipmentItem(inventoryItem, container, recipient)
					// Create a new shipment item if not found
					if (!shipmentItem) {
						shipmentItem = new ShipmentItem(
								product: item.product,
								lotNumber: inventoryItem.lotNumber ?: '',
								expirationDate: inventoryItem?.expirationDate,
								inventoryItem: inventoryItem,
								container: container,
								quantity: item.quantity,
                                recipient: recipient
						);
						addToShipmentItems(shipmentItem, shipment)
					}
					// Modify quantity and container for existing shipment items
					else {
						//shipmentItem.inventoryItem = inventoryItem
						shipmentItem.container = container
						shipmentItem.quantity = item.quantity
                        shipmentItem.recipient = recipient
					}
				}

                log.info "Packing list items " + packingListItems
                log.info "Shipment items  " + shipment?.shipmentItems

                if(packingListItems?.size() != shipment?.shipmentItems?.size()) {
                    throw new ShipmentException(message: "Expected ${packingListItems?.size()} packing list items, but there were ${shipment?.shipmentItems?.size()} items added to the shipment. This usually means that you are trying to add identical items to the same pallet or you are trying to import a packing list that does not contain items that have already been added to the shipment. Please review your packing list for duplicate or missing items.", shipment: shipment)
                }

			}
		} catch (ShipmentItemException e) {
			log.error("Unable to import packing list items due to exception: " + e.message, e)
            throw new RuntimeException(e.message)
			//throw e;

		} catch (Exception e) {
			log.error("Unable to import packing list items due to exception: " + e.message, e)
			//throw new RuntimeException("make sure this causes a rollback", e)
			throw new RuntimeException(e.message)
		}
		finally {
			inputStream.close();
		}

		return true
	}

}
