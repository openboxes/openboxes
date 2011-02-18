package org.pih.warehouse.shipping

import org.apache.poi.hssf.record.formula.functions.NumericFunction.OneArg;

import sun.util.logging.resources.logging;

class CreateShipmentNewController {
	
	ShipmentService shipmentService
	
    def index = { 
    	redirect(action:'createShipment')
    }
    
    def createShipmentFlow = {
    	
    	start {
    		action {
    			log.info("starting create shipment workflow")
    			
    			// create a new shipment instance if we don't have one already
    			if (!flow.shipmentInstance) { 
    				flow.shipmentInstance = shipmentService.getShipmentInstance(params.id as Long)
    			}
    			return success()
    		}
    		on("success").to("enterShipmentDetails")
    	}
    		
    	enterShipmentDetails {
    		on("next") {
    			bindData(flow.shipmentInstance, params)
    			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
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
    	}
    	
    	enterTrackingDetails {
    		on("back") {
    			// TODO: figure out why this isn't working
    			// flow.shipmentInstance.properties = params
    			
    			// don't need to do validation if just going back
    		}.to("enterShipmentDetails")
    		
    		on("next") {
    			bindData(flow.shipmentInstance, params)
    			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    			}	
					
    		}.to("enterContainerDetails")
    		
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
    	}
    	
    	enterContainerDetails {
    		on("back") {
    			// TODO: figure out why this isn't working
    			// flow.shipmentInstance.properties = params
    			
    			// don't need to do validation if just going back
    		}.to("enterTrackingDetails")
    		
    		on("next") {
				shipmentService.saveShipment(flow.shipmentInstance)	
			}.to("finish")
			
			on("save") {
				shipmentService.saveShipment(flow.shipmentInstance)	
			}.to("finish")
			
			on("cancel").to("finish")
    		
			on("saveContainer").to("saveContainerAction")
			
			on("deleteContainer") {
				def container = Container.get(params.container.id)	
				shipmentService.deleteContainer(container)
			}.to("enterContainerDetails")
			
			on("saveBox").to("saveBoxAction")
			
			on("deleteBox") {		
				def box = Container.get(params.box.id)
				shipmentService.deleteContainer(box)
			}.to("enterContainerDetails")
			
			on("saveItem").to("saveItemAction")
			
			on("deleteItem"){
				
				log.error("the parameter passed is " + params.item.id)
				
				def item = ShipmentItem.get(params.item.id)
				
				log.error("the item fetched has id " + item.id + " and quantity " + item.quantity)
				
				shipmentService.deleteShipmentItem(item)
			}.to("enterContainerDetails")
			
			on("addBoxToContainer"){
				// this parameter triggers the "Add Box" dialog for the container to be opened on page reload
				// -1 means we need to assign the id of the new container to add box
				flash.addBox = (params.container?.id) ? params.container.id as Integer : -1
			}.to("saveContainerAction")
			
			on("addItemToContainer") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload 
				// -1 means we need to assign the id of the new container to add item
				flash.addItem = (params.container?.id) ? params.container.id as Integer : -1
			}.to("saveContainerAction")
			
			on("addItemToBox") {	
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reloa
				flash.addItem = params.box.id as Integer
			}.to("saveBoxAction")
			
			on("addAnotherItem") {
				// this parameter triggers the "Add Item" dialog for the container to be opened on page reload
				flash.addItem = params.container.id as Integer
			}.to("saveItemAction")
			
			on("addAnotherBox") {
				// this parameter triggers the "Add Box" dialog for the container to be opened on page reload
				flash.addBox = params.container.id as Integer
			}.to("saveBoxAction")
    	}
    	
    	saveContainerAction {
    		action {
    			def container
				
				// fetch the existing container if this is an edit, otherwise add a container to this shipment
				if (params.container?.id) {
					container = Container.get(params.container?.id)
				}
				else {
					def containerType = ContainerType.findByName(params.type)
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
    				
    				// assign the id of the container if needed
    				if (flash.addItem == -1) { flash.addItem = container.id }
    				if (flash.addBox == -1) { flash.addBox = container.id }
    				
    				valid()
    			}	
    		}
    		
    		on("valid").to("enterContainerDetails")
    		on("invalid").to("enterContainerDetails")
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
				
    			// NOTE: remember if we add fields, we need to add them here; need to white-list them since one form is populating two objects
				box.properties['name','height','width','length','weight'] = params
				
				// TODO: make sure that this works properly if there are errors?
				if(box.hasErrors() || !box.validate()) { 
					invalid()
    			}
				else {
					shipmentService.saveContainer(box)
				}
				
				// now add the item, if one has been specified
				if (params.product?.id) {
					
					def item = box.addNewItem()
					
					// NOTE: remember if we add fields, we need to add them here; need to white-list them since oen form is populating two objects
					item.properties['product','quantity','lotNumber','recipient'] = params
					
					// TODO: make sure that this works properly if there are errors?
					if(item.hasErrors() || !item.validate()) { 
						invalid()
	    			}
					else {
						shipmentService.saveShipmentItem(item)
					}
				}

				valid()
    		}
    		
    		
			on("valid").to("enterContainerDetails")
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
						throw new Exception("Invaild container passed to editItem action.")
					}
					item = container.addNewItem()
				}
    			
    			bindData(item, params)
				
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
    	
    	finish {
    		redirect(controller:"shipment", action : "listShipping")
    	}
    }
	
}
