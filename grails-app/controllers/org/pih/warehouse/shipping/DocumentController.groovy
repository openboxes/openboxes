package org.pih.warehouse.shipping;

import org.pih.warehouse.core.Document;
import org.pih.warehouse.core.DocumentType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command object 
 */
class DocumentCommand {
   String typeId
   String name
   String shipmentId
   String documentNumber
   MultipartFile fileContents
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
	   def file = command.fileContents;	   
	   def shipmentInstance = Shipment.get(command.shipmentId);	   
	   log.info "multipart file: " + file.originalFilename + " " + file.contentType + " " + file.size + " " 
	   
	   // file must not be empty and must be less than 1MB
	   if (!file?.empty && file.size < 1024*1000) {		   
		   Document document = new Document( 
			   size: file.size, 
			   name: command.name,
			   filename: file.originalFilename,
			   fileContents: command.fileContents.bytes,
			   contentType: file.contentType, 
			   documentNumber: command.documentNumber,
			   documentType:  DocumentType.get(Long.parseLong(command.typeId)));
		   
		   if (!document.hasErrors()) {			   
			   shipmentInstance.addToDocuments(document).save(flush:true)
			   log.info "saved document to shipment"
			   flash.message= "Successfully saved file to Shipment"			   
		   }
		   else {
			   log.info "Document did not save " + document.errors;
			   flash.message = "Cannot save document " + document.errors;
			   redirect(controller: "shipment", action: "addDocument", id: shipmentInstance.id,
				   model: [shipmentInstance: shipmentInstance, document : document])
		   }
	   }
	   else {
		   log.info "Document is empty or too large"		   
		   flash.message = "File is too large (must be less than 1MB)";
		   redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
	   }
	   log.info "something happened, just not sure what"
	   redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
   }
		
	
	/**
	 * Allow user to download the file associated with the given id.
	 */
	def download = { 
		log.debug "Download file with id = ${params.id}";

		def document = Document.get(params.id)
        if (!document) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'document.label', default: 'Document'), params.id])}";
			redirect(controller: "shipment", action: "showDetails", id:document.getShipment().getId());    			
        }
        else {            
    		log.info "document = ${document} ${document.contentType}";    		    					
    		response.setContentType(document.contentType)
    		response.setHeader("Content-Disposition", "attachment;filename=${document.filename}")
			response.outputStream << document.fileContents;
        }
	}
	


}
