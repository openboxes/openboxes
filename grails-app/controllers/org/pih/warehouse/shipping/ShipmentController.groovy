package org.pih.warehouse.shipping;

import java.sql.ResultSet;

import grails.converters.JSON
import groovy.sql.Sql;
import au.com.bytecode.opencsv.CSVWriter;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;

class ShipmentController {
   
    def scaffold = Shipment
    def shipmentService
	def dataSource
	def sessionFactory
	
	def create = {
		def shipmentInstance = new Shipment()
		shipmentInstance.properties = params
		
		if (params.type == "incoming") { 
			shipmentInstance.destination = session.warehouse;			
		}
		else if (params.type == "outgoing") { 
			shipmentInstance.origin = session.warehouse;			
		}		
		//return [shipmentInstance: shipmentInstance]
		render(view: "create", model: [ shipmentInstance : shipmentInstance,
				warehouses : Warehouse.list(), eventTypes : EventType.list()]);
	}

	def save = {
		def shipmentInstance = new Shipment(params)
				
		if (shipmentInstance.save(flush: true)) {
		
			// Try to add the initial event
			def eventType = EventType.get(params.eventType.id);
			if (eventType) {
				def shipmentEvent = new Event(eventType: eventType, eventLocation: session.warehouse, eventDate: new Date())
				shipmentInstance.addToEvents(shipmentEvent).save(flush:true);
			}
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
			redirect(action: "showDetails", id: shipmentInstance.id)
		}
		else {
			//redirect(action: "create", model: [shipmentInstance: shipmentInstance], params: [type:params.type])
			render(view: "create", model: [shipmentInstance : shipmentInstance,
				warehouses : Warehouse.list(), eventTypes : EventType.list()]);
		}
	}

