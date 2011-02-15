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
				def container = Container.get(params.id)	
				shipmentService.deleteContainer(container)
			}.to("enterContainerDetails")
			
			on("cancelContainer").to("enterContainerDetails")
			
			on("saveItem").to("saveItemAction")
			
			on("deleteItem"){
				def item = ShipmentItem.get(params.item.id)
				shipmentService.deleteShipmentItem(item)
			}.to("enterContainerDetails")
			
			on("cancelItem").to("enterContainerDetails")
			
			on("addBox"){
				// this parameter triggers the "Add Box" for the container to be opened on page reload 
				flash.addBox = params.id as Integer
			}.to("saveContainerAction")
			
			on("addItem") {
				// this parameter triggers the "Add Item" for the container to be opened on page reload 
				flash.addItem = params.id as Integer
			}.to("saveContainerAction")
			
			on("addAnotherItem") {
				// this parameter triggers the "Add Item" for the container to be opened on page reload
				flash.addItem = params.container.id as Integer
			}.to("saveItemAction")
    	}
    	
    	saveContainerAction {
    		action {
    			def container
				
				// fetch the existing container if this is an edit, otherwise add a container to this shipment
				if (params.id) {
					container = Container.get(params.id)
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
    				valid()
    			}	
    		}
    		
    		on("valid").to("enterContainerDetails")
    		on("invalid").to("enterContainerDetails")
    	}
    
    	saveItemAction {
    		action {
    			def item
    			
    			// fetch the existing item if this is an edit, otherwise add an item to this shipment
				if (params.item?.id) {
					item = ShipmentItem.get(params.item?.id)
				}
				else {
					def container = Container.get(params.container?.id)
					
					if (!container) {
						throw new Exception("Invaild container passed to editItem action.")
					}
					item = container.addNewItem()
				}
    			
    			bindData(item,params)
				
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
