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
		}
		catch (Exception e) {
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
    
    def upload = { DocumentUploadCommand command ->
		println "shipmentId: $command.shipmentId"
		println "contents: $command.contents"

		def file = request.getFile('contents');
		def filename = file.originalFilename;
		def shipment = Shipment.get(command.shipmentId);
		def size = file.size;
		def type = command.type;
		
		Attachment document = new Attachment(type: type, size: size, filename: filename, contents: command.contents);
		shipment.addToDocuments(document).save(flush:true);
		
		flash.message= "Successfully saved file to Shipment"
		
		File newFile = new File("/tmp/warehouse/shipment/" + command.shipmentId + "/" + filename);
		if (newFile.mkdirs())
			file.transferTo(newFile);
		
		flash.message += " and stored the file $newFile.absolutePath to the local file system";
		
		
		
		redirect(action: 'show', id: command.shipmentId)
    }
    
    def form = {
        [ shipments : Shipment.list() ]
    }
    def view = {
    	// pass through to "view shipment" page
    }
}


/**
 * Dependent object 
 */
class DocumentUploadCommand {
	   String shipmentId
	   String type
	   byte [] contents
	}

