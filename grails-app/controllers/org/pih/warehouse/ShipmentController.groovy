package org.pih.warehouse

import java.io.File;

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

    def addProduct = {     		
    	def shipment = Shipment.get(params.shipmentId);   	
    	def product = Product.get(params.productId);
    	def quantity = params.quantity;
    	//shipment.addToProducts(product).save(flush:true);    
    	def shipmentLineItem = new ShipmentLineItem(product: product, quantity: quantity);      	
    	shipment.addToShipmentLineItems(shipmentLineItem).save(flush:true);
    	flash.message = "Added $params.quantity units of $product.name";		
		redirect(action: 'show', id: params.shipmentId)    	
    	
    }
    
    
    def form = {
        [ shipments : Shipment.list() ]
    }
    
    def view = {
    	// pass through to "view shipment" page
    }
}

