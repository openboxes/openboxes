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

import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.MailService;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.RoleType;
import org.pih.warehouse.core.User;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.inventory.TransactionException
import org.pih.warehouse.report.ReportService

class CreateShipmentWorkflowController {
	
	def mailService
	def reportService
	def shipmentService
	def inventoryService
	def userService
	
    def index = { 
		log.info "CreateShipmentWorkflowController.index() -> " + params
		flash.type = params.type
    	redirect(action:'createShipment')
    }
    
    def createShipmentFlow = {
    	
    	start {
    		action {
    			log.info("Starting shipment workflow " + params)
    			// create a new shipment instance if we don't have one already
    			if (!flow.shipmentInstance) { 
    				flow.shipmentInstance = shipmentService.getShipmentInstance(params.id)
    				flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(flow.shipmentInstance)
    			}
				if (params.skipTo) { 
					if (params.skipTo == 'Packing')
						return enterContainerDetails()
					else if (params.skipTo == 'Details')
						return enterShipmentDetails()
					else if (params.skipTo == 'Tracking')
						return enterTrackingDetails()
					else if (params.skipTo == 'Sending')
						return sendShipment()
				}
    			return success()
    		}
    		on("success").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("enterTrackingDetails").to("enterTrackingDetails")
			on("enterContainerDetails").to("enterContainerDetails")
			on("sendShipment").to("sendShipment")
    	}
    		
    	enterShipmentDetails {
    		on("next") {
    			bindData(flow.shipmentInstance, params)
    		    				
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    				
    				// set (or reset) the shipment workflow here, since the shipment type may have change
    				flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(flow.shipmentInstance)
    			}	
    		}.to("enterTrackingDetails")
    		
    		on("save") {
    			bindData(flow.shipmentInstance, params)
    			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    			}	
    		}.to("finish")
    		
    		on("cancel").to("finish")
    		
