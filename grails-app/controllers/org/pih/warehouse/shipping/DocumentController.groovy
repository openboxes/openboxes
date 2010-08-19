package org.pih.warehouse.shipping;


/**
 * Command object 
 */
class DocumentCommand {
   String typeId
   String shipmentId
   String documentNumber
   byte [] fileContents
}

class DocumentController {

    static allowedMethods = [index: "GET", upload: "POST", download: "GET"];

    //def scaffold = Document;
    
 

    /**
     * Show index page - just a redirect to the list page.
     */
	def index = {    	
		log.info "document controller index";
		redirect(action: "list", params:params)
	}
           

	/**
	* Upload a document to the server
	*/
   def upload = { DocumentCommand command ->
	   log.info "upload document: " + params
	   def file = request.getFile('fileContents');
	   def filename = file.originalFilename;
	   def shipmentInstance = Shipment.get(command.shipmentId);
	   def documentNumber = params.documentNumber;
	   
	   log.info "file: " + file
	   
	   // file must be less than 1MB
	   if (!file?.empty && file.size < 1024*1000) {		   
		   def fileSize = file.size;
		   def documentType = DocumentType.get(Long.parseLong(command.typeId));
		   
		   Document document = new Document(documentType: documentType, size: fileSize, 
			   filename: filename, contents: command.fileContents, documentNumber: command.documentNumber);
		   
		   if (!document.hasErrors()) {			   
			   shipmentInstance.addToDocuments(document).save(flush:true)
			   log.info "saved document to shipment"
			   flash.message= "Successfully saved file to Shipment"			   
			   File newFile = new File("/tmp/warehouse/shipment/" + command.shipmentId + "/" + filename);
			   newFile.mkdirs();
			   file.transferTo(newFile);			   
			   flash.message += " and stored the file $newFile.absolutePath to the local file system";			   
			   //redirect(action: "showDetails", id: shipmentInstance.id)
		   }
		   else {
			   log.info "did not save document " + document.errors;
			   log.info "not sure why what the fuck"
			   flash.message = "Cannot save document " + document.errors;
			   redirect(controller: "shipment", action: "addDocument", id: shipmentInstance.id,
				   model: [shipmentInstance: shipmentInstance, document : document])
		   }
	   }
	   else {
		   log.info "document is empty or too large"		   
		   flash.message = "File $filename is too large (must be less than 1MB)";
		   redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
	   }
	   log.info "something happened, just not sure what"
	   redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
   }
		
	
	/**
	 * Allow user to download the file associated with the given id.
	 */
	def download = { 
		log.debug "download file id = ${params.id}";

		def document = Document.get(params.id)
        if (!document) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}";
			redirect(controller: "shipment", action: "showDetails", id:document.getShipment().getId());    			
        }
        else {            
    		log.debug "document = ${document}";    		
    		def path = "/tmp/warehouse/shipment/" + document.getShipment().getId() + "/" + document.getFilename();
    		log.info "uploaded file path = ${path}";
    			
    		def file = new File(path);    
    		
    		if (file.exists()) { 
	    		response.setContentType("application/octet-stream")
	    		response.setHeader("Content-disposition", "attachment;filename=${file.getName()}")
	    		response.outputStream << file.newInputStream() 
    		}
    		else { 
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'file.label', default: 'Document File'), params.id])}";
    			redirect(controller: "shipment", action: "showDetails", id:document.getShipment().getId());    			
    		}
        }
	}
	


}
