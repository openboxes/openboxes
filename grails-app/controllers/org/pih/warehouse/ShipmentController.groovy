package org.pih.warehouse

import java.io.File;

import org.pih.warehouse.Event;
import org.pih.warehouse.ShipmentEvent;

class ShipmentController {
   
    def scaffold = Shipment
    
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
    
    def addContainer = { 
    	def shipment = Shipment.get(params.shipmentId);   	
    	def containerType = ContainerType.get(params.containerTypeId);    	
    	def name = (params.name) ? params.name : containerType.name + " " + shipment.getContainers().size()
        def container = new Container(
        	name: name, 
        	weight: 0, 
        	units: "kgs", 
        	containerType: containerType);
        shipment.addToContainers(container);
        flash.message = "Added a new container to the shipment";		
		redirect(action: 'edit', id: params.shipmentId)    
    }
    
    def deleteContainer = { 
    		
		def container = Container.get(params.id);
    	def shipmentId = container.getShipment().getId();
    	
    	if (container.getShipmentItems().size() > 0) {
    		flash.message = "Cannot delete a container that is not empty";
    		redirect(action: 'edit', id: shipmentId);    		
    	}
    	else { 
    		container.delete();	    	    	
    		redirect(action: 'edit', id: shipmentId)     		
    	}    		
    }
    

    def addItem = {     		
    	def container = Container.get(params.containerId);
    	def product = Product.get(params.productId);
    	def quantity = params.quantity;
    	def shipmentItem = new ShipmentItem(product: product, quantity: quantity);    	
    	container.addToShipmentItems(shipmentItem).save(flush:true);
    	flash.message = "Added $params.quantity units of $product.name";		
		redirect(action: 'edit', id: params.shipmentId)    	
    	
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

    
    def deleteDocument = { 
    	def document = Document.get(params.id);
   		def shipmentId = document.getShipment().getId();    	
    	if (document) { 	    	
       	    document.delete();	    	    	
        	flash.message = "Deleted document $params.id from shipment";		
	    	redirect(action: 'show', id: shipmentId) 
    	}
    	else { 
        	flash.message = "Could not remove document $params.id from shipment";		
    		redirect(action: 'show', id: shipmentId)    	
    		
    	}
    }
    
    
    def addEvent = { 
    		
    	def targetLocation = null    	
    	if (params.targetLocationId) { 
        	Location.get(params.targetLocationId)
    	}
    	
    	ShipmentEvent event = new ShipmentEvent(
    		eventType:EventType.get(params.eventTypeId), 
    		eventDate: params.eventDate, 
    		eventLocation: Location.get(params.eventLocationId),
    		targetLocation: targetLocation
    	);
    	
    	def shipment = Shipment.get(params.shipmentId);     	
    	shipment.addToEvents(event).save(flush:true);    

    	flash.message = "Added event";		
		redirect(action: 'edit', id: params.shipmentId)    	
	}    

    def deleteEvent = { 
    	def event = Event.get(params.id);
    	def shipmentId = event.getShipment().getId();    	
    	event.delete();	    	    	
    	redirect(action: 'show', id: shipmentId) 
    }
    
    
    def form = {
        [ shipments : Shipment.list() ]
    }
    
    def view = {
    	// pass through to "view shipment" page
    }
}

