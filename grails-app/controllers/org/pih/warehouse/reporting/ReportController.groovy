/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.reporting

import org.pih.warehouse.report.ChecklistReportCommand
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.report.ProductReportCommand

class ReportController {
	
	def documentService
	def inventoryService
	def productService
	def reportService
	
	def showProductReport = { ProductReportCommand command -> 	
		
		//if (!command?.product) { 
		//	throw new Exception("Unable to locate product " + params?.product?.id)
		//}
		
		if (!command?.hasErrors()) {			
			reportService.generateProductReport(command)
		}
						
		[command : command]
		
		
	}
	
	
	def showTransactionReport = { 
		
		InventoryReportCommand command = new InventoryReportCommand();
		command.rootCategory = productService.getRootCategory();
		
		
		[command : command ]
	}
	
	
	def generateTransactionReport = { InventoryReportCommand command -> 
		// We always need to initialize the root category 
		command.rootCategory = productService.getRootCategory();
		if (!command?.hasErrors()) { 			
			reportService.generateTransactionReport(command);			
		}
		render(view: 'showTransactionReport', model: [command : command])
	}
	
	def showShippingReport = { ChecklistReportCommand command ->
		command.rootCategory = productService.getRootCategory();
		if (!command?.hasErrors()) {
			reportService.generateShippingReport(command);
		}
		[command : command]
	}
	
	def showPaginatedPackingListReport = { ChecklistReportCommand command ->
		command.rootCategory = productService.getRootCategory();
		if (!command?.hasErrors()) {
			reportService.generateShippingReport(command);
		}
		[command : command]
	}	
	
	def printShippingReport = { ChecklistReportCommand command ->
		command.rootCategory = productService.getRootCategory();
		if (!command?.hasErrors()) {
			reportService.generateShippingReport(command);
		}
		[command : command]
	}
	
	def printPaginatedPackingListReport = { ChecklistReportCommand command ->
		try {
			command.rootCategory = productService.getRootCategory();
			if (!command?.hasErrors()) {
				reportService.generateShippingReport(command);
			}
		} catch (Exception e) {
			log.error("error", e)
			e.printStackTrace()
		}
		[command : command]
	}
	

	def downloadTransactionReport = {		
		def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort

		// JSESSIONID is required because otherwise the login page is rendered
		def url = baseUri + params.url + ";jsessionid=" + session.getId()		
		url += "?print=true" 
		url += "&location.id=" + params.location.id
		url += "&category.id=" + params.category.id
		url += "&startDate=" + params.startDate
		url += "&endDate=" + params.endDate
		url += "&showTransferBreakdown=" + params.showTransferBreakdown
		url += "&hideInactiveProducts=" + params.hideInactiveProducts
		url += "&insertPageBreakBetweenCategories=" + params.insertPageBreakBetweenCategories
		url += "&includeChildren=" + params.includeChildren
		url += "&includeEntities=true" 

		// Let the browser know what content type to expect
		//response.setHeader("Content-disposition", "attachment;") // removed filename=
		response.setContentType("application/pdf")

		// Render pdf to the response output stream
		log.info "BaseUri is $baseUri"	
		log.info("Session ID: " + session.id)
		log.info "Fetching url $url"
		reportService.generatePdf(url, response.getOutputStream())
	}
	
	def downloadShippingReport = {		
		if (params.format == 'docx') { 
			def tempFile = documentService.generateChecklistAsDocx()
	//		def filename = "shipment-checklist.docx"
			//response.setHeader("Content-disposition", "attachment; filename=" + filename);
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
			response.outputStream << tempFile.readBytes()
		} 
		else if (params.format == 'pdf') { 
			def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort
			def url = baseUri + params.url + ";jsessionid=" + session.getId()
			url += "?print=true&orientation=portrait"
			url += "&shipment.id=" + params.shipment.id
			url += "&includeEntities=true" 
			log.info "Fetching url $url"	
			response.setContentType("application/pdf")
			//response.setHeader("Content-disposition", "attachment;") // removed filename=	
			reportService.generatePdf(url, response.getOutputStream())
		}
		else { 
			throw new UnsupportedOperationException("Format '${params.format}' not supported")
		}
	}


	
	
		
}