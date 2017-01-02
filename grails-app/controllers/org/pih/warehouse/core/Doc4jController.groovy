/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core

import org.pih.warehouse.shipping.Shipment;

class Doc4jController {

	def fileService
	def documentService
	def shipmentService 
		
	def downloadLetter() {
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) { 
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}
		
		def tempFile = fileService.generateLetterAsDocx(shipmentInstance)
		def filename = "Certificate of Donation - " + shipmentInstance?.name?.trim() + ".docx"
		response.setHeader("Content-disposition", "attachment; filename='" + filename + "'");
		response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		response.outputStream << tempFile.readBytes()
	}

	def downloadLetterAsPdf() {
		
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) {
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}
		
		def filename = "Certificate of Donation - " + shipmentInstance?.name?.trim() + ".pdf"
		fileService.generateLetterAsPdf(shipmentInstance, response.outputStream)
	
		response.setHeader("Content-disposition", "attachment; filename='" + filename + "'");
		response.setContentType("application/pdf")
		return;
	}

	
	/**
	 * 
	 */
	def downloadPackingList() {
		log.info params
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) {
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}

        try {
            def filename = "Packing List - " + shipmentInstance?.name?.trim() + ".xls"
            response.setHeader("Content-disposition", "attachment;filename='"+filename+"'");
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            documentService.generatePackingList(response.outputStream, shipmentInstance)
            response.outputStream.flush()

        } catch (Exception e) {
            log.error("Unable to generate packing list XLS: " + e.message, e)
            flash.message = e.message
            redirect(controller: "shipment", action: "showDetails", id: params.id)

        }
	}
	
}
