/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping

import grails.validation.Validateable
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.springframework.web.multipart.MultipartFile

/**
 * Command object 
 */
@Validateable
class DocumentCommand {
   String name
   String typeId
   String orderId
   String productId
   String requestId
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
	 * Saves changes to document metadata (or, more specifically, saves changes to metadata--type,name,documentNumber--associated with
	 * a document without modifying the document itself--the upload method handles this)
	 */
	def save = { DocumentCommand command ->
		// fetch the existing document
		Document documentInstance = Document.get(params.documentId)
		if (!documentInstance) {
			// this should never happen, so fail hard
			throw new RuntimeException("Unable to retrieve document " + params.documentId)
		}
		
		// bind the command object to the document object ignoring the shipmentId and fileContents params (which can't change after creation)
		bindData(documentInstance, command, ['shipmentId','orderId','fileContents'])
		// manually update the document type
		documentInstance.documentType = DocumentType.get(command.typeId)
		
		if (!documentInstance.hasErrors()) {
			flash.message = "${warehouse.message(code: 'document.succesfullyUpdatedDocument.message')}"
			if (command.shipmentId) { 
				redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
			} 
			else if (command.orderId) { 				
				redirect(controller: 'order', action: 'show', id: command.orderId)
			}
		}
		else {
			if (command.shipmentId) { 
				redirect(controller: "shipment", action: "addDocument", id: command.shipmentId,
				  model: [shipmentInstance: Shipment.get(command.shipmentId), documentInstance : documentInstance])
			}
			else if (command.orderId) { 
				redirect(controller: "order", action: "addDocument", id: command.orderId,
					model: [orderInstance: Order.get(command.orderId), documentInstance : documentInstance])
  
			}
		}
	}
	
	/**
	* Upload a document to the server
	*/
   def upload = { DocumentCommand command ->
	   log.info "Uploading document: " + params	  	   
	   def file = command.fileContents;	   
	   def shipmentInstance = Shipment.get(command.shipmentId);	   
	   def orderInstance = Order.get(command.orderId);	   
	   def requestInstance = Requisition.get(command.requestId);
	   
	   log.info "multipart file: " + file.originalFilename + " " + file.contentType + " " + file.size + " " 
	   
	   // file must not be empty and must be less than 10MB
	   // FIXME The size limit needs to go somewhere
	   if (file?.isEmpty()) {
		  flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
	   } 
	   else if (file.size < 10*1024*1000) {		   
		   log.info "Creating new document ";
		   def typeId = command?.typeId?:"0"
		   Document documentInstance = new Document( 
			   size: file.size, 
			   name: command.name,
			   filename: file.originalFilename,
			   fileContents: command.fileContents.bytes,
			   contentType: file.contentType, 
			   documentNumber: command.documentNumber,
			   documentType:  DocumentType.get(typeId))
		   
		   // Check to see if there are any errors
		   if (documentInstance.validate() && !documentInstance.hasErrors()) {			   
			   log.info "Saving document " + documentInstance;
			   if (shipmentInstance) { 
				   shipmentInstance.addToDocuments(documentInstance).save(flush:true)
				   flash.message = "${warehouse.message(code: 'document.successfullySavedToShipment.message', args: [shipmentInstance?.name])}"			   
			   }
			   else if (orderInstance) { 
				   orderInstance.addToDocuments(documentInstance).save(flush:true)
				   flash.message = "${warehouse.message(code: 'document.successfullySavedToOrder.message', args: [orderInstance?.description])}"
			   }
			   else if (requestInstance) { 
				   requestInstance.addToDocuments(documentInstance).save(flush:true)
				   flash.message = "${warehouse.message(code: 'document.successfullySavedToRequest.message', args: [requestInstance?.description])}"
			   }
		   }
		   // If there are errors, we need to redisplay the document form
		   else {
			   log.info "Document did not save " + documentInstance.errors;
			   flash.message = "${warehouse.message(code: 'document.cannotSave.messagee', args: [documentInstance.errors])}"
			   if (shipmentInstance) { 
				   redirect(controller: "shipment", action: "addDocument", id: shipmentInstance.id,
					   model: [shipmentInstance: shipmentInstance, documentInstance : documentInstance])
				   return;
			   } else if (orderInstance) { 
				   redirect(controller: "order", action: "addDocument", id: orderInstance.id,
					   model: [orderInstance: orderInstance, documentInstance : documentInstance])
				   return;
			   }
			   else if (requestInstance) { 
				   redirect(controller: "requisition", action: "addDocument", id: requestInstance.id,
					   model: [requestInstance: requestInstance, documentInstance : documentInstance])
				   return;

			   }
		   }
	   }
	   else {
		   log.info "Document is too large"		   
		   flash.message = "${warehouse.message(code: 'document.documentTooLarge.message')}"
		   if (shipmentInstance) { 
			   redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
			   return;
		   }
		   else if (orderInstance) { 
			   redirect(controller: 'order', action: 'show', id: command.orderId)
			   return;
		   }
		   else if (requestInstance) { 
			   redirect(controller: 'requisition', action: 'show', id: command.requestId)
			   return;
	
		   }
	   }
	   
	   // This is, admittedly, a hack but I wanted to avoid having to add this code to each of 
	   // these controllers.
	   log.info ("Redirecting to appropriate show details page")
	   if (shipmentInstance) {
		   redirect(controller: 'shipment', action: 'showDetails', id: command.shipmentId)
		   return;
	   }
	   else if (orderInstance) {
		   redirect(controller: 'order', action: 'show', id: command.orderId)
		   return;
	   }
	   else if (requestInstance) { 
		   redirect(controller: 'requisition', action: 'show', id: command.requestId)
		   return;

	   }

   }
		
	
	/**
	 * Allow user to download the file associated with the given id.
	 */
	def download = { 
		log.debug "Download file with id = ${params.id}";

		def document = Document.get(params.id)
        if (!document) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'document.label', default: 'Document'), params.id])}"
			redirect(controller: "shipment", action: "showDetails", id:document.getShipment().getId());
        }
        else {            
    		log.info "document = ${document} ${document.contentType}";    		    					
    		response.setContentType(document.contentType)
    		response.setHeader("Content-Disposition", "attachment;filename='${document.filename}'")
			response.outputStream << document.fileContents;
        }
	}
	


}