	def update = {		
		
		log.info params
		def shipmentInstance = Shipment.get(params.id)
		if (shipmentInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (shipmentInstance.version > version) {					
					shipmentInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'shipment.label', default: 'Shipment')] as Object[], "Another user has updated this Shipment while you were editing")
					render(view: "editDetails", model: [shipmentInstance: shipmentInstance])
					return
				}
			}
			shipmentInstance.properties = params
			if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
				redirect(action: "showDetails", id: shipmentInstance.id)
			}
			else {
				render(view: "editDetails", model: [shipmentInstance: shipmentInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: "list")
		}
	}

	
		
	def showDetails = {
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			[shipmentInstance: shipmentInstance]
		}
	}

	def showDetailsAlt = {
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			[shipmentInstance: shipmentInstance]
		}
	}
	
	def editDetails = {
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			[shipmentInstance: shipmentInstance]
		}
	}

	def sendShipment = {
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			if ("POST".equalsIgnoreCase(request.getMethod())) { 
			
				shipmentInstance.properties = params
				if (!shipmentInstance.hasErrors() && shipmentInstance.save(flush: true)) {
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
					redirect(action: "showDetails", id: shipmentInstance.id)
				}
			}
			
			
			render(view: "sendShipment", model: [shipmentInstance: shipmentInstance])
		}		
	}

	def receiveShipment = {
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			[shipmentInstance: shipmentInstance]
		}
	}

	def showPackingList = { 
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			[shipmentInstance: shipmentInstance]
		}
	}
	
	def downloadPackingList = { 
		def shipmentInstance = Shipment.get(params.id)
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			//List<String[]> allElements = new ArrayList<String[]>();
			//shipmentInstance.getAllShipmentItems().each { 
			//	def row = it.name
			//}
			String query = """\
				select  
					container.name,  
					container.height, 
					container.width, 
					container.length, 
					container.volume_units, 
					container.weight, 
					container.weight_units,
					shipment_item.quantity,
					product.name,
					shipment_item.serial_number
				from shipment, container, shipment_item, product
				where shipment.id = container.shipment_id
				and shipment_item.container_id = container.id
				and shipment_item.product_id = product.id """
				
			StringWriter sw = new StringWriter();
			CSVWriter writer = new CSVWriter(sw);
			Sql sql = new Sql(sessionFactory.currentSession.connection())	
			
			String [] colArray = new String[6];
			colArray.putAt(0, "unit");
			colArray.putAt(1, "dimensions");
			colArray.putAt(2, "weight");
			colArray.putAt(3, "qty");
			colArray.putAt(4, "item");
			colArray.putAt(5, "serial number");
			writer.writeNext(colArray);
			sql.eachRow(query) { row -> 
				
				def rowArray = new String[6];
				rowArray.putAt(0, row[0]);
				rowArray.putAt(1, row[1] + "x" + row[2] + "x" + row[3] + " " + row[4]);
				rowArray.putAt(2, row[5] + " " + row[6] );
				rowArray.putAt(3, row[7]);
				rowArray.putAt(4, row[8]);
				rowArray.putAt(5, row[9]);
				writer.writeNext(rowArray);
			}
			
			//writer.writeAll(resultSet, false);
			log.info "results: " + sw.toString();
			response.setHeader("Content-disposition", "attachment; filename=shipments.csv");
			render(contentType: "text/csv", text: sw.toString());			
			sql.close();
			//resultSet.close();
			
			
		}
	}
	
	
	def editContents = {
		def shipmentInstance = Shipment.get(params.id)
		def containerInstance = Container.get(params?.container?.id);
		
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: (params.type == "incoming") ? "listIncoming" : "listOutgoing")
		}
		else {
			[shipmentInstance: shipmentInstance, containerInstance: containerInstance]
		}
	}
	
	
	
	HashMap getShipmentsByStatus(List shipments) { 
		def shipmentListByStatus = new HashMap<String, ListCommand>();
		shipments.each {
			
			Shipment shipment = (Shipment) it;			
			def status = shipment.getMostRecentStatus();
			def shipmentList = shipmentListByStatus[status];			
			// Shipment list does not exist for this status, create a new one
			if (!shipmentList) {
				shipmentList = new ListCommand(category: status, objectList: new ArrayList());
			}			
			// Populate shipment list and map
			shipmentList.objectList.add(shipment);
			shipmentListByStatus.put(status, shipmentList)
		}
		
		return shipmentListByStatus;
				
		
	}
	
	
	def listIncoming = { 
		def incomingShipments = null;

		def currentLocation = Location.get(session.warehouse.id);		
		if (params.searchQuery) { 
			incomingShipments = shipmentService.getShipmentsByNameAndDestination(params.searchQuery, currentLocation);			
		} else {  
			incomingShipments = shipmentService.getShipmentsByDestination(currentLocation);		
		}
		
		[
			shipmentInstanceMap : getShipmentsByStatus(incomingShipments),
			shipmentInstanceList : incomingShipments,
			shipmentInstanceTotal : incomingShipments.size(),
		];
	}
	
	
	def listOutgoing = { 
		def currentLocation = Location.get(session.warehouse.id);		
		def outgoingShipments = shipmentService.getShipmentsByOrigin(currentLocation);		
		[
			shipmentInstanceMap : getShipmentsByStatus(outgoingShipments),
			shipmentInstanceList : outgoingShipments,
			shipmentInstanceTotal : outgoingShipments.size(),
		];
		
		
	}
	
	
    def list = { 
    	def browseBy = params.id;
    	def currentLocation = Location.get(session.warehouse.id);    	
    	log.debug ("current location" + currentLocation.name)    	
    	def allShipments = shipmentService.getShipmentsByLocation(currentLocation);
		def incomingShipments = shipmentService.getShipmentsByDestination(currentLocation);	
		def outgoingShipments = shipmentService.getShipmentsByOrigin(currentLocation);			
		def shipmentInstanceList = ("incoming".equals(browseBy)) ? incomingShipments : 
			("outgoing".equals(browseBy)) ? outgoingShipments : allShipments;		
		// Arrange shipments by status 
		def shipmentListByStatus = new HashMap<String, ListCommand>();
		allShipments.each {
			def shipmentList = shipmentListByStatus[it.shipmentStatus];
			if (!shipmentList) {
				shipmentList = new ListCommand(category: it.shipmentStatus.name, color: it.shipmentStatus.color, 
					sortOrder: it.shipmentStatus.sortOrder, objectList: new ArrayList());		
			}
			shipmentList.objectList.add(it);	
			shipmentListByStatus.put(it.shipmentStatus, shipmentList)		
		}
		
		// Get a count of shipments by status		 
		// QUERY: select shipment_status.id, count(*) from shipment group by shipment_status.id 
			
		def criteria = Shipment.createCriteria()
		def results = criteria {			
			projections {
				groupProperty("shipmentStatus")
				count("shipmentStatus", "shipmentCount") //Implicit alias is created here !
			}
			//order 'myCount'
		}			
			
		[ 	results : results, shipmentInstanceList : shipmentInstanceList,
			shipmentInstanceTotal : allShipments.size(), shipmentListByStatus : shipmentListByStatus,
			incomingShipmentCount : incomingShipments.size(), outgoingShipmentCount : outgoingShipments.size()
		]
    }
	        
    
    def addShipmentAjax = {
		try {
		    //def newPost = postService.createPost(session.user.userId, params.content);
		    //def recentShipments = Shipment.findAllBy(session.user, [sort: 'id', order: 'desc', max: 20])
		    //render(template:"list", collection: recentShipments, var: 'shipment')
		    render { div(class:"errors", "success message") }
		} catch (Exception e) {
		    render { div(class:"errors", e.message) }
		}
    }

	
	def availableContacts = { 
		def contacts = null;
		if (params.query) {
			contacts = Contact.withCriteria { 
				or { 
					ilike("name", "%${params.query}%")
					ilike("email", "%${params.query}%")
					ilike("phone", "%${params.query}%")
					ilike("firstName", "%${params.query}%")
					ilike("lastName", "%${params.query}%")
				}
			}
			
			contacts = contacts.collect() {
				[id : it.id, name : it.name]
			}
		}
		def jsonItems = [result: contacts]
		render jsonItems as JSON;
	}
		
	
	def availableShipments = { 
		log.debug params;
		def items = null;
		if (params.query) {
			items = Shipment.findAllByNameLike("%${params.query}%", [max:10, offset:0, "ignore-case":true]);
			items = items.collect() {
				[id:it.id, name:it.name]
			}
		}
		def jsonItems = [result: items]
		render jsonItems as JSON;
	}

	
    
    def availableItems = {     		
    	log.debug params;
    	def items = null;
    	if (params.query) { 
			
			//String [] parts = params.query.split(" ");
			
	    	//items = Product.findAllByNameLike("%${params.query}%", [max:10, offset:0, "ignore-case":true]);
	    	items = Product.withCriteria { 
				or { 
					ilike("name", "%${params.query}%")
					ilike("description", "%${params.query}%")
				}
			}
			
			items = items.collect() {
	    		[id:it.id, name:it.name]
	    	}
    	}
    	def jsonItems = [result: items]    	
    	render jsonItems as JSON;    		
    }
    
    def addItemAutoComplete = {     		
    	log.info params;    	
		def shipment = Shipment.get(params.id);		
		def container = Container.get(params.container.id);
    	def product = Product.get(params.selectedItem_id)
		def quantity = (params.quantity) ? Integer.parseInt(params.quantity) : 1;
		def shipmentItem = null;
		
		// Create a new unverified product
		if (!product) { 
			product = new Product(name: params.selectedItem, unverified: true).save(failOnError:true)
		}
		
		// Add item to container if product doesn't already exist
    	if (container) { 
			boolean found = false;
			container.shipmentItems.each { 
				if (it.product == product) { 					
					it.quantity += quantity;
					it.save();
					found = true;
				}
			}			
			if (!found) { 			
				shipmentItem = new ShipmentItem(product: product, quantity: quantity, serialNumber: params.serialNumber, recipient: params.recipient);
				container.addToShipmentItems(shipmentItem).save(flush:true);				
			}			
    	}
		/*
		// Add to all shipment containers 
		else { 
			shipment.getContainers().each { 
				it.addToShipmentItems(shipmentItem).save(flush:true);				
			}
		}*/
		
    	redirect action: "editContents", id: shipment?.id, params: ["container.id": container?.id];
    }    
	
    
    
    def addContainer = { 		
		log.debug params 		
    	def shipment = Shipment.get(params.shipmentId);   	
    	def containerType = ContainerType.get(params.containerTypeId);    	
    	def containerName = (params.name) ? params.name : containerType.name + " " + (shipment.getContainers().size() + 1);
        def container = new Container(name: containerName, weight: params.weight, weightUnits: params.weightUnits, containerType: containerType);
        shipment.addToContainers(container);
		redirect(action: 'editContents', id: params.shipmentId)    
    }

	/*
	def editContainer = {
		def container = Shipment.get(params.containerId);
		def containerType = ContainerType.get(params.containerTypeId);
		def containerName = (params.name) ? params.name : containerType.name + " " + shipment.getContainers().size()
		def container = new Container(name: containerName, weight: params.weight, units: params.weightUnits, containerType: containerType);
		container.save(flush:true);
		flash.message = "Added a new piece to the shipment";
		redirect(action: 'show', id: params.shipmentId)
	}*/

	
	def editContainer = {		
		
		log.info params
		
		def shipmentInstance = Shipment.get(params.shipmentId)		
		def containerInstance = Container.get(params.containerId)
		if (containerInstance) {
			/*
			if (params.version) {
				def version = params.version.toLong()
				if (containerInstance.version > version) {					
					containerInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'container.label', default: 'Container')] as Object[], "Another user has updated this Product while you were editing")
					render(view: "edit", model: [containerInstance: containerInstance])
					return
				}
			}
			*/
			containerInstance.properties = params
			
			Iterator iter = containerInstance.shipmentItems.iterator()
			while (iter.hasNext()) {
				def item = iter.next()
				log.info item.product.name + " " + item.quantity;
				
				if (item.quantity == 0) {
					item.delete();
					//containerInstance.removeFromShipmentItems(item);
					iter.remove();					
				}				
			}
			
			if (!containerInstance.hasErrors() && containerInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'container.label', default: 'Container'), containerInstance.id])}"
				redirect(action: "editContents", id: shipmentInstance.id)
			}
			else {
				flash.message = "Could not edit container"
				redirect(action: "showDetails", id: shipmentInstance.id)
				//render(view: "edit", model: [containerInstance: containerInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'container.label', default: 'Container'), params.containerId])}"
			redirect(action: "showDetails", id: shipmentInstance.id)
			//redirect(action: "list")
		}
	}
	
	
	    
    def copyContainer = { 
    	def shipment = Shipment.get(params.shipmentId);   	
    	def container = Container.get(params.containerId);  
    	

		def numCopies = Integer.parseInt(params.copies)
    	int index = (shipment?.containers)?(shipment.containers.size()):1;
    	try { 
			index = Integer.parseInt(container.name);
		} catch (NumberFormatException e) {
			log.warn("The given value " + params.name + " is not an integer");
		}
		
		
		while ( numCopies-- > 0 ) {
    		def containerCopy = new Container(container.properties);
    		containerCopy.id = null;
    		containerCopy.name = "" + (++index);
    		containerCopy.containerType = container.containerType;
    		containerCopy.weight = container.weight;
    		containerCopy.dimensions = container.dimensions;
    		containerCopy.shipmentItems = null;
    		containerCopy.save(flush:true);
			    		
    		container.shipmentItems.each { 
    			def shipmentItemCopy = new ShipmentItem(product: it.product, quantity: it.quantity, serialNumber: it.serialNumber, recipient: it.recipient);
    			containerCopy.addToShipmentItems(shipmentItemCopy).save(flush:true);
    		}    		
    		shipment.addToContainers(containerCopy).save(flush:true);
    	}
		flash.message = "Copied package multiple times within the shipment";		
		redirect(action: 'editContents', id: params.shipmentId)        		
    }    
    
    
    def deleteContainer = { 
    		
		def container = Container.get(params.id);
    	def shipmentId = container.getShipment().getId();
		/*
    	if (container.getShipmentItems().size() > 0) {
    		flash.message = "Cannot delete a shipment unit that is not empty";
    	}
    	else { 
			container.delete();
    	} */
		
		container.delete();   		
		redirect(action: 'editContents', id: shipmentId)     		
    }
    
    
    def addComment = { 
    	log.debug params;
    	def shipment = (params.shipmentId) ? Shipment.get(params.shipmentId) : null;    	
    	def recipient = (params.recipientId) ? User.get(params.recipientId) : null;
    	def comment = new Comment(comment: params.comment, commenter: session.user, recipient: recipient)
    	if (shipment) { 
	    	shipment.addToComments(comment).save();
	    	flash.message = "Added comment '${params.comment}'to shipment $shipment.id";		
    	}
		redirect(action: 'show', id: params.shipmentId)    	    		
    }
    
    def deleteComment = { 
    	def comment = Comment.get(params.id);
   		def shipmentId = comment.getShipment().getId();    	
    	if (comment) { 	    	
       	    comment.delete();	    	    	
        	flash.message = "Deleted comment $comment from shipment $shipment.id";		
	    	redirect(action: 'show', id: shipmentId) 
    	}
    	else { 
        	flash.message = "Could not remove comment $params.id from shipment";		
    		redirect(action: 'show', id: shipmentId)    	
    		
    	}
    }
    
    

    def addItem = {     		
    	log.debug params;
		def container = Container.get(params.containerId);
    	def product = Product.get(params.productId);
    	def quantity = params.quantity;

		// if container already includes a shipment item with this product, 
    	// we just need to add to the total quantity
    	def weight = product.weight * Integer.valueOf(quantity);
    	
		//def donor = null;
		//if (params.donorId)
		def donor = Organization.get(params.donorId);
					
		def shipmentItem = new ShipmentItem(product: product, quantity: quantity, weight: weight, donor: donor);
    	container.addToShipmentItems(shipmentItem).save(flush:true);
    	flash.message = "Added $params.quantity units of $product.name";		
		redirect(action: 'show', id: params.shipmentId)    	
    	
    }
	
	
	def addDocument = { 
		log.info params
		def shipmentInstance = Shipment.get(params.id);
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
			redirect(action: "list")
		}
		render(view: "addDocument", model: [shipmentInstance : shipmentInstance, document : new Document()]);
	}
    

    def deleteItem = { 
    	def item = ShipmentItem.get(params.id);
		def container = item.getContainer();
		def shipmentId = container.getShipment().getId();    	
    	if (item) { 	    	
	    	item.delete();	    	    	
        	flash.message = "Deleted shipment item $params.id from container $container.name";		
	    	redirect(action: 'show', id: shipmentId) 
    	}
    	else { 
        	flash.message = "Could not remove item $params.id from container";		
    		redirect(action: 'show', id: shipmentId)    	
    		
    	}
    }
	
	def editItem = { 
		def item = ShipmentItem.get(params.id);
		def container = item.getContainer();
		def shipmentId = container.getShipment().getId();
		if (item) {
			item.quantity = Integer.parseInt(params.quantity);
			item.save();
			flash.message = "Deleted shipment item $params.id from container $container.name";
			redirect(action: 'editContents', id: shipmentId)
		}
		else {
			flash.message = "Could not edit item $params.id from container";
			redirect(action: 'showDetails', id: shipmentId, params: [container.id, container.id])
			
		}

		
	}

    
    def deleteDocument = { 
    	def document = Document.get(params.id);
   		def shipmentId = document.getShipment().getId();    	
    	if (document) { 	    	
       	    document.delete();	    	    	
        	flash.message = "Deleted document $params.id from shipment";		
	    	redirect(action: 'showDetails', id: shipmentId) 
    	}
    	else { 
        	flash.message = "Could not remove document $params.id from shipment";		
    		redirect(action: 'showDetails', id: shipmentId)    	
    		
    	}
    }
    
    
    def addEvent = { 
		
		
    	if ("GET".equals(request.getMethod())) { 
			
			def shipmentInstance = Shipment.get(params.id);
			if (!shipmentInstance) {
				flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipmentEvent.label', default: 'ShipmentEvent'), params.id])}"
				redirect(action: "list")
			}
			render(view: "addEvent", model: [shipmentInstance : shipmentInstance, shipmentEvent : new Event()]);
		
			
		}
		else { 						
	    	Event event = new Event(
	    		eventType:EventType.get(params.eventTypeId), eventDate: params.eventDate, 
	    		eventLocation: Location.get(params.eventLocationId)
	    	);
	    	
	    	def shipment = Shipment.get(params.shipmentId);     	
	    	shipment.addToEvents(event).save(flush:true);    
	
	    	flash.message = "Added event";		
			redirect(action: 'showDetails', id: params.shipmentId)    	
		}
	}    

    def deleteEvent = { 
    	def event = Event.get(params.id);
    	def shipmentId = event.getShipment().getId();    	
    	event.delete();	    	    	
    	redirect(action: 'show', id: shipmentId) 
    }
    
	
	def addReferenceNumber = { 		
		def referenceNumber = new ReferenceNumber(params);
		def shipment = Shipment.get(params.shipmentId);
		shipment.addToReferenceNumbers(referenceNumber);
		flash.message = "Added reference number";
		redirect(action: 'show', id: params.shipmentId)
	}
    
    def form = {
        [ shipments : Shipment.list() ]
    }
    
    def view = {
    	// pass through to "view shipment" page
    }
}


class ListCommand { 	
	String category;
	String color;
	List objectList;
	Integer sortOrder;

    static constraints = {

    }
	
	
	public int compareTo(def other) {
		return id <=> other?.id
		
		//return sortOrder <=> other?.sortOrder // <=> is the compareTo operator in groovy
	}
	

	
}

