package org.pih.warehouse.batch
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.pih.warehouse.core.Constants;
import org.pih.warehouse.core.Location;
import org.pih.warehouse.importer.InventoryExcelImporter;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.inventory.Transaction;
import org.pih.warehouse.inventory.TransactionEntry;
import org.pih.warehouse.inventory.TransactionType;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.springframework.validation.Errors;
import org.grails.plugins.excelimport.ExcelImportUtils;
import au.com.bytecode.opencsv.CSVReader;


class ImportService {

	def inventoryService
	
    boolean transactional = true
		
	/**
	 * Reads a file for the given filename and generates an object that mirrors the 
	 * file.  Also preprocesses the object to make sure that the data is formatted
	 * correctly. 
	 * 
	 * @param filename
	 * @param errors
	 * @return
	 */
	public List prepareData(Location location, String filename, Errors errors) { 
		log.debug "Prepare inventory from file " + filename

		
		def inventoryImporter = new InventoryExcelImporter();
		def inventoryMapList = inventoryImporter.inventoryItems;
		
		inventoryImporter.validate();


		return inventoryMapList
	}


}
