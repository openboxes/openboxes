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

import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.report.ChecklistReportCommand
import org.pih.warehouse.report.InventoryReportCommand
import org.pih.warehouse.report.ProductReportCommand

class ReportController {
	
	def documentService
	def inventoryService
	def productService
	def reportService


    def getCsv(list) {
        println list

        def csv = "";

        csv+= "Status,"
        csv+= "Product group,"
        csv+= "Product codes,"
        csv+= "Min,"
        csv+= "Reorder,"
        csv+= "Max,"
        csv+= "QoH,"
        csv+= "Value"
        csv+= "\n"

                //StringEscapeUtils.escapeCsv(product?.name?:"")
        // "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}"
        list.each { row ->
            csv += row.status + ","
            csv += StringEscapeUtils.escapeCsv(row.name) + ","
            csv += StringEscapeUtils.escapeCsv(row.productCodes.join(",")) + ","
            csv += row.minQuantity + ","
            csv += row.reorderQuantity + ","
            csv += row.maxQuantity + ","
            csv += row.onHandQuantity + ","
            csv += row.totalValue + ","
            csv += "\n"
        }

        return csv;
    }


    def exportInventoryReport = {
        println "Export inventory report " + params
        def map = []
        def location = Location.get(session.warehouse.id)
        if (params.list("status")) {
            def data = reportService.calculateQuantityOnHandByProductGroup(location.id)
            params.list("status").each {
                println it
                map += data.productGroupDetails[it].values()
            }
            map.unique()
        }

        def filename = "Stock report - " + location.name + ".csv"
        response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
        render(contentType: "text/csv", text:getCsv(map))
        return;
    }

	def showInventoryReport = {


	}


    def showInventorySamplingReport = {

        def sw = new StringWriter()
        def count = (params.n?:10).toInteger()
        def location = Location.get(session.warehouse.id)
        def inventoryItems = []

        try {
            inventoryItems = inventoryService.getInventorySampling(location, count);

            if (inventoryItems) {

                println inventoryItems
                //sw.append(csvrows[0].keySet().join(",")).append("\n")
                sw.append("Product Code").append(",")
                sw.append("Product").append(",")
                sw.append("Lot number").append(",")
                sw.append("Expiration date").append(",")
                sw.append("Bin location").append(",")
                sw.append("On hand quantity").append(",")
                sw.append("\n")
                inventoryItems.each { inventoryItem ->
                    if (inventoryItem) {
                        def inventoryLevel = inventoryItem?.product?.getInventoryLevel(location.id)
                        sw.append('"' + (inventoryItem?.product?.productCode?:"").toString()?.replace('"','""') + '"').append(",")
                        sw.append('"' + (inventoryItem?.product?.name?:"").toString()?.replace('"','""') + '"').append(",")
                        sw.append('"' + (inventoryItem?.lotNumber?:"").toString()?.replace('"','""') + '"').append(",")
                        sw.append('"' + inventoryItem?.expirationDate.toString()?.replace('"','""') + '"').append(",")
                        sw.append('"' + (inventoryLevel?.binLocation?:"")?.toString()?.replace('"','""') + '"').append(",")
                        sw.append("\n")
                    }
                }
            }

        } catch (RuntimeException e) {
            log.error (e.message)
            sw.append(e.message)
        }




        //render sw.toString()

        response.setHeader("Content-disposition", "attachment; filename='Inventory-sampling-${new Date().format("yyyyMMdd-hhmmss")}.csv'")
        render(contentType:"text/csv", text: sw.toString(), encoding:"UTF-8")

    }



    def showConsumptionReport = {

        def transactions = Transaction.findAllByTransactionDateBetween(new Date()-10, new Date())

        [transactions: transactions]
    }


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

	def printPickListReport = { ChecklistReportCommand command ->

		Map binLocations
		//command.rootCategory = productService.getRootCategory();
		if (!command?.hasErrors()) {
			reportService.generateShippingReport(command);
			binLocations = inventoryService.getBinLocations(command.shipment)
		}
		[command : command, binLocations: binLocations]
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