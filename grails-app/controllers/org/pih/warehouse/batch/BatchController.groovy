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
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.importer.InventoryLevelExcelImporter
import org.pih.warehouse.importer.ProductExcelImporter
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest

// import au.com.bytecode.opencsv.CSVReader;


class BatchController {

    def dataService
	def inventoryService
	def importService

	def index = { }


    def uploadData = { ImportDataCommand command ->

        if (request instanceof DefaultMultipartHttpServletRequest) {
            def uploadFile = request.getFile('xlsFile');
            if(!uploadFile.empty){
                println "Class: ${uploadFile.class}"
                println "Name: ${uploadFile.name}"
                println "OriginalFileName: ${uploadFile.originalFilename}"
                println "Size: ${uploadFile.size}"
                println "ContentType: ${uploadFile.contentType}"

                def webRootDir = servletContext.getRealPath("/")
                def userDir = new File(webRootDir, "/uploads/")
                userDir.mkdirs()
                def localFile = new File(userDir, uploadFile.originalFilename)
                uploadFile.transferTo( localFile )
            }
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
						throw new RuntimeException(e);
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
					log.info command.type
					if (command.type == "inventory")
						dataImporter = new InventoryExcelImporter(command?.filename);
					else if (command.type == "product")
						dataImporter = new ProductExcelImporter(command?.filename)
                    else if (command.type == "inventoryLevel")
                        dataImporter = new InventoryLevelExcelImporter(command?.filename)
					else
						throw new RuntimeException("Unable to import data using ${command.type} importer")
				}
				catch (OfficeXmlFileException e) {
                    log.error ("Error with import file " + e.message, e)
					command.errors.reject("importFile", e.message)
				}
				
				if (dataImporter) { 

					println "Using data importer ${dataImporter.class.name}"

			  	    // Get data from importer (should be done as a separate step 'processData' or within 'validateData')
					command.data = dataImporter.data

                    //render command.data as JSON;
                    //return;

					// Validate data using importer (might change data)
					//dataImporter.validateData(command);
					
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

				
				// If there are no errors and the user requests to import the data, we should execute the import
				if (!command.hasErrors() && params.importNow) {
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

