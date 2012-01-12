package org.pih.warehouse.core

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;

import java.io.File;
import java.io.FileOutputStream;


import org.pih.warehouse.shipping.Shipment;

class Doc4jController {

	def fileService
	def documentService
	def shipmentService 
		
	def downloadLetter = { 
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) { 
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}
		
		def tempFile = fileService.generateLetterAsDocx(shipmentInstance)
		def filename = shipmentInstance?.name + " - Certificate of Donation.docx"
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
		response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		response.outputStream << tempFile.readBytes()
	}

	def downloadLetterAsPdf = { 	
		
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) {
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}
		
		def filename = shipmentInstance?.name + " - Certificate of Donation.pdf"
		fileService.generateLetterAsPdf(shipmentInstance, response.outputStream)
	
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
		response.setContentType("application/pdf")
		//response.outputStream = outputStream;
		return;
	}

	
	/**
	 * 
	 */
	def downloadPackingList = { 
		log.info params
		def shipmentInstance = Shipment.get(params.id);
		
		if (!shipmentInstance) {
			throw new Exception("Unable to locate shipment with ID ${params.id}")
		}

		// For some reason, this needs to be here or we get a File Not Found error (ERR_FILE_NOT_FOUND)
		render ""
		
		def filename = shipmentInstance?.name + " - Packing List.xls"
		log.info ("filename " + filename )
		response.setHeader("Content-disposition", "attachment; filename=" + filename);
		response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		documentService.generatePackingList(response.outputStream, shipmentInstance)
		//response.outputStream << tempFile.readBytes()
		return;

	}
	
}
