/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.batch

import grails.converters.JSON
import org.apache.poi.poifs.filesystem.OfficeXmlFileException
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.runtime.InvokerHelper
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.importer.InventoryLevelExcelImporter
import org.pih.warehouse.importer.LocationExcelImporter
import org.pih.warehouse.importer.PersonExcelImporter
import org.pih.warehouse.importer.ProductExcelImporter
import org.pih.warehouse.importer.ProductSupplierExcelImporter
import org.pih.warehouse.importer.UserExcelImporter
import org.pih.warehouse.importer.UserLocationExcelImporter
import org.pih.warehouse.product.ProductSupplier
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest

// import au.com.bytecode.opencsv.CSVReader;


class BatchController {

    def dataService
    def documentService
	def inventoryService
	def importService
	def grailsApplication
	def genericApiService

	def index = { }


    def uploadData = { ImportDataCommand command ->

        if (request instanceof DefaultMultipartHttpServletRequest) {
            def uploadFile = request.getFile('xlsFile');
            if(!uploadFile.empty){
                def webRootDir = servletContext.getRealPath("/")
                def userDir = new File(webRootDir, "/uploads/")
                userDir.mkdirs()
                def localFile = new File(userDir, uploadFile.originalFilename)
                uploadFile.transferTo( localFile )
            }
        }
    }


	def downloadExcel = {
		println "Download XLS template " + params

		def objects = genericApiService.getList(params.type, [:])
		def domainClass = genericApiService.getDomainClass(params.type)
		def data = dataService.transformObjects(objects, domainClass.PROPERTIES)

		response.contentType = "application/vnd.ms-excel"
		response.setHeader 'Content-disposition', "attachment; filename=\"${params.type}.xls\""
		documentService.generateExcel(response.outputStream, data)
		response.outputStream.flush()
	}

    def downloadTemplate = {
        println "Download XLS template " + params
        def filename = params.template
        try {
            def file = documentService.findFile("templates/" + filename)
            response.contentType = "application/vnd.ms-excel"
            response.setHeader 'Content-disposition', "attachment; filename=\"${filename}\""
            response.outputStream << file.bytes
            response.outputStream.flush()
        }
        catch (FileNotFoundException e) {
            response.status = 404
        }
    }

    def downloadCsvTemplate = {
        println "Download csv template " + params
        def filename = params.template
        try {
            def file = documentService.findFile("templates/" + filename)
            response.contentType = "text/csv"
            response.setHeader 'Content-disposition', "attachment; filename=\"${filename}\""
            response.outputStream << file.bytes
            response.outputStream.flush()
        }
        catch (FileNotFoundException e) {
            response.status = 404
        }
    }


    def importData = { ImportDataCommand command ->
		
		log.info params 
		log.info command.location

		// def dataMapList = null;
		if ("POST".equals(request.getMethod())) {
			File localFile = null;
			if (request instanceof DefaultMultipartHttpServletRequest) {
				def uploadFile = request.getFile('xlsFile');
				if (!uploadFile?.empty) {
					try {
						localFile = new File("uploads/" + uploadFile.originalFilename);
						localFile.mkdirs()
						uploadFile.transferTo(localFile);
						session.localFile = localFile;
						//flash.message = "File uploaded successfully"

					} catch (Exception e) {
						//throw new RuntimeException(e);
                        flash.message = "Unable to upload file due to exception: " + e.message
                        return;
					}
				}
				else {
					flash.message = "${warehouse.message(code: 'inventoryItem.emptyFile.message')}"
				}
			}
			// Otherwise, we need to retrieve the file from the session
			else {
				localFile = session.localFile
			}

			def dataImporter
			//if (!command?.type) { 
			//	command.errors.reject("${warehouse.message(code: 'importDataCommand.type.invalid')}")
			//}

			if (localFile) {
				log.info "Local xls file " + localFile.getAbsolutePath()
				command.importFile = localFile
				command.filename = localFile.getAbsolutePath()
				command.location = Location.get(session.warehouse.id)
				try { 
					// Need to choose the right importer 
					switch(command.type) {
						case "inventory":
							dataImporter = new InventoryExcelImporter(command?.filename);
							break;
						case "inventoryLevel":
							dataImporter = new InventoryLevelExcelImporter(command?.filename)
							break;
						case "location":
							dataImporter = new LocationExcelImporter(command?.filename)
							break;
						case "person":
							dataImporter = new PersonExcelImporter(command?.filename)
							break;
						case "product":
							dataImporter = new ProductExcelImporter(command?.filename)
							break;
						case "productSupplier":
							dataImporter = new ProductSupplierExcelImporter(command?.filename)
							break;
						case "user":
							dataImporter = new UserExcelImporter(command?.filename)
							break;
						case "userLocation":
							dataImporter = new UserLocationExcelImporter(command?.filename)
							break;
						default:
	                        command.errors.reject("type", "${warehouse.message(code: 'import.invalidType.message', default:'Please choose a valid import type')}")
                    }
				}
				catch (OfficeXmlFileException e) {
                    log.error ("Error with import file " + e.message, e)
					command.errors.reject("importFile", e.message)
				}
				
				if (dataImporter) { 

					println "Using data importer ${dataImporter.class.name}"

			  	    // Get data from importer (should be done as a separate step 'processData' or within 'validateData')
					command.data = dataImporter.data

					// Validate data using importer (might change data)
					dataImporter.validateData(command);
					
					//command.data = dataImporter.data
					command.columnMap = dataImporter.columnMap
					
				}
				//else {
                //    command.errors.reject("importFile", "${warehouse.message(code: '.message', args:[localFile.getAbsolutePath()])}")
				//}


				if (command?.data?.isEmpty()) {
					//flash.message = "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args:[localFile.getAbsolutePath()])}"
					//command.reject ...
					command.errors.reject("importFile", "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args:[localFile.getAbsolutePath()])}")
				}

                if (command.type == 'inventory' && !command.date) {
                    command.errors.reject("date", "${warehouse.message(code: 'import.inventoryImportMustHaveDate.message', default:"Inventory import must specify the date of the stock count")}")
                }
				
				// If there are no errors and the user requests to import the data, we should execute the import
				if (!command.hasErrors() && params.import) {
					println "Data is about to be imported ..."
					dataImporter.importData(command)

                    println "Finished importing data"
					if (!command.errors.hasErrors()) {
                        println "No errors"
						flash.message = "${warehouse.message(code: 'inventoryItem.importSuccess.message', args:[localFile.getAbsolutePath()])}"
						redirect(action: "importData");
						return;
					}
                    println "There were errors"
				}
				else if (!command.hasErrors()) { 
					flash.message = "${warehouse.message(code: 'inventoryItem.dataReadyToBeImported.message')}"
				}


				render(view: "importData", model: [ commandInstance: command]);
			}
			else {
				flash.message = "${warehouse.message(code: 'inventoryItem.notValidXLSFile.message')}"
			}

		}
	}

}




class ImportProductsCommand {
	def filename
	def importFile
	def products
	
	static constraints = {
	
	}
	
}

class ImportInventoryCommand {
	
	def filename
	def importFile
	def transactionInstance
	def warehouseInstance
	def inventoryInstance
	def products
	def transactionEntries
	def categories
	def inventoryItems
	
	static constraints = {
		
	}
}

