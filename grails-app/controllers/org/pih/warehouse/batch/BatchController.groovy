package org.pih.warehouse.batch

import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.importer.ImportDataCommand;
import org.pih.warehouse.importer.InventoryExcelImporter;
import org.pih.warehouse.importer.ProductExcelImporter;
import org.pih.warehouse.product.Product;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import au.com.bytecode.opencsv.CSVReader;


class BatchController {

	def inventoryService
	def importService

	def index = { }

	def importData = { ImportDataCommand command ->
		
		log.info params 
		log.info command.location
		
		def dataMapList = null;
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
					//else 
					//	throw new RuntimeException("Unable to import data using unknown importer")
				}
				catch (OfficeXmlFileException e) { 
					command.errors.reject("importFile", e.message)
				}
				
				if (dataImporter) { 
					// Get data from importer (should be done as a separate step 'processData' or within 'validateData')
					command.data = dataImporter.data
					
					// Validate data using importer (might change data)
					dataImporter.validateData(command);
					
					//command.data = dataImporter.data
					command.columnMap = dataImporter.columnMap
					
				}
				else { 
					
				}
				if (command?.data?.isEmpty()) {
					//flash.message = "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args:[localFile.getAbsolutePath()])}"
					//command.reject ...
					command.errors.reject("importFile", "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args:[localFile.getAbsolutePath()])}")
				}

				
				// If there are no errors and the user requests to import the data, we should execute the import
				if (!command.hasErrors() && params.importNow) {
					
					dataImporter.importData(command)
					
					if (!command.errors.hasErrors()) {
						flash.message = "${warehouse.message(code: 'inventoryItem.importSuccess.message', args:[localFile.getAbsolutePath()])}"
						redirect(action: "importData");
						return;
					}
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

