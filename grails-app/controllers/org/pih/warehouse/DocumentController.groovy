package org.pih.warehouse


/**
 * Command object 
 */
class DocumentCommand {
   String shipmentId
   String type
   byte [] contents
}

class DocumentController {

    static allowedMethods = [index: "GET", upload: "POST", download: "GET"];

    //def scaffold = Attachment;
    
 

    /**
     * Show index page - just a redirect to the list page.
     */
	def index = {    	
		log.info "document controller index";
		redirect(action: "list", params:params)
	}
           
	
	
	/**
	 * Allow user to download the file associated with the given id.
	 */
	def download = { 
		log.error "download file id = ${params.id}";

		def document = Document.get(params.id)
        if (!document) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}";
			redirect(controller: "shipment", action: "show", id:document.getShipment().getId());    			
        }
        else {
            //redirect(action: "show", [document:document]);        	
    		log.error "document = ${document}";
    		
    		def path = "/tmp/warehouse/shipment/" + document.getShipment().getId() + "/" + document.getFilename();
    		log.error "path = ${path}";
    			
    		def file = new File(path);    
    		
    		if (file.exists()) { 
	    		response.setContentType("application/octet-stream")
	    		response.setHeader("Content-disposition", "attachment;filename=${file.getName()}")
	    		response.outputStream << file.newInputStream() 
    		}
    		else { 
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'file.label', default: 'Document File'), params.id])}";
    			redirect(controller: "shipment", action: "show", id:document.getShipment().getId());    			
    		}
        }
	}
	
	/**
	 * Upload a document to the server
	 */
    def upload = { DocumentCommand command ->
		log.info "shipmentId: $command.shipmentId"
		log.info "contents: $command.contents"

		def file = request.getFile('contents');
		def filename = file.originalFilename;
        
		// file must be less than 1MB
        if (!file?.empty && file.size < 1024*1000) {
			def shipment = Shipment.get(command.shipmentId);
			def size = file.size;
			def type = command.type;
			
			Document document = new Document(type: type, size: size, filename: filename, contents: command.contents);
			shipment.addToDocuments(document).save(flush:true);
			
			flash.message= "Successfully saved file to Shipment"
			
			File newFile = new File("/tmp/warehouse/shipment/" + command.shipmentId + "/" + filename);
			if (newFile.mkdirs())
				file.transferTo(newFile);
			
			flash.message += " and stored the file $newFile.absolutePath to the local file system";
        }
        else { 
        	
        	flash.message = "File $filename is too large (must be less than 1MB)";
        }
        
		redirect(controller: 'shipment', action: 'show', id: command.shipmentId)
    }    

}
