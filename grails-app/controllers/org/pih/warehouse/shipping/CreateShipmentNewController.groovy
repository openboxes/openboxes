package org.pih.warehouse.shipping

import org.apache.poi.hssf.record.formula.functions.NumericFunction.OneArg;

class CreateShipmentNewController {

	// TODOS:
	// confirm that it can load up an existing shipment by id
	
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
    			flow.shipmentInstance.properties = params
    			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    			}	
    		}.to("enterTrackingDetails")
    		
    		on("save") {
    			flow.shipmentInstance.properties = params
    			
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
    			flow.shipmentInstance.properties = params
    			
    			if(flow.shipmentInstance.hasErrors() || !flow.shipmentInstance.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveShipment(flow.shipmentInstance)
    			}	
					
    		}.to("enterContainerDetails")
    		
    		on("save") {
    			flow.shipmentInstance.properties = params
    			
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
    		
    		on("addContainer") {				
    			def sortOrder = (flow.shipmentInstance?.containers) ? flow.shipmentInstance?.containers?.size()+1 : 1
				def name = sortOrder as String
				
				def container = new Container(
					name: name, 
					containerType:ContainerType.findByName(params.type), 
					shipment: flow.shipmentInstance,
					sortOrder: sortOrder
				)
				
				flow.shipmentInstance?.addToContainers(container)
						
				shipmentService.saveShipment(flow.shipmentInstance)		
			}.to("enterContainerDetails")	
    	
			on("editContainer") {
				def container = Container.get(params.id)
				
				container.properties = params
				
				if(container.hasErrors() || !container.validate()) { 
					return error()
    			}
    			else {
    				shipmentService.saveContainer(container)
    			}	
			}.to("enterContainerDetails")
			
			on("deleteContainer") {
				def container = Container.get(params.id)
				
				shipmentService.deleteContainer(container)
			}.to("enterContainerDetails")
			
			on("cancelContainer").to("enterContainerDetails")
			
		
    	}
    	
    	finish {
    		redirect(controller:"shipment", action : "listShipping")
    	}
    }
}
