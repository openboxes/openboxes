package org.pih.warehouse.shipping;

import java.sql.ResultSet;

import grails.converters.JSON
import groovy.sql.Sql;
import au.com.bytecode.opencsv.CSVWriter;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.Event;
import org.pih.warehouse.core.EventType;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.Warehouse;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.receiving.Receipt;
import org.pih.warehouse.receiving.ReceiptItem;

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

			// Bind request parameters 
			shipmentInstance.properties = params
			
			log.info "autocomplete shipment method: " + params
			// Create a new shipment method if one does not exist
			if (!shipmentInstance.shipmentMethod) {
				shipmentInstance.shipmentMethod = new ShipmentMethod();
			}

			// If there's an ID but no name, it means we want to remove the shipper and shipper service
			if (!params.shipperService.name) { 			
				shipmentInstance.shipmentMethod.shipper = null
				shipmentInstance.shipmentMethod.shipperService = null
			}
			// Otherwise we set the selected accordingly
			else if (params.shipperService.id) { 
				def shipperService = ShipperService.get(params.shipperService.id);
				shipmentInstance.shipmentMethod.shipperService = shipperService;
				shipmentInstance.shipmentMethod.shipper = shipperService.shipper;
				shipmentInstance.shipmentMethod.save(flush:true);
			}
									
			// This is necessary because Grails seems to be binding things incorrectly.  If we just let 
			// Grails do the binding by itself, it tries to change the ID of the 'carrier' that is already
			// associated with the shipment, rather than changing the 'carrier' object associated with 
			// the shipment.
			
			// Get the carrier object
			def safeCarrier = Person.get(params?.safeCarrier?.id)
			if (safeCarrier && params?.safeCarrier?.name == safeCarrier?.name) {
				log.info "found safe carrier by id " + safeCarrier;
				if (safeCarrier?.id != shipmentInstance?.carrier?.id) { 
					shipmentInstance?.carrier = safeCarrier;
				}
			} 
			else { 
				if (params?.safeCarrier?.name) {
					safeCarrier = convertStringToPerson(params.safeCarrier.name).save(flush:true);
					shipmentInstance.carrier = safeCarrier;
				} 
				else { 
					shipmentInstance.carrier = null;
				}
			}				
		
			
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
		log.info params
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
					def event = new Event(
						eventDate: new Date(),
						eventType:EventType.findByName("Departed"), 
						eventLocation: Location.get(session.warehouse.id)).save(flush:true);						

					shipmentInstance.addToEvents(event).save(flush:true);

					def comment = new Comment(comment: params.comment, sender: session.user)
					shipmentInstance.addToComments(comment).save(flush:true);

										
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
					redirect(action: "listOutgoing")
				}
			}
			render(view: "sendShipment", model: [shipmentInstance: shipmentInstance])
		}
	}
	
	def receiveShipment = {
		log.info "params: " + params
		def receiptInstance = new Receipt(params);
		def shipmentInstance = Shipment.get(params.id)		
		if (!shipmentInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'shipment.label', default: 'Shipment'), params.id])}"
			redirect(action: "listIncoming")
		}
		else {			
			if ("POST".equalsIgnoreCase(request.getMethod())) {				
				
				//receiptInstance.shipment = shipmentInstance;
				if (!receiptInstance.hasErrors() && receiptInstance.save(flush: true)) {
					def event = new Event(
						eventDate: new Date(),
						eventType:EventType.findByName("Received"),
						eventLocation: Location.get(session.warehouse.id)).save(flush:true);
					shipmentInstance.addToEvents(event).save(flush:true);
					
					def comment = new Comment(comment: params.comment, sender: session.user)
					shipmentInstance.addToComments(comment).save(flush:true);
					
					flash.message = "${message(code: 'default.updated.message', args: [message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
					redirect(action: "listIncoming")
				}				
			}
			else { 
				// Instantiate the model class to be used 
				receiptInstance = new Receipt(shipment:shipmentInstance, recipient:shipmentInstance?.recipient);
				
				shipmentInstance.allShipmentItems.each {										
					ReceiptItem receiptItem = new ReceiptItem(it.properties);
					receiptItem.setQuantityDelivered (it.quantity);
					receiptItem.setQuantityReceived (it.quantity);				
					receiptItem.setLotNumber(it.lotNumber);
					receiptItem.setSerialNumber (it.serialNumber);
					receiptInstance.addToReceiptItems(receiptItem);
				} 
			}
			[shipmentInstance:shipmentInstance, receiptInstance:receiptInstance]
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
			String query = """
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
				and shipment_item.product_id = product.id 
				and shipment.id = ${params.id}"""
			
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
				rowArray.putAt(1, (row[1])?row[1]:"0" + "x" + (row[2])?row[2]:"0" + "x" + (row[3])?row[3]:"0" + " " + (row[4])?row[4]:"");
				rowArray.putAt(2, row[5] + " " + row[6] );
				rowArray.putAt(3, row[7]);
				rowArray.putAt(4, row[8]);
				rowArray.putAt(5, row[9]);
				writer.writeNext(rowArray);
			}
			
			//writer.writeAll(resultSet, false);
			log.info "results: " + sw.toString();
			response.setHeader("Content-disposition", "attachment; filename=PackingList.csv");
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
			def shipmentList = shipmentListByStatus[it.mostRecentStatus];
			if (!shipmentList) {
				shipmentList = new ListCommand(category: it.mostRecentStatus, color: "#ddd", 
				sortOrder: 0, objectList: new ArrayList());
			}
			shipmentList.objectList.add(it);	
			shipmentListByStatus.put(it.mostRecentStatus, shipmentList)
		}
		
		// Get a count of shipments by status		 
		// QUERY: select shipment_status.id, count(*) from shipment group by shipment_status.id 
		
		def criteria = Shipment.createCriteria()
		def results = criteria {			
			projections {
				groupProperty("shipmentType")
				count("shipmentType", "shipmentCount") //Implicit alias is created here !
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
			render { div(class:"errors", "success message")
			}
		} catch (Exception e) {
			render { div(class:"errors", e.message)
			}
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
	
	def findPersonByName = {
		log.info "findPersonByName: " + params
		def items = new TreeSet();
		try {
			
			if (params.term) {
								
				items = Person.withCriteria {
					or {
						ilike("firstName", "%" +  params.term + "%")
						ilike("lastName", "%" +  params.term + "%")
						ilike("email", "%" + params.term + "%")
					}
				}
			
				if (items) {
					items = items.collect() {
						[	value: it.id,
							valueText: it.firstName + " " + it.lastName,
							label: "<img src=\"/warehouse/user/viewPhoto/" + it.id + "\" width=\"24\" height=\"24\" style=\"vertical-align: bottom;\"\"/>&nbsp;" + it.firstName + " " + it.lastName + "&nbsp;&lt;" +  it.email + "&gt;",
							desc: (it?.email)?it.email:"no email",
						]
					}
				}
				else {
					def item =  [
						value: null,
						valueText : params.term,
						label: "Add new person '" + params.term + "'?",
						desc: params.term,
						icon: "none"
					];
					items.add(item)
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		
		}
		render items as JSON;
	}
	
	
	
	def findProductByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = Product.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
					ilike("upc", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						valueText: it.name,
						label: it.name,
						desc: it.description,
						icon: "none"]
				}
			}
			else {
				def item =  [
					value: null,
					valueText : params.term,
					label: "Add a new product '" + params.term + "'?",
					desc: params.term,
					icon: "none"
				];
				items.add(item)
			}
		}
		render items as JSON;
	}
	

	def findWarehouseByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = Warehouse.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						valueText: it.name,
						label: "<img src=\"/warehouse/warehouse/viewLogo/" + it.id + "\" width=\"24\" height=\"24\" style=\"vertical-align: bottom;\"\"/>&nbsp;" + it.name,
						desc: it.name,
						icon: "<img src=\"/warehouse/warehouse/viewLogo/" + it.id + "\" width=\"24\" height=\"24\" style=\"vertical-align: bottom;\"\"/>"]
				}
			}
			/*
			else {
				def item =  [
					value: 0,
					valueText : params.term,
					label: "Add a new warehouse for '" + params.term + "'?",
					desc: params.term,
					icon: "none"
				];
				items.add(item)
			}*/
		}
		render items as JSON;
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
		def product = Product.get(params.selectedItem.id)
		def recipient = Person.get(params.recipient.id);		
		def quantity = (params.quantity) ? Integer.parseInt(params.quantity) : 1;
		def shipmentItem = null;
		
		// Create a new unverified product
		if (!product) { 
			product = new Product(name: params.selectedItem.name, unverified: true).save(failOnError:true);
		}		
		if (!recipient) { 
			def name = params.recipient.name;
			
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
				shipmentItem = new ShipmentItem(product: product, quantity: quantity, serialNumber: params.serialNumber, recipient: recipient);
				container.addToShipmentItems(shipmentItem).save(flush:true);
			}
		}
		
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
				redirect(action: "editContents", id: shipmentInstance.id, params: ["container.id" : params.containerId])
			}
			else {
				flash.message = "Could not edit container"
				redirect(action: "showDetails", id: shipmentInstance.id, params: ["containerId" : params.containerId])
				//render(view: "edit", model: [containerInstance: containerInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'container.label', default: 'Container'), params.containerId])}"
			redirect(action: "showDetails", id: shipmentInstance.id, params: ["containerId" : params.containerId])
			//redirect(action: "list")
		}
	}
	
	
	
	def copyContainer = { 
		def container = Container.get(params.id);  
		def shipment = Shipment.get(params.shipmentId);   	

		if (container && shipment) { 		
			def numCopies = (params.copies) ? Integer.parseInt( params.copies ) : 1
			int index = (shipment?.containers)?(shipment.containers.size()):1;
			/*try { 
				index = Integer.parseInt(container.name);
			} catch (NumberFormatException e) {
				log.warn("The given value " + params.name + " is not an integer");
			}*/
			
			
			while ( numCopies-- > 0 ) {
				def containerCopy = new Container(container.properties);
				containerCopy.id = null;
				containerCopy.name = "" + (++index);
				containerCopy.containerType = container.containerType;
				containerCopy.weight = container.weight;
				//containerCopy.dimensions = container.dimensions;
				containerCopy.shipmentItems = null;
				containerCopy.save(flush:true);
				
				container.shipmentItems.each { 
					def shipmentItemCopy = new ShipmentItem(product: it.product, quantity: it.quantity, serialNumber: it.serialNumber, recipient: it.recipient);
					containerCopy.addToShipmentItems(shipmentItemCopy).save(flush:true);
				}    		
				shipment.addToContainers(containerCopy).save(flush:true);
			}
			flash.message = "Copied package successfully";		
		} else { 
			flash.message = "Unable to copy package";		
		}
		
		redirect(action: 'showDetails', id: params.shipmentId)
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
		render(view: "addDocument", model: [shipmentInstance : shipmentInstance, document : new Document()]);
	}
	
	def addComment = {
		log.debug params;
		def shipmentInstance = Shipment.get(params.id)
		render(view: "addComment", model: [shipmentInstance : shipmentInstance, comment : new Comment()]);
		
		//def recipient = (params.recipientId) ? User.get(params.recipientId) : null;
		//def comment = new Comment(comment: params.comment, commenter: session.user, recipient: recipient)
		//if (shipment) {
		//	shipment.addToComments(comment).save();
		//	flash.message = "Added comment '${params.comment}'to shipment $shipment.id";
		//}
		//redirect(action: 'addComment', id: params.shipmentId)
	}

	def addPackage = {		
		def shipmentInstance = Shipment.get(params.id);
		def container = new Container(
			containerType : ContainerType.findByName(params.containerType));
			
		render(view: "addPackage", model: [shipmentInstance : shipmentInstance, container : container]);

	}
	
	def savePackage = {
		def shipmentInstance = Shipment.get(params.shipmentId);
		def container = new Container(params);
		if (shipmentInstance) {
			flash.message = "Added a new ${params.containerType} to the shipment";
			//container.containerType = ContainerType.get(params.containerType.id);
			//container.name = (shipmentInstance?.containers) ? String.valueOf(shipmentInstance.containers.size() + 1) : "1";
			shipmentInstance.addToContainers(container).save(flush:true);
		}
		redirect(action: 'showDetails', id: params.shipmentId);
		
	}
	
		
	def saveComment = { 
		def shipmentInstance = Shipment.get(params.shipmentId);
		def recipient = (params.recipientId) ? User.get(params.recipientId) : null;
		def comment = new Comment(comment: params.comment, sender: session.user, recipient: recipient)
		if (shipmentInstance) { 
			shipmentInstance.addToComments(comment).save(flush:true);
			flash.message = "Added comment '${params.comment}'to shipment ${shipmentInstance.name}";			
		}
		redirect(action: 'showDetails', id: params.shipmentId);
		
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
		def shipment = Shipment.get(params.shipmentId);
		if (shipment && document) { 	    	
			shipment.removeFromDocuments(document).save(flush:true);
			document.delete();	    	    	
			flash.message = "Deleted document $params.id from shipment";		
		}
		else { 
			flash.message = "Could not remove document $params.id from shipment";
		}		
		redirect(action: 'showDetails', id: params.shipmentId)
	}
	
	def deleteEvent = {		
		def event = Event.get(params.id);
		def shipment = Shipment.get(params.shipmentId);
		if (shipment && event) {
			shipment.removeFromEvents(event).save(flush:true);
			event.delete();
			flash.message = "Deleted event $params.id from shipment";
		}
		else {
			flash.message = "Could not remove event $params.id from shipment";
		}
		redirect(action: 'showDetails', id: params.shipmentId)
	}
	
	def deleteContainer = {
		def container = Container.get(params.id);
		def shipment = Shipment.get(params.shipmentId);

		if (shipment && container) {
			container.delete();
			//shipment.removeFromContainers(container).save(flush:true);
			flash.message = "Deleted package $params.id from shipment";
		}
		else {
			flash.message = "Could not remove event $params.id from shipment";
		}

		redirect(action: 'showDetails', id: params.shipmentId)
	}
	
	def deleteItem = {
		def shipmentItem = ShipmentItem.get(params.id);
		def container = shipmentItem.getContainer();
		def shipmentId = container.getShipment().getId();
		if (item) {
			container.removeFromShipmentItems(shipmentItem)
			//item.delete();
			flash.message = "Deleted shipment item $params.id from container $container.name";
			redirect(action: 'showDetails', id: shipmentId)
		}
		else {
			flash.message = "Could not remove item $params.id from container";
			redirect(action: 'showDetails', id: shipmentId)
		}
	}
	
	def deleteComment = {
		def comment = Comment.get(params.id);
		def shipment = Shipment.get(params.shipmentId);
		if (shipment && comment) {
			shipment.removeFromComments(comment).save(flush:true);
			comment.delete();
			flash.message = "Deleted comment $comment from shipment $shipment.id";
			redirect(action: 'showDetails', id: params.shipmentId)
		}
		else {
			flash.message = "Could not remove comment $params.id from shipment";
			redirect(action: 'showDetails', id: params.shipmentId)
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

	Person convertStringToPerson(String name) { 
		def person = new Person();
		if (name) {
			def nameArray = name.split(" ");
			nameArray.each { 
				if (it.contains("@")) { 
					person.email = it;
				}
				else if (!person.firstName) { 
					person.firstName = it;
				}
				else if (!person.lastName) { 
					person.lastName = it;
				}
				else { 
					person.lastName += " " + it;
				}
			}
			/*
			if (nameArray.length == 3) {
				recipient = new Person(firstName : nameArray[0], lastName : nameArray[1], email : nameArray[2]);
			} else if (nameArray.length == 2) {
				recipient = new Person(firstName : nameArray[0], lastName : nameArray[1]);
			} else if (nameArray.length == 1) {
				recipient = new Person(firstName : nameArray[0]);
			}*/			
		}
		return person;
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