			on("addLocation") {
				flash.addLocation = true
				log.info("locationInstance.hashCode " + flash.locationInstance?.hashCode())
				if (!flash.locationInstance) {
					flash.locationInstance = new Location();
				}
			}.to("enterShipmentDetails")
			on("saveLocation").to("saveLocationAction")

			
			// for the top-level links
    		on("enterShipmentDetails").to("enterShipmentDetails")
			on("enterTrackingDetails").to("enterTrackingDetails")
			on("enterContainerDetails").to("enterContainerDetails")
			on("reviewShipment").to("reviewShipment")
			on("sendShipment").to("sendShipment")
    	}
    	
    	enterTrackingDetails {
    		on("back") {
    			// TODO: figure out why this isn't working
    			// flow.shipmentInstance.properties = params
    			
    			// don't need to do validation if just going back
    		}.to("enterShipmentDetails")
    		
    		on("next") {
    			bindData(flow.shipmentInstance, params)
    			
    			// need to manually bind the reference numbers and shipper
     			bindReferenceNumbers(flow.shipmentInstance, flow.shipmentWorkflow, params)
     			bindShipper(flow.shipmentInstance, params)
    			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    			}	
					
    		}.to("enterContainerDetails")
    		
    		on("save") {
    			bindData(flow.shipmentInstance, params)
    			
    			// need to manually bind the reference numbers and shipper
     			bindReferenceNumbers(flow.shipmentInstance, flow.shipmentWorkflow, params)
     			bindShipper(flow.shipmentInstance, params)
     			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    			}	
    			
    		}.to("finish")
						
			on("addPerson") {
				flash.addPerson = true
				if (!flash.personInstance) { 
					flash.personInstance = new Person();
				}
			}.to("enterTrackingDetails")
			
			on("addShipper") {
				flash.addShipper = true				
				if (!flash.shipperInstance) { 
					flash.shipperInstance = new Shipper()
				}
			}.to("enterTrackingDetails")
			

			on("savePerson").to("savePersonAction")
			on("saveShipper").to("saveShipperAction")
			
    		on("cancel").to("finish")
    		
    		// for the top-level links
    		on("enterShipmentDetails").to("enterShipmentDetails")
			on("enterTrackingDetails").to("enterTrackingDetails")
			on("enterContainerDetails").to("enterContainerDetails")
			on("reviewShipment").to("reviewShipment")
			on("sendShipment").to("sendShipment")
    	}
    	
    	enterContainerDetails {
    		on("back") {
    			// TODO: figure out why this isn't working
    			// flow.shipmentInstance.properties = params
    			
    			// don't need to do validation if just going back
    		}.to("enterTrackingDetails")
    		
			on("enterContainerDetails") { 
				log.info ("Enter container details " + params)
				def selectedContainer = Container.get(params?.containerId)
				if (params.direction) { 
					def containerList = new ArrayList(flow.shipmentInstance.containers)
					def sortOrder = selectedContainer ? selectedContainer.sortOrder : -1
					
					def index = (sortOrder + Integer.parseInt(params.direction))
					log.info "current = " + sortOrder + ", nextIndex " + index
					selectedContainer = containerList.find { it.sortOrder == index }
				}
				
				[ selectedContainer : selectedContainer ]
			}.to("enterContainerDetails")
			
    		on("next") {
				shipmentService.saveShipment(flow.shipmentInstance)	
			}.to("sendShipment")	// formerly showDetails
			
			on("save") {
				shipmentService.saveShipment(flow.shipmentInstance)	
			}.to("finish")
			
			on("cancel").to("finish")
			
			on("editContainer") {
				// set the container we will to edit
				flash.containerToEdit = Container.get(params.containerToEditId)
			}.to("enterContainerDetails")

			on("moveContainer") {
				// set the container we will to edit
				def location = Location.get(session.warehouse.id)
				flash.containerToMove = Container.get(params.containerToMoveId)
				def shipments = shipmentService.getOutgoingShipments(location)
				flash.shipments = shipments - flow.shipmentInstance 
				
			}.to("enterContainerDetails")

			on("saveContainer").to("saveContainerAction")
			
			on("deleteContainer") {
				def container = Container.get(params.container.id)	
				shipmentService.deleteContainer(container)
				flow.selectedContainer = null;
				
			}.to("enterContainerDetails")
			
			on("cloneContainer") {
				flash.cloneQuantity = params.cloneQuantity 
			}.to("saveContainerAction")
			
			on("editBox") {
				// set the box we will to edit
				flash.boxToEdit = Container.get(params.boxToEditId)
			}.to("enterContainerDetails")
			
			on("saveBox").to("saveBoxAction")
			
			on("deleteBox") {		
				def box = Container.get(params.box.id)
				shipmentService.deleteContainer(box)
			}.to("enterContainerDetails")
			
			on("cloneBox") {
				flash.cloneQuantity = params.cloneQuantity  
			}.to("saveBoxAction")
			
			on("editItem") {
				// set the item we will to edit
				flash.itemToEdit = ShipmentItem.get(params.itemToEditId)
			}.to("enterContainerDetails")
			
			on("moveItem") {
				// set the item we will to edit
				flash.itemToMove = ShipmentItem.get(params.itemToMoveId)
			}.to("enterContainerDetails")

			on("saveItem").to("saveItemAction")
			
			on("updateItem").to("updateItemAction")
			
			on("deleteItem"){	
				def item = ShipmentItem.get(params.item.id)
				shipmentService.deleteShipmentItem(item)
				
			}.to("enterContainerDetails")
			
			on("addContainer") {
				// set the container type to add
				flash.containerTypeToAdd = ContainerType.findById(params.containerTypeToAddId)
			}.to("enterContainerDetails")
			
			on("addBoxToContainer"){
				// this parameter triggers the "Add Box" dialog for the container to be opened on page reload
				// -1 means we need to assign the id of the newly created container to add box
				flash.addBoxToContainerId = (params.container?.id) ? params.container.id : -1
			}.to("saveContainerAction")
			
			on("addItemToContainer") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload 
				// -1 means we need to assign the id of the new container to add item
				flash.addItemToContainerId = (params.container?.id) ? params.container.id : -1
			}.to("saveContainerAction")

			on("addItemToShipment") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload
				// -1 means we need to assign the id of the new container to add item
				flash.addItemToShipmentId = (params.container?.id) ? params.container.id : -1
			}.to("enterContainerDetails")
						
			on("addItemToBox") {	
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload
				// -1 means we need to assign the id of the newly created box to to the item 
				flash.addItemToContainerId = (params.box?.id) ? params.box.id : -1
			}.to("saveBoxAction")
			
			on("addAnotherItem") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload
				flash.addItemToContainerId = params.container.id
			}.to("saveItemAction")
			
			on("moveItemToContainer").to("moveItemAction")
			on("moveContainerToShipment").to("moveContainerAction")
			/**
			on("addAnotherBox") {
				// this parameter triggers the "Add Box" dialog for the container to be opened on page reload
				flash.addBoxToContainerId = params.container.id as Integer
			}.to("saveBoxAction")
			*/
			
			// for the top-level links
    		on("enterShipmentDetails").to("enterShipmentDetails")
			on("enterTrackingDetails").to("enterTrackingDetails")
			on("enterContainerDetails").to("enterContainerDetails")
			on("reviewShipment").to("reviewShipment")
			on("sendShipment").to("sendShipment")
    	}
		
		sendShipment {
						
			// All transition buttons on the bottom of the page
			on("back") {

			}.to("enterContainerDetails")
			
			on("next").to("sendShipmentAction")
				
			on("save") {
				shipmentService.saveShipment(flow.shipmentInstance)
			}.to("finish")

			on("cancel").to("finish")
			on("success").to("finish")
			
			// Top-level navigation transitions
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("enterTrackingDetails").to("enterTrackingDetails")
			on("enterContainerDetails").to("enterContainerDetails")
			on("sendShipment").to("sendShipment")
			
			
		}
		
		sendShipmentAction { 
			action { SendShipmentCommand command ->
				println "Send shipment " + params
				
				flow.command = command
				
				if (!command.validate()) {
					log.info command.errors
					return error();
				}

				
				def transactionInstance
				def userInstance = User.get(session.user.id)
				def shipmentInstance = Shipment.get(params.id)
				def shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)
		
				// This probably shouldn't occur, but we can leave it for now
				if (!shipmentInstance) {
					flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), params.id])}"
					redirect(action: "list", params:[type: params.type])
				}
				else {
					// handle a submit
					if ("POST".equalsIgnoreCase(request.getMethod())) {						
						// create the list of email recipients
						def emailRecipients = new HashSet()
						if (params.emailRecipientId) {
							def recipientIds = params.list("emailRecipientId")
							recipientIds.each { recipientId ->
								def recipient = Person.get(recipientId)
								if (recipient && recipient.email)
									emailRecipients.add(recipient)
							}
						}
										
						try {
							// send the shipment
							shipmentService.sendShipment(shipmentInstance, command.comments, session.user, session.warehouse,
															command.actualShippingDate, command.debitStockOnSend);
							triggerSendShipmentEmails(shipmentInstance, userInstance, emailRecipients)
							
						}
						catch (TransactionException e) {
							command.transaction = e.transaction
							//shipmentInstance = Shipment.get(params.id)
							//shipmentWorkflow = shipmentService.getShipmentWorkflow(params.id)
							return error()
						}
										
						if (!shipmentInstance?.hasErrors() && !transactionInstance?.hasErrors()) {
							//flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'shipment.label', default: 'Shipment'), shipmentInstance.id])}"
							//redirect(controller: 'shipment', action: "showDetails", id: shipmentInstance?.id)
							command.shipment = shipmentInstance
							command.transaction = transactionInstance
							return success()
							
						}
						else { 
							return error();
						}
					}
				}				
			}
			
			on("success").to("finish")
			on("error").to("sendShipment")
			
		}
		
		
    	saveContainerAction {
    		action {
				log.info("Save container " + params)
				
    			def container
				
				// fetch the existing container if this is an edit, otherwise add a container to this shipment
				if (params.container?.id) {
					container = Container.get(params.container?.id)
				}
				else {
					def containerType = ContainerType.get(params.containerTypeToAddId)
					if (!containerType) {
						throw new Exception("Invaild container type passed to editContainer action.")
					}
					
					container = flow.shipmentInstance.addNewContainer(containerType)
				}
				
				bindData(container,params)
				
				log.info("Container recipient " + container.recipient)
				
				// TODO: make sure that this works properly if there are errors?
				if(container.hasErrors() || !container.validate()) { 
					invalid()
    			}
    			else {
					log.info "# containers: " + flow?.shipmentInstance?.containers?.size()
					shipmentService.saveContainer(container)
					
    				// save a reference to this container if we need to clone it
    				if (flash.cloneQuantity) { flash.cloneContainer = container }
    				
    				// assign the id of the container if needed
    				if (flash.addItemToContainerId == -1) { flash.addItemToContainerId = container.id }
    				if (flash.addBoxToContainerId == -1) { flash.addBoxToContainerId = container.id }
    				
					// used to refocus page with the appropriate container
					flash.selectedContainer = container

    				valid()
    			}	
    		}
			
			
    		on("valid").to("cloneContainerAction")
    		on("invalid").to("enterContainerDetails")
    	}
    	
		cloneContainerAction {
			action {

				// see if we have to make copies of this container
				if (flash.cloneQuantity && flash.cloneContainer) {
					shipmentService.copyContainer(flash.cloneContainer, flash.cloneQuantity as Integer)
				}

				valid()
			}

			on("valid").to("enterContainerDetails")
		}
		saveLocationAction { 
			 action { 
				 def locationInstance
				 log.info "saveLocationAction: " + params				 
				 if (flash.locationInstance) { 
					 locationInstance.properties = params 
				 }
				 else { 
					 locationInstance = new Location(params)					 
				 }
				 
				 //flash.locationInstance = locationInstance;
				 
				 def locations = Location.findAll(locationInstance);
				 //flash.message 
				 log.info "saveLocationAction: found " + locations?.size() + " locations"  
				 if (locations) { 
					 flash.message = "${warehouse.message(code:'location.alreadyExists.message', args:[locationInstance.name])}"
					 invalid()
				 }
				 else {		
					 
					 if (locationInstance.save(flush:true) && !locationInstance.hasErrors()) {  
						 log.info "saved location " + locationInstance + " with id " + locationInstance?.id
						 flash.message = "${warehouse.message(code:'location.created.message', args:[locationInstance.name])}"
						 valid()
					 }
					 else { 							 
						 log.info "invalid location " + locationInstance.errors
						 flash.message = "${warehouse.message(code:'location.invalid.message', args:[locationInstance.name])}"
						 flash.addLocation = true
						 flash.locationInstance = locationInstance
						 invalid()				 
					 }
				 }
			 }
			 on("valid").to("enterShipmentDetails")
			 on("invalid").to("enterShipmentDetails")
			 
		 }
		 savePersonAction {
			 action {
				 log.info "savePersonAction: " + params
				 
				 def personInstance = new Person(params)
				 flash.personInstance = personInstance;
				 
				 def persons = Person.findAll(personInstance);
				 flash.message
				 log.info "savePersonAction: found " + persons?.size() + " persons"
				 if (persons) {
					 flash.message = "${warehouse.message(code:'person.alreadyExists.message', args:[personInstance.firstName, personInstance.lastName])}"
					 invalid()
				 }
				 else {
					 log.info "validate person"
					 if (!personInstance.validate()) {
						 log.info "invalid person " + personInstance.errors
						 flash.message = "${warehouse.message(code:'person.invalid.message', args:[personInstance.firstName, personInstance.lastName])}"
						 invalid()
					 }
					 else {
						 
						 if (personInstance.save(flush:true) && !personInstance.hasErrors()) {
							 log.info "saved person " + personInstance + " with id " + personInstance?.id
							 flash.message = "${warehouse.message(code:'person.created.message', args:[personInstance.firstName, personInstance.lastName])}"
							 valid()
						 }
						 else {
							 log.info "invalid person " + personInstance.errors
							 flash.message = "${warehouse.message(code:'person.invalid.message', args:[personInstance.firstName, personInstance.lastName])}"
							 invalid()
						 }
					 }
				 }
				 
			 }
			 on("valid").to("enterTrackingDetails")
			 on("invalid").to("enterTrackingDetails")
			 
		 }
		 saveShipperAction {
			 action {
				 log.info "saveShipperAction: " + params
				 
				 def shipperInstance = new Shipper(params)
				 flash.shipperInstance = shipperInstance;
				 
				 def shippers = Shipper.findAll(shipperInstance);
				 flash.message
				 log.info "saveShipperAction: found " + shippers?.size() + " persons"
				 if (shippers) {
					 flash.message = "${warehouse.message(code:'shipper.alreadyExists.message', args:[shipperInstance?.name])}"
					 invalid()
				 }
				 else {
					 log.info "validate shipper"
					 if (!shipperInstance.validate()) {
						 log.info "invalid person " + shipperInstance.errors
						 flash.message = "${warehouse.message(code:'shipper.invalid.message', args:[shipperInstance?.name])}"
						 invalid()
					 }
					 else {
						 
						 if (shipperInstance.save(flush:true) && !shipperInstance.hasErrors()) {
							 log.info "saved shipper " + shipperInstance + " with id " + shipperInstance?.id
							 flash.message = "${warehouse.message(code:'shipper.created.message', args:[shipperInstance?.name])}"
							 valid()
						 }
						 else {
							 log.info "invalid shipper " + shipperInstance.errors
							 flash.message = "${warehouse.message(code:'shipper.invalid.message', args:[shipperInstance?.name])}"
							 invalid()
						 }
					 }
				 }
				 
			 }
			 on("valid").to("enterTrackingDetails")
			 on("invalid").to("enterTrackingDetails")
			 
		 }
		     
     	
    	saveBoxAction {
    		action {
    			
    			// first handle the box
    			def box
				def container 
				
				// fetch the existing container if this is an edit, otherwise add a container to this shipment
				if (params.box?.id) {
					box = Container.get(params.box.id)
				}
				else {
					// if not, get the container that we are adding the box to
					container = Container.get(params.container.id)
					box = container.addNewContainer(ContainerType.findById(Constants.BOX_CONTAINER_TYPE_ID))
				}
				
    			bindData(box,params)
    						
				log.info("setting recipient ...");
				// If a recipient is not specified, we should specify one
				if (!box?.recipient) {
					box.recipient = container?.recipient
				}
				
				// TODO: make sure that this works properly if there are errors?
				if(box.hasErrors() || !box.validate()) { 
					invalid()
    			}
				else {
					shipmentService.saveContainer(box)
					
					// save a reference to this box if we need to clone it
					if (flash.cloneQuantity) { flash.cloneContainer = box }
					
					// assign the id of the box if needed
    				if (flash.addItemToContainerId == -1) { flash.addItemToContainerId = box.id }
					
					// used to refocus page with the appropriate container
					flash.selectedContainer = box
					
				}
				
				valid()
    		}
    		
			on("valid").to("cloneContainerAction")
    		on("invalid").to("enterContainerDetails")
    	}
		
		moveItemAction {
			action {

				// move an item to another container
				log.info "Move item to container " + params

                def shipment = flow.shipmentInstance
                def item = shipment.shipmentItems.find {it.id == params.item.id };

                if (item) {
                    def destinations = makeDestinationMap(item, params)

                    if (shipmentService.moveItem(item, destinations)) {

						if (!shipment.hasErrors() ) {
                            shipment.save(flush:true)
						} else {
							throw new RuntimeException("shipment has errors " + shipment.errors)
						}

                        valid()
                    } else {
                        invalid()
                    }
                }
                else {
                    invalid()
                }
			}
			
			on("valid").to("enterContainerDetails")
			on("invalid").to("enterContainerDetails")
		}
		moveContainerAction {
			action {				
				// move an item to another container
				log.info "Move container to another shipment " + params
				def container = Container.get(params.container.id);
				def oldShipment = container.shipment
				log.info "Old shipment " + oldShipment.id
				def newShipment = Shipment.get(params.shipment.id)
				
				try { 					
					shipmentService.moveContainer(container, newShipment)
					
					// Shipment in the flow scope does not refresh automatically
					flow.shipmentInstance.refresh()
					
					
				} 
				catch (UnsupportedOperationException e) { 
					flash.message = "${warehouse.message(code: 'default.unsupportedOperation.message')}"
					error()
				}
				log.info "Old shipment " + oldShipment.id
				//flash.shipmentInstance = Shipment.get(oldShipment.id)
				
			}
			on("success").to("enterContainerDetails")
			on("error").to("enterContainerDetails")
		}
				
		saveItemAction { 
			action { 
				try {
					log.info "save item action: " + params
					def shipment = flow?.shipmentInstance
					def container = Container.get(params?.container?.id)
					//def product = Product.get(params?.product?.id)
					def inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
					if (!inventoryItem) { 
						inventoryItem = new InventoryItem(params)
						inventoryItem = inventoryService.findOrCreateInventoryItem(inventoryItem)
					}					
					
					// Create a new shipment item
					// FIXME product: product, lotNumber: params.lotNumber
					def shipmentItem = new ShipmentItem(shipment: shipment, container: container, inventoryItem: inventoryItem)
					
					// FIXME Property [shipment] of class [class org.pih.warehouse.shipping.ShipmentItem] cannot be null
					//shipmentItem.shipment = flow.shipmentInstance
					
					// Bind the form parameters to the shipment item 
					// blacklisting names so that we don't change product name or recipient name here!
					bindData(shipmentItem, params, ['product.name', 'recipient.name'])  
						
					// If a recipient is not specified, the shipment item should inherit the recipient from the parent container
					if (!shipmentItem?.recipient) {
						shipmentItem.recipient = container?.recipient
					}
					//shipmentItem.shipment = flow.shipmentInstance;
					
					// In case there are errors, we use this flow-scoped variable to display errors to user
					flow.itemInstance = shipmentItem;
					
					// Add shipment item if this is an incoming shipment (bypass validation so that we don't check against on-hand quantity)
					if (shipment?.destination?.id == session?.warehouse?.id || shipmentService.validateShipmentItem(shipmentItem)) {	
												
						// Add shipment item to shipment
						shipment.addToShipmentItems(shipmentItem);
						
						// Need to validate shipment item before adding it to the shipment
						shipmentService.saveShipment(shipment)
						valid()
					}

				} catch (RuntimeException e) {					
					log.error("Error saving shipment item ", e)
					// Need to instantiate an item instance (if it doesn't exist) so we can add errors to it
					if (!flow.itemInstance) 
						flow.itemInstance = new ShipmentItem();
					
					// If there are no errors already (added from the save or
					// validation method, then we should add the generic error message from the exception)
					flow.itemInstance.errors.reject(e.message)

						
					invalid();
				}
			}
			on("valid").to("enterContainerDetails")
			on("invalid").to("enterContainerDetails")
		}
				
    	updateItemAction {
    		action {
				try { 
					log.info "update existing item: " + params

	    			// Updating an existing shipment item 
					def shipmentItem = ShipmentItem.get(params.item?.id)

					// Bind the parameters to the item instance
					bindData(shipmentItem, params, ['product.name','recipient.name'])  // blacklisting names so that we don't change product name or recipient name here!
						
					// Update the inventory item associated with this shipment item										
					def inventoryItem = InventoryItem.get(params?.inventoryItem?.id)
					if (!inventoryItem) {
						inventoryItem = new InventoryItem(params)
						inventoryItem = inventoryService.findOrCreateInventoryItem(inventoryItem)
					}
					shipmentItem.inventoryItem = inventoryItem

					
					// In case there are errors, we use this flow-scoped variable to display errors to user
					flow.itemInstance = shipmentItem;
					
					// Validate shipment item
					shipmentItem.shipment = flow.shipmentInstance;
					if (flow?.shipmentInstance?.destination?.id == session?.warehouse?.id || shipmentService.validateShipmentItem(shipmentItem)) {
						if (!shipmentItem.id) { 
							log.info ("saving new shipment item")
							// Need to validate shipment item before adding it to the shipment
							flow.shipmentInstance.addToShipmentItems(shipmentItem);
							shipmentService.saveShipment(flow.shipmentInstance)
						}
						else { 					
							log.info ("saving existing shipment item")
							shipmentService.saveShipmentItem(shipmentItem)
						}
						valid()
					}

				} catch (RuntimeException e) {
					log.error("Error saving shipment item ", e)
					// Need to instantiate an item instance (if it doesn't exist) so we can add errors to it
					if (!flow.itemInstance) flow.itemInstance = new ShipmentItem();
					flow.itemInstance.errors.reject(e.getMessage())
				}
				invalid();
    		}
    		
    		on("valid").to("enterContainerDetails")
    		on("invalid").to("enterContainerDetails")
    	}
    	
    	showDetails {
    		redirect(controller:"shipment", action : "showDetails", params : [ "id" : flow.shipmentInstance.id ?: '' ])
    	}
    	
    	finish {
    		if (flow.shipmentInstance.id) {
    			redirect(controller:"shipment", action : "showDetails", params : [ "id" : flow.shipmentInstance.id ?: '' ])
    		}
    		else {
    			redirect(controller:"shipment", action : "list")
    		}
    	}
    }



    void bindReferenceNumbers(Shipment shipment, ShipmentWorkflow workflow, Map params) {
		// need to manually bind the reference numbers
		if (!shipment.referenceNumbers) {shipment.referenceNumbers = [] }
		for (ReferenceNumberType type in workflow?.referenceNumberTypes) {
			
			// find the reference number for this reference number type
			ReferenceNumber referenceNumber = shipment.referenceNumbers.find( {it.referenceNumberType.id == type.id} )	
			
			if (params.referenceNumbersInput?."${type.id}") {
				// check to see if this reference value already exists
				if (referenceNumber) {
					// if it exists, assign the new id
					referenceNumber.identifier = params.referenceNumbersInput?."${type.id}"
				}
				else {
					// otherwise, we need to add a new reference number
					shipment.referenceNumbers.add(new ReferenceNumber( [identifier: params.referenceNumbersInput?."${type.id}",
																		referenceNumberType: type]))
				}
			}
			else {
				// if there is no param for this reference number, we need to remove it from the list of reference numbers
				if (referenceNumber) {
					shipment.referenceNumbers.remove(referenceNumber)
				}
			}
		}
	}
	
	void bindShipper(Shipment shipment, Map params) {		
		// need to manually bind the shipper since it is nested within the "shipmentMethod"
		if (params.shipperInput) {	
			if (!shipment.shipmentMethod) {
				// create the new ShipmentMethod object if need be
				shipment.shipmentMethod = new ShipmentMethod()
			}
			shipment.shipmentMethod.shipper = Shipper.get(params.shipperInput.id)
		}
		else {
			// if there is no input for shipper, we remove the *entire* shipment method
			// TODO: does this delete the underlying shipment method upon saving?		
			shipment.shipmentMethod = null   
		}		
	}
	
	/**
	 *
	 * @param shipmentInstance
	 * @param userInstance
	 * @param recipients
	 */
	void triggerSendShipmentEmails(Shipment shipmentInstance, User userInstance, Set<Person> recipients) {
		if (!recipients) recipients = new HashSet<Person>()
		
		// Add all admins to the email
		def adminList = userService.findUsersByRoleType(RoleType.ROLE_ADMIN)
		adminList.each { adminUser -> 
			if (adminUser?.email) {
				log.info("Adding email " + adminUser?.email)
				recipients.add(adminUser);	
			}
		}
		
		// add the current user to the list of email recipients
		if (userInstance) {
			recipients.add(userInstance)
		}

				
	   if (!shipmentInstance.hasErrors()) {
		   
		   log.info("Create shipment flow " + createShipmentFlow)
		   if (!userInstance) userInstance = User.get(session.user.id)
		   def shipmentName = "${shipmentInstance.name}"
		   def shipmentType = "${format.metadata(obj:shipmentInstance.shipmentType)}"
		   def shipmentDate = "${formatDate(date:shipmentInstance?.actualShippingDate, format: 'MMMMM dd yyyy')}"
		   def subject = "${warehouse.message(code:'shipment.hasBeenShipped.message',args:[shipmentType, shipmentName, shipmentDate])}"
		   def body = g.render(template:"/email/shipmentShipped", model:[shipmentInstance:shipmentInstance, userInstance:userInstance])
		   def toList = recipients?.collect { it?.email }?.unique()
		   log.info("Mailing shipment emails to ${toList} ")
		   		  
		   try {
			   mailService.sendHtmlMail(subject, body.toString(), toList)
		   } catch (Exception e) {
			   log.error "Error triggering send shipment emails " + e.message
		   }
	   }
   }

    static protected Map makeDestinationMap(ShipmentItem item, Map params) {

        def shipment = item.shipment;
        def containerIds = shipment.containers.collect { it.id }
        if(item.container) {
            containerIds.remove(item.container.id)
            containerIds << "0";
        }

        def destinations = [:]
        containerIds.each { containerId ->
            def quantityFromForm = params["quantity-" + containerId]
            def quantity = quantityFromForm ? quantityFromForm as Integer : 0

            if (quantity > 0) {
                destinations[containerId] = quantity;
            }
        }
        return destinations
    }
	
}
