package org.pih.warehouse.shipping

import org.apache.poi.hssf.record.formula.functions.NumericFunction.OneArg;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.Warehouse;

import sun.util.logging.resources.logging;

class CreateShipmentWorkflowController {
	
	ShipmentService shipmentService
	
    def index = { 
    	redirect(action:'createShipment')
    }
    
    def createShipmentFlow = {
    	
    	start {
    		action {
    			log.info("Starting shipment workflow " + params)
    			
    			// create a new shipment instance if we don't have one already
    			if (!flow.shipmentInstance) { 
    				flow.shipmentInstance = shipmentService.getShipmentInstance(params.id as Long)
    				flow.shipmentWorkflow = shipmentService.getShipmentWorkflow(flow.shipmentInstance)
    			}
				if (params.skipTo) { 
					if (params.skipTo == 'Packing')
						return enterContainerDetails()
					else if (params.skipTo == 'Details')
						return enterShipmentDetails()
					else if (params.skipTo == 'Tracking')
						return enterTrackingDetails()
					
				}
    			return success()
    		}
    		on("success").to("enterShipmentDetails")
			on("enterContainerDetails").to("enterContainerDetails")
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
				log.info ("enter container details " + params)
				[ selectedContainer : Container.get(params?.containerId)]
			}.to("enterContainerDetails")
			
    		on("next") {
				shipmentService.saveShipment(flow.shipmentInstance)	
			}.to("showDetails")
			
			on("save") {
				shipmentService.saveShipment(flow.shipmentInstance)	
			}.to("finish")
			
			on("cancel").to("finish")
			
			on("editContainer") {
				// set the container we will to edit
				flash.containerToEdit = Container.get(params.containerToEditId)
			}.to("enterContainerDetails")
			
			on("saveContainer").to("saveContainerAction")
			
			on("deleteContainer") {
				def container = Container.get(params.container.id)	
				shipmentService.deleteContainer(container)
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
			
			
			on("saveItem").to("saveItemAction")
			
			on("deleteItem"){	
				def item = ShipmentItem.get(params.item.id)
				shipmentService.deleteShipmentItem(item)
			}.to("enterContainerDetails")
			
			on("addContainer") {
				// set the container type to add
				flash.containerTypeToAdd = ContainerType.findByName(params.containerTypeToAddName)
			}.to("enterContainerDetails")
			
			on("addBoxToContainer"){
				// this parameter triggers the "Add Box" dialog for the container to be opened on page reload
				// -1 means we need to assign the id of the newly created container to add box
				flash.addBoxToContainerId = (params.container?.id) ? params.container.id as Integer : -1
			}.to("saveContainerAction")
			
			on("addItemToContainer") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload 
				// -1 means we need to assign the id of the new container to add item
				flash.addItemToContainerId = (params.container?.id) ? params.container.id as Integer : -1
			}.to("saveContainerAction")
			
			on("addItemToBox") {	
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload
				// -1 means we need to assign the id of the newly created box to to the item 
				flash.addItemToContainerId = (params.box?.id) ? params.box.id as Integer : -1
			}.to("saveBoxAction")
			
			on("addAnotherItem") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload
				flash.addItemToContainerId = params.container.id as Integer
			}.to("saveItemAction")
			
			on("moveItemToContainer") { 
				// move an item to another container
				log.info params
			}.to("enterContainerDetails")
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
    	
    	saveContainerAction {
    		action {
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
				
				// TODO: make sure that this works properly if there are errors?
				if(container.hasErrors() || !container.validate()) { 
					invalid()
    			}
    			else {
    				shipmentService.saveContainer(container)
    				
    				// save a reference to this container if we need to clone it
    				if (flash.cloneQuantity) { flash.cloneContainer = container }
    				
    				// assign the id of the container if needed
    				if (flash.addItemToContainerId == -1) { flash.addItemToContainerId = container.id }
    				if (flash.addBoxToContainerId == -1) { flash.addBoxToContainerId = container.id }
    				
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
    				def container
    				shipmentService.copyContainer(flash.cloneContainer, flash.cloneQuantity as Integer)
    			}
    		
    			valid()
    		}
    		
    		on("valid").to("enterContainerDetails")
    	}
    
     	
    	saveBoxAction {
    		action {
    			
    			// first handle the box
    			def box
				
				// fetch the existing container if this is an edit, otherwise add a container to this shipment
				if (params.box?.id) {
					box = Container.get(params.box.id)
				}
				else {
					// if not, get the container that we are adding the box to
					def container = Container.get(params.container.id)
					box = container.addNewContainer(ContainerType.findByName("Box"))
				}
				
    			bindData(box,params)
    		
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
					
				}
				
				valid()
    		}
    		
			on("valid").to("cloneContainerAction")
    		on("invalid").to("enterContainerDetails")
    	}
    	
    	saveItemAction {
    		action {
    			def item
    			
    			// if we have an item id, we are editing an existing item, so we need to fetch it
				if (params.item?.id) {
					item = ShipmentItem.get(params.item?.id)
				}
    			// otherwise, if we have a container id we are adding a new item to this container
				else {
					def container = Container.get(params.container?.id)
					
					if (!container) {
						throw new Exception("Invaild container passed to editItem action. Invalid id ${params.container?.id}.")
					}
					item = container.addNewItem()
				}
    			
    			println("the params to bind = " + params)
    			
    			bindData(item, params, ['product.name','recipient.name'])  // blacklisting names so that we don't change product name or recipient name here!
				
				// TODO: make sure that this works properly if there are errors?
				if(item.hasErrors() || !item.validate()) { 
					invalid()
    			}
    			else {
    				shipmentService.saveShipmentItem(item)
    				valid()
    			}	
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
    			redirect(controller:"shipment", action : "listShipping")
    		}
    	}
    }
	
	void bindReferenceNumbers(Shipment shipment, ShipmentWorkflow workflow, Map params) {
		// need to manually bind the reference numbers
		if (!shipment.referenceNumbers) {shipment.referenceNumbers = [] }
		for (ReferenceNumberType type in workflow.referenceNumberTypes) {
			
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
}
