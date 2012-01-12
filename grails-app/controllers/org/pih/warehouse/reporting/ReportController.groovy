package org.pih.warehouse.reporting


import org.pih.warehouse.core.Constants;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import grails.converters.*;

import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.DialogForm;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.core.UnitOfMeasure;
import org.pih.warehouse.inventory.Inventory;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.InventoryService;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.io.util.MyUserAgent;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.report.ChecklistReportCommand;
import org.pih.warehouse.report.InventoryReportCommand;
import org.pih.warehouse.report.InventoryReportEntryCommand;
import org.pih.warehouse.report.ProductReportCommand;
import org.pih.warehouse.report.ProductReportEntryCommand;
import org.pih.warehouse.shipping.Container;
import org.pih.warehouse.shipping.ShipmentItem;
import org.pih.warehouse.shipping.Shipper;
import org.pih.warehouse.shipping.ShipperService;
import org.pih.warehouse.shipping.Shipment;

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
	
	
	def showTransactionReport = { InventoryReportCommand cmd -> 
		
		// We always need to initialize the root category 
		cmd.rootCategory = productService.getRootCategory();
		if (!cmd?.hasErrors()) { 			
			reportService.generateTransactionReport(cmd);			
		}
		[cmd : cmd]
	}
	
	

	def downloadTransactionReport = {		
		def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort
		log.info "BaseUri is $baseUri"	
		log.info("session ID: " + session.id)

		// JSESSIONID is required because otherwise we get a 
		def url = baseUri + params.url //+ ";JSESSIONID=" + session.getId()		
		url += "?print=true" 
		url += "&location.id=" + params.location.id
		url += "&category.id=" + params.category.id
		url += "&startDate=" + params.startDate
		url += "&endDate=" + params.endDate
		log.info "Fetching url $url"

		response.setContentType("application/pdf")
		//response.setHeader("Content-disposition", "attachment;") // removed filename=

		reportService.generatePdf(url, response.getOutputStream())
		//byte[] content = generatePdf(url)
		//response.setContentLength(content.length)
		//response.getOutputStream().write(content)
	}
	
	def showChecklistReport = { ChecklistReportCommand command ->
		command.rootCategory = productService.getRootCategory();
		if (!command?.hasErrors()) {
			reportService.generateChecklistReport(command);
		}
		[command : command]
	}
	
	def downloadChecklistReport = {
		
		if (params.format == 'docx') { 
			def tempFile = documentService.generateChecklistAsDocx()
			def filename = "Checklist.docx"
			response.setHeader("Content-disposition", "attachment; filename=" + filename);
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
			response.outputStream << tempFile.readBytes()
		} 
		else if (params.format == 'pdf') { 
			def baseUri = request.scheme + "://" + request.serverName + ":" + request.serverPort
			def url = baseUri + params.url + ";jsessionid=" + session.getId()
			url += "?print=true&orientation=portrait"
			url += "&shipment.id=" + params.shipment.id
			log.info "Fetching url $url"	
			response.setContentType("application/pdf")
			response.setHeader("Content-disposition", "attachment;") // removed filename=	
			reportService.generatePdf(url, response.getOutputStream())
		}
		else { 
			throw new UnsupportedOperationException("Format '${params.format}' not supported")
		}
		return
	}


	
	
		
}